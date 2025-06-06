/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.opcode;

import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDeltaImpl;
import org.benf.cfr.reader.bytecode.analysis.stack.StackSim;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryCPEntry;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryDynamicInfo;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryLiteral;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodHandle;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.util.ConfusedCFRException;

public class OperationFactoryLDC
extends OperationFactoryCPEntry {
    @Override
    public StackDelta getStackDelta(JVMInstr instr, byte[] data, ConstantPoolEntry[] cpEntries, StackSim stackSim, Method method) {
        StackType stackType = OperationFactoryLDC.getStackType(cpEntries[0]);
        int requiredComputationCategory = 1;
        if (stackType.getComputationCategory() != requiredComputationCategory) {
            throw new ConfusedCFRException("Got a literal, but expected a different category");
        }
        return new StackDeltaImpl(StackTypes.EMPTY, stackType.asList());
    }

    static StackType getStackType(ConstantPoolEntry cpe) {
        if (cpe instanceof ConstantPoolEntryLiteral) {
            ConstantPoolEntryLiteral constantPoolEntryLiteral = (ConstantPoolEntryLiteral)((Object)cpe);
            return constantPoolEntryLiteral.getStackType();
        }
        if (cpe instanceof ConstantPoolEntryDynamicInfo) {
            ConstantPoolEntryDynamicInfo di = (ConstantPoolEntryDynamicInfo)cpe;
            ConstantPoolEntryNameAndType nt = di.getNameAndTypeEntry();
            JavaTypeInstance type = nt.decodeTypeTok();
            return type.getStackType();
        }
        if (cpe instanceof ConstantPoolEntryMethodHandle) {
            return TypeConstants.METHOD_HANDLE.getStackType();
        }
        if (cpe instanceof ConstantPoolEntryMethodType) {
            return TypeConstants.METHOD_TYPE.getStackType();
        }
        throw new ConfusedCFRException("Expecting a ConstantPoolEntryLiteral or ConstantPoolEntryDynamicInfo");
    }
}

