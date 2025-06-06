/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph;

import java.util.LinkedList;
import org.benf.cfr.reader.bytecode.analysis.opgraph.IndexedStatement;

public class InstrIndex
implements Comparable<InstrIndex> {
    private final int index;
    private TempRelatives tempList;

    public InstrIndex(int index) {
        this.index = index;
        this.tempList = null;
    }

    private InstrIndex(int index, TempRelatives tempList) {
        this.index = index;
        this.tempList = tempList;
    }

    private int idx() {
        if (this.tempList == null) {
            return 0;
        }
        return this.tempList.indexOf(this);
    }

    public String toString() {
        int subidx = this.idx();
        return "lbl" + this.index + (subidx == 0 ? "" : "." + subidx);
    }

    @Override
    public int compareTo(InstrIndex other) {
        int a = this.index - other.index;
        if (a != 0) {
            return a;
        }
        if (this.tempList != other.tempList) {
            throw new IllegalStateException("Bad templists");
        }
        a = this.idx() - other.idx();
        return a;
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    private void mkTempList() {
        if (this.tempList == null) {
            this.tempList = new TempRelatives(this);
        }
    }

    public InstrIndex justBefore() {
        this.mkTempList();
        InstrIndex res = new InstrIndex(this.index, this.tempList);
        this.tempList.before(this, res);
        return res;
    }

    public InstrIndex justAfter() {
        this.mkTempList();
        InstrIndex res = new InstrIndex(this.index, this.tempList);
        this.tempList.after(this, res);
        return res;
    }

    boolean directlyPreceeds(InstrIndex other) {
        return this.index == other.index - 1;
    }

    public boolean isBackJumpTo(IndexedStatement other) {
        return this.isBackJumpTo(other.getIndex()) < 0;
    }

    private int isBackJumpTo(InstrIndex other) {
        return Integer.signum(other.compareTo(this));
    }

    public boolean isBackJumpFrom(IndexedStatement other) {
        return this.isBackJumpFrom(other.getIndex());
    }

    public boolean isBackJumpFrom(InstrIndex other) {
        return this.isBackJumpTo(other) > 0;
    }

    private static class TempRelatives {
        private final LinkedList<InstrIndex> rels = new LinkedList();

        TempRelatives(InstrIndex start) {
            this.rels.add(start);
        }

        public int indexOf(InstrIndex i) {
            return this.rels.indexOf(i);
        }

        public void before(InstrIndex than, InstrIndex isBefore) {
            int idx = this.rels.indexOf(than);
            this.rels.add(idx, isBefore);
        }

        public void after(InstrIndex than, InstrIndex isBefore) {
            int idx = this.rels.indexOf(than);
            this.rels.add(idx + 1, isBefore);
        }
    }
}

