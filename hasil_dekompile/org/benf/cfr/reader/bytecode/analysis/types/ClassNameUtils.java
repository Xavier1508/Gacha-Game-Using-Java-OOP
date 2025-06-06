/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;

public class ClassNameUtils {
    public static String convertFromPath(String from) {
        return from.replace('/', '.');
    }

    public static String convertToPath(String from) {
        return from.replace('.', '/');
    }

    public static Pair<String, String> getPackageAndClassNames(JavaTypeInstance type) {
        return ClassNameUtils.getPackageAndClassNames(type.getRawName());
    }

    public static Pair<String, String> getPackageAndClassNames(String rawName) {
        String full = ClassNameUtils.convertFromPath(rawName);
        int idx = full.lastIndexOf(46);
        if (idx == -1) {
            return Pair.make("", rawName);
        }
        return Pair.make(full.substring(0, idx), full.substring(idx + 1));
    }

    public static String getTypeFixPrefix(JavaTypeInstance typ) {
        String rawName = typ.getRawName();
        rawName = rawName.replace("[]", "_arr").replaceAll("[*?<>. ]", "_");
        return rawName + "_";
    }
}

