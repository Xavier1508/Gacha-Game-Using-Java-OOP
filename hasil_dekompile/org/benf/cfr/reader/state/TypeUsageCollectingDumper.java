/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.state;

import java.io.BufferedOutputStream;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.loc.HasByteCodeLoc;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.mapping.NullMapping;
import org.benf.cfr.reader.mapping.ObfuscationMapping;
import org.benf.cfr.reader.state.DetectedStaticImport;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.state.TypeUsageInformationEmpty;
import org.benf.cfr.reader.state.TypeUsageInformationImpl;
import org.benf.cfr.reader.util.collections.SetFactory;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.TypeContext;

public class TypeUsageCollectingDumper
implements Dumper {
    private final Options options;
    private final JavaRefTypeInstance analysisType;
    private final Set<JavaRefTypeInstance> refTypeInstanceSet = SetFactory.newSet();
    private final Set<JavaTypeInstance> emitted = SetFactory.newSet();
    private final Set<DetectedStaticImport> staticImports = SetFactory.newSet();

    public void addStaticUsage(JavaRefTypeInstance clazz, String name) {
        this.staticImports.add(new DetectedStaticImport(clazz, name));
    }

    public TypeUsageCollectingDumper(Options options, ClassFile analysisClass) {
        this.options = options;
        this.analysisType = (JavaRefTypeInstance)analysisClass.getClassType().getDeGenerifiedType();
        this.refTypeInstanceSet.add(TypeConstants.OBJECT);
    }

    public TypeUsageInformation getRealTypeUsageInformation() {
        return new TypeUsageInformationImpl(this.options, this.analysisType, this.refTypeInstanceSet, this.staticImports);
    }

    @Override
    public TypeUsageInformation getTypeUsageInformation() {
        return TypeUsageInformationEmpty.INSTANCE;
    }

    @Override
    public ObfuscationMapping getObfuscationMapping() {
        return NullMapping.INSTANCE;
    }

    @Override
    public Dumper label(String s, boolean inline) {
        return this;
    }

    @Override
    public void enqueuePendingCarriageReturn() {
    }

    @Override
    public Dumper removePendingCarriageReturn() {
        return this;
    }

    @Override
    public Dumper keyword(String s) {
        return this;
    }

    @Override
    public Dumper operator(String s) {
        return this;
    }

    @Override
    public Dumper separator(String s) {
        return this;
    }

    @Override
    public Dumper literal(String s, Object o) {
        return this;
    }

    @Override
    public Dumper print(String s) {
        return this;
    }

    @Override
    public Dumper methodName(String s, MethodPrototype p, boolean special, boolean defines) {
        return this;
    }

    @Override
    public Dumper packageName(JavaRefTypeInstance t) {
        return this;
    }

    @Override
    public Dumper identifier(String s, Object ref, boolean defines) {
        return this;
    }

    @Override
    public Dumper print(char c) {
        return this;
    }

    @Override
    public Dumper newln() {
        return this;
    }

    @Override
    public Dumper endCodeln() {
        return this;
    }

    @Override
    public void indent(int diff) {
    }

    @Override
    public Dumper explicitIndent() {
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public void addSummaryError(Method method, String s) {
    }

    @Override
    public boolean canEmitClass(JavaTypeInstance type) {
        return this.emitted.add(type);
    }

    @Override
    public Dumper fieldName(String name, JavaTypeInstance owner, boolean hiddenDeclaration, boolean isStatic, boolean defines) {
        return this;
    }

    @Override
    public Dumper withTypeUsageInformation(TypeUsageInformation innerclassTypeUsageInformation) {
        return this;
    }

    @Override
    public Dumper comment(String s) {
        return this;
    }

    @Override
    public Dumper beginBlockComment(boolean inline) {
        return this;
    }

    @Override
    public Dumper endBlockComment() {
        return this;
    }

    @Override
    public int getOutputCount() {
        return 0;
    }

    @Override
    public Dumper dump(JavaTypeInstance javaTypeInstance) {
        return this.dump(javaTypeInstance, TypeContext.None);
    }

    @Override
    public Dumper dump(JavaTypeInstance javaTypeInstance, TypeContext typeContext) {
        if (javaTypeInstance instanceof JavaRefTypeInstance) {
            this.refTypeInstanceSet.add((JavaRefTypeInstance)javaTypeInstance);
        }
        javaTypeInstance.dumpInto(this, this.getTypeUsageInformation(), typeContext);
        return this;
    }

    @Override
    public Dumper dump(Dumpable d) {
        d.dump(this);
        return this;
    }

    @Override
    public int getCurrentLine() {
        return 0;
    }

    @Override
    public int getIndentLevel() {
        return 0;
    }

    @Override
    public void informBytecodeLoc(HasByteCodeLoc loc) {
    }

    @Override
    public BufferedOutputStream getAdditionalOutputStream(String description) {
        throw new IllegalStateException();
    }
}

