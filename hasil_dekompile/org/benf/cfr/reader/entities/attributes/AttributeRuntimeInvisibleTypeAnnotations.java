/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.entities.attributes;

import org.benf.cfr.reader.entities.attributes.AttributeTypeAnnotations;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class AttributeRuntimeInvisibleTypeAnnotations
extends AttributeTypeAnnotations {
    public static final String ATTRIBUTE_NAME = "RuntimeInvisibleTypeAnnotations";

    public AttributeRuntimeInvisibleTypeAnnotations(ByteData raw, ConstantPool cp) {
        super(raw, cp);
    }

    @Override
    public String getRawName() {
        return ATTRIBUTE_NAME;
    }

    public String toString() {
        return ATTRIBUTE_NAME;
    }
}

