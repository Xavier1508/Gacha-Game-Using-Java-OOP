/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;

public interface ComparableUnderEC {
    public boolean equivalentUnder(Object var1, EquivalenceConstraint var2);
}

