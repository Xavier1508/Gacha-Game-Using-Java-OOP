/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.util.output.Dumper;

public class NamedVariableFromHint
implements NamedVariable {
    private String name;
    private int slot;
    private int idx;

    NamedVariableFromHint(String name, int slot, int idx) {
        this.name = name;
        this.slot = slot;
        this.idx = idx;
    }

    @Override
    public void forceName(String name) {
        this.name = name;
    }

    @Override
    public String getStringName() {
        return this.name;
    }

    @Override
    public Dumper dump(Dumper d) {
        return this.dump(d, false);
    }

    @Override
    public Dumper dump(Dumper d, boolean defines) {
        return d.identifier(this.name, this, defines);
    }

    @Override
    public boolean isGoodName() {
        return true;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        NamedVariableFromHint that = (NamedVariableFromHint)o;
        if (this.slot != that.slot) {
            return false;
        }
        if (this.idx != that.idx) {
            return false;
        }
        return this.name.equals(that.name);
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.slot;
        result = 31 * result + this.idx;
        return result;
    }

    public String toString() {
        return this.name + " (" + this.slot + ")";
    }
}

