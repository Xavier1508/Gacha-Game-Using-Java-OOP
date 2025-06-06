/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.entities.annotations;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.annotations.ElementValue;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class ElementValueEnum
implements ElementValue {
    private final JavaTypeInstance type;
    private final String valueName;

    public ElementValueEnum(JavaTypeInstance type, String valueName) {
        this.type = type;
        this.valueName = valueName;
    }

    @Override
    public Dumper dump(Dumper d) {
        return d.dump(this.type).print('.').print(this.valueName);
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.type);
    }

    @Override
    public ElementValue withTypeHint(JavaTypeInstance hint) {
        return this;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ElementValueEnum) {
            ElementValueEnum other = (ElementValueEnum)obj;
            return this.type.equals(other.type) && this.valueName.equals(other.valueName);
        }
        return false;
    }
}

