/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public class EmptyMatchResultCollector
implements MatchResultCollector {
    @Override
    public void clear() {
    }

    @Override
    public void collectStatement(String name, StructuredStatement statement) {
    }

    @Override
    public void collectMatches(String name, WildcardMatch wcm) {
    }
}

