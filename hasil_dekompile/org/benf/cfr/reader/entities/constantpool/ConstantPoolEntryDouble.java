/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryLiteral;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryDouble
extends AbstractConstantPoolEntry
implements ConstantPoolEntryLiteral {
    private final double value;

    public ConstantPoolEntryDouble(ConstantPool cp, ByteData data) {
        super(cp);
        this.value = data.getDoubleAt(1L);
    }

    @Override
    public long getRawByteLength() {
        return 9L;
    }

    @Override
    public void dump(Dumper d) {
        d.print("CONSTANT_Double " + this.value);
    }

    public double getValue() {
        return this.value;
    }

    @Override
    public StackType getStackType() {
        return StackType.DOUBLE;
    }
}

