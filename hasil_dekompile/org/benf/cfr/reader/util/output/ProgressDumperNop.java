/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.util.output;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.util.output.ProgressDumper;

public class ProgressDumperNop
implements ProgressDumper {
    public static final ProgressDumper INSTANCE = new ProgressDumperNop();

    private ProgressDumperNop() {
    }

    @Override
    public void analysingType(JavaTypeInstance type) {
    }

    @Override
    public void analysingPath(String path) {
    }
}

