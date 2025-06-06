/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.util.lambda;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodHandle;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.util.ConfusedCFRException;

public class LambdaUtils {
    private static TypedLiteral getTypedLiteral(Expression e) {
        if (!(e instanceof Literal)) {
            throw new IllegalArgumentException("Expecting literal");
        }
        return ((Literal)e).getValue();
    }

    private static TypedLiteral.LiteralType getLiteralType(Expression e) {
        TypedLiteral t = LambdaUtils.getTypedLiteral(e);
        return t.getType();
    }

    public static ConstantPoolEntryMethodHandle getHandle(Expression e) {
        TypedLiteral t = LambdaUtils.getTypedLiteral(e);
        if (t.getType() != TypedLiteral.LiteralType.MethodHandle) {
            throw new IllegalArgumentException("Expecting method handle");
        }
        return (ConstantPoolEntryMethodHandle)t.getValue();
    }

    private static ConstantPoolEntryMethodType getType(Expression e) {
        TypedLiteral t = LambdaUtils.getTypedLiteral(e);
        if (t.getType() != TypedLiteral.LiteralType.MethodType) {
            throw new IllegalArgumentException("Expecting method type");
        }
        return (ConstantPoolEntryMethodType)t.getValue();
    }

    public static MethodPrototype getLiteralProto(Expression arg) {
        TypedLiteral.LiteralType flavour = LambdaUtils.getLiteralType(arg);
        switch (flavour) {
            case MethodHandle: {
                ConstantPoolEntryMethodHandle targetFnHandle = LambdaUtils.getHandle(arg);
                ConstantPoolEntryMethodRef targetMethRef = targetFnHandle.getMethodRef();
                return targetMethRef.getMethodPrototype();
            }
            case MethodType: {
                ConstantPoolEntryMethodType targetFnType = LambdaUtils.getType(arg);
                ConstantPoolEntryUTF8 descriptor = targetFnType.getDescriptor();
                return ConstantPoolUtils.parseJavaMethodPrototype(null, null, null, null, false, Method.MethodConstructor.NOT, descriptor, targetFnType.getCp(), false, false, null, descriptor.getValue());
            }
        }
        throw new ConfusedCFRException("Can't understand this lambda - disable lambdas.");
    }
}

