/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.util.graph;

import org.benf.cfr.reader.util.functors.BinaryProcedure;
import org.benf.cfr.reader.util.graph.AbstractGraphVisitorFI;
import org.benf.cfr.reader.util.graph.GraphVisitor;

public class GraphVisitorFIFO<T>
extends AbstractGraphVisitorFI<T> {
    public GraphVisitorFIFO(T first, BinaryProcedure<T, GraphVisitor<T>> callee) {
        super(first, callee);
    }
}

