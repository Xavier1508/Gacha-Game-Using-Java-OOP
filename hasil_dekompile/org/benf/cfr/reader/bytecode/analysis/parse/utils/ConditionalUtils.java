/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.TernaryExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.util.MiscUtils;

public class ConditionalUtils {
    public static ConditionalExpression simplify(ConditionalExpression condition) {
        int conditionSize;
        ConditionalExpression applyDemorgan = condition.getDemorganApplied(false);
        int demorganSize = applyDemorgan.getSize(Precedence.WEAKEST);
        if (demorganSize < (conditionSize = condition.getSize(Precedence.WEAKEST))) {
            condition = applyDemorgan;
        }
        condition = condition.getRightDeep();
        return condition;
    }

    public static Expression simplify(TernaryExpression condition) {
        if (condition.getInferredJavaType().getRawType() != RawJavaType.BOOLEAN) {
            return condition;
        }
        Expression e1 = condition.getLhs();
        Expression e2 = condition.getRhs();
        ConditionalExpression pred = condition.getCondition();
        if (e1.equals(Literal.TRUE) && e2.equals(Literal.FALSE)) {
            MiscUtils.handyBreakPoint();
        } else if (e1.equals(Literal.FALSE) && e2.equals(Literal.TRUE)) {
            pred = pred.getNegated();
        } else {
            return condition;
        }
        return ConditionalUtils.simplify(pred);
    }
}

