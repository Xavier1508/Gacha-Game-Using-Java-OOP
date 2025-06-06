/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.LinkedList;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.util.collections.Functional;
import org.benf.cfr.reader.util.functors.Predicate;

public class StaticLifter {
    private final ClassFile classFile;

    public StaticLifter(ClassFile classFile) {
        this.classFile = classFile;
    }

    public void liftStatics(Method staticInit) {
        LinkedList<ClassFileField> classFileFields = new LinkedList<ClassFileField>(Functional.filter(this.classFile.getFields(), new Predicate<ClassFileField>(){

            @Override
            public boolean test(ClassFileField in) {
                if (!in.getField().testAccessFlag(AccessFlag.ACC_STATIC)) {
                    return false;
                }
                if (in.getField().testAccessFlag(AccessFlag.ACC_SYNTHETIC)) {
                    return false;
                }
                return in.getInitialValue() == null;
            }
        }));
        if (classFileFields.isEmpty()) {
            return;
        }
        List<Op04StructuredStatement> statements = MiscStatementTools.getBlockStatements(staticInit.getAnalysis());
        if (statements == null) {
            return;
        }
        for (Op04StructuredStatement statement : statements) {
            StructuredStatement structuredStatement = statement.getStatement();
            if (structuredStatement instanceof StructuredComment) continue;
            if (!(structuredStatement instanceof StructuredAssignment)) break;
            StructuredAssignment assignment = (StructuredAssignment)structuredStatement;
            if (this.liftStatic(assignment, classFileFields)) continue;
            return;
        }
    }

    private boolean liftStatic(StructuredAssignment assignment, LinkedList<ClassFileField> classFileFields) {
        ClassFileField field;
        LValue lValue = assignment.getLvalue();
        if (!(lValue instanceof StaticVariable)) {
            return false;
        }
        StaticVariable fieldVariable = (StaticVariable)lValue;
        try {
            field = this.classFile.getFieldByName(fieldVariable.getFieldName(), fieldVariable.getInferredJavaType().getJavaTypeInstance());
        }
        catch (NoSuchFieldException e) {
            return false;
        }
        if (classFileFields.isEmpty()) {
            return false;
        }
        if (field != classFileFields.getFirst()) {
            return false;
        }
        classFileFields.removeFirst();
        if (field.getInitialValue() != null) {
            return false;
        }
        field.setInitialValue(assignment.getRvalue());
        assignment.getContainer().nopOut();
        return true;
    }
}

