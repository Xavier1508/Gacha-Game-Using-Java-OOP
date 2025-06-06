/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.state;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import org.benf.cfr.reader.apiunreleased.ClassFileSource2;
import org.benf.cfr.reader.apiunreleased.JarContent;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.ClassNameUtils;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.mapping.NullMapping;
import org.benf.cfr.reader.mapping.ObfuscationMapping;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.state.OverloadMethodSetCache;
import org.benf.cfr.reader.util.AnalysisType;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.MiscConstants;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.collections.ListFactory;
import org.benf.cfr.reader.util.collections.MapFactory;
import org.benf.cfr.reader.util.collections.SetFactory;
import org.benf.cfr.reader.util.functors.BinaryFunction;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;

public class DCCommonState {
    private final ClassCache classCache;
    private final ClassFileSource2 classFileSource;
    private final Options options;
    private final Map<String, ClassFile> classFileCache;
    private Set<JavaTypeInstance> versionCollisions;
    private transient LinkedHashSet<String> couldNotLoadClasses = new LinkedHashSet();
    private final ObfuscationMapping obfuscationMapping;
    private final OverloadMethodSetCache overloadMethodSetCache;
    private final Set<JavaTypeInstance> permittedSealed;

    public DCCommonState(Options options, ClassFileSource2 classFileSource) {
        this.options = options;
        this.classFileSource = classFileSource;
        this.classCache = new ClassCache(this);
        this.classFileCache = MapFactory.newExceptionRetainingLazyMap(new UnaryFunction<String, ClassFile>(){

            @Override
            public ClassFile invoke(String arg) {
                return DCCommonState.this.loadClassFileAtPath(arg);
            }
        });
        this.versionCollisions = SetFactory.newSet();
        this.obfuscationMapping = NullMapping.INSTANCE;
        this.overloadMethodSetCache = new OverloadMethodSetCache();
        this.permittedSealed = SetFactory.newSet();
    }

    public DCCommonState(DCCommonState dcCommonState, final BinaryFunction<String, DCCommonState, ClassFile> cacheAccess) {
        this.options = dcCommonState.options;
        this.classFileSource = dcCommonState.classFileSource;
        this.classCache = new ClassCache(this);
        this.classFileCache = MapFactory.newExceptionRetainingLazyMap(new UnaryFunction<String, ClassFile>(){

            @Override
            public ClassFile invoke(String arg) {
                return (ClassFile)cacheAccess.invoke(arg, DCCommonState.this);
            }
        });
        this.versionCollisions = dcCommonState.versionCollisions;
        this.obfuscationMapping = dcCommonState.obfuscationMapping;
        this.overloadMethodSetCache = dcCommonState.overloadMethodSetCache;
        this.permittedSealed = dcCommonState.permittedSealed;
    }

    public DCCommonState(DCCommonState dcCommonState, ObfuscationMapping mapping) {
        this.options = dcCommonState.options;
        this.classFileSource = dcCommonState.classFileSource;
        this.classCache = new ClassCache(this);
        this.classFileCache = MapFactory.newExceptionRetainingLazyMap(new UnaryFunction<String, ClassFile>(){

            @Override
            public ClassFile invoke(String arg) {
                return DCCommonState.this.loadClassFileAtPath(arg);
            }
        });
        this.versionCollisions = dcCommonState.versionCollisions;
        this.obfuscationMapping = mapping;
        this.overloadMethodSetCache = dcCommonState.overloadMethodSetCache;
        this.permittedSealed = dcCommonState.permittedSealed;
    }

    public void setCollisions(Set<JavaTypeInstance> versionCollisions) {
        this.versionCollisions = versionCollisions;
    }

    public Set<JavaTypeInstance> getVersionCollisions() {
        return this.versionCollisions;
    }

    public void configureWith(ClassFile classFile) {
        this.classFileSource.informAnalysisRelativePathDetail(classFile.getUsePath(), classFile.getFilePath());
    }

    String getPossiblyRenamedFileFromClassFileSource(String name) {
        return this.classFileSource.getPossiblyRenamedPath(name);
    }

    public Set<String> getCouldNotLoadClasses() {
        return this.couldNotLoadClasses;
    }

    public ClassFile loadClassFileAtPath(String path) {
        try {
            Pair<byte[], String> content = this.classFileSource.getClassFileContent(path);
            BaseByteData data = new BaseByteData(content.getFirst());
            return new ClassFile(data, content.getSecond(), this);
        }
        catch (Exception e) {
            this.couldNotLoadClasses.add(path);
            throw new CannotLoadClassException(path, e);
        }
    }

    public DecompilerComment renamedTypeComment(String typeName) {
        String originalName = this.classCache.getOriginalName(typeName);
        if (originalName != null) {
            return new DecompilerComment("Renamed from " + originalName);
        }
        return null;
    }

    private static boolean isMultiReleaseJar(JarContent jarContent) {
        String val = jarContent.getManifestEntries().get("Multi-Release");
        if (val == null) {
            return false;
        }
        return Boolean.parseBoolean(val);
    }

    public TreeMap<Integer, List<JavaTypeInstance>> explicitlyLoadJar(String path, AnalysisType type) {
        JarContent jarContent = this.classFileSource.addJarContent(path, type);
        TreeMap<Integer, List<JavaTypeInstance>> baseRes = MapFactory.newTreeMap();
        Map<Integer, List<JavaTypeInstance>> res = MapFactory.newLazyMap(baseRes, new UnaryFunction<Integer, List<JavaTypeInstance>>(){

            @Override
            public List<JavaTypeInstance> invoke(Integer arg) {
                return ListFactory.newList();
            }
        });
        boolean isMultiReleaseJar = DCCommonState.isMultiReleaseJar(jarContent);
        for (String classPath : jarContent.getClassFiles()) {
            Matcher matcher;
            int version = 0;
            if (isMultiReleaseJar && (matcher = MiscConstants.MULTI_RELEASE_PATH_PATTERN.matcher(classPath)).matches()) {
                try {
                    String ver = matcher.group(1);
                    version = Integer.parseInt(ver);
                    classPath = matcher.group(2);
                }
                catch (Exception e) {
                    continue;
                }
            }
            if (!classPath.toLowerCase().endsWith(".class")) continue;
            res.get(version).add(this.classCache.getRefClassFor(classPath.substring(0, classPath.length() - 6)));
        }
        return baseRes;
    }

    public ClassFile getClassFile(String path) throws CannotLoadClassException {
        return this.classFileCache.get(path);
    }

    public JavaRefTypeInstance getClassTypeOrNull(String path) {
        try {
            ClassFile classFile = this.getClassFile(path);
            return (JavaRefTypeInstance)classFile.getClassType();
        }
        catch (CannotLoadClassException e) {
            return null;
        }
    }

    public ClassFile getClassFile(JavaTypeInstance classInfo) throws CannotLoadClassException {
        String path = classInfo.getRawName();
        path = ClassNameUtils.convertToPath(path) + ".class";
        return this.getClassFile(path);
    }

    public ClassFile getClassFileOrNull(JavaTypeInstance classInfo) {
        try {
            return this.getClassFile(classInfo);
        }
        catch (CannotLoadClassException ignore) {
            return null;
        }
    }

    public ClassFile getClassFileMaybePath(String pathOrName) throws CannotLoadClassException {
        if (pathOrName.endsWith(".class")) {
            return this.getClassFile(pathOrName);
        }
        File f = new File(pathOrName);
        if (f.exists()) {
            return this.getClassFile(pathOrName);
        }
        return this.getClassFile(ClassNameUtils.convertToPath(pathOrName) + ".class");
    }

    public ClassCache getClassCache() {
        return this.classCache;
    }

    public Options getOptions() {
        return this.options;
    }

    public AnalysisType detectClsJar(String path) {
        String lcPath = path.toLowerCase();
        if (lcPath.endsWith(".jar")) {
            return AnalysisType.JAR;
        }
        if (lcPath.endsWith(".war")) {
            return AnalysisType.WAR;
        }
        return AnalysisType.CLASS;
    }

    public ObfuscationMapping getObfuscationMapping() {
        return this.obfuscationMapping;
    }

    public OverloadMethodSetCache getOverloadMethodSetCache() {
        return this.overloadMethodSetCache;
    }
}

