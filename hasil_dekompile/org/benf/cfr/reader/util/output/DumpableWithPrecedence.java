/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.util.output;

import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.util.Troolean;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public interface DumpableWithPrecedence
extends Dumpable {
    public Precedence getPrecedence();

    public Dumper dumpWithOuterPrecedence(Dumper var1, Precedence var2, Troolean var3);
}

