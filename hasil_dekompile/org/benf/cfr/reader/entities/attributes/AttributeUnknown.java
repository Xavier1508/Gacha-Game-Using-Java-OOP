/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.entities.attributes;

import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class AttributeUnknown
extends Attribute {
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2L;
    private static final long OFFSET_OF_REMAINDER = 6L;
    private final int length;
    private final String name;

    public AttributeUnknown(ByteData raw, String name) {
        this.length = raw.getS4At(2L);
        this.name = name;
    }

    @Override
    public String getRawName() {
        return this.name;
    }

    @Override
    public Dumper dump(Dumper d) {
        return d.print("Unknown Attribute : " + this.name);
    }

    @Override
    public long getRawByteLength() {
        return 6L + (long)this.length;
    }

    public String toString() {
        return "Unknown Attribute : " + this.name;
    }
}

