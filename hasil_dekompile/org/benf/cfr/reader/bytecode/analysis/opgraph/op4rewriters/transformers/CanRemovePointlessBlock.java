/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers;

import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;

public interface CanRemovePointlessBlock {
    public void removePointlessBlocks(StructuredScope var1);
}

