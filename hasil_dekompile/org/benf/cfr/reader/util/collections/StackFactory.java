/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.util.collections;

import java.util.Stack;

public class StackFactory {
    public static <X> Stack<X> newStack() {
        return new Stack();
    }
}

