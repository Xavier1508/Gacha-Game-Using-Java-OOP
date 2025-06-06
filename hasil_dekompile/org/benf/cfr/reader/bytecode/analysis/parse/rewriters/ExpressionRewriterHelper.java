/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.rewriters;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;

public class ExpressionRewriterHelper {
    public static void applyForwards(List<Expression> list, ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        for (int x = 0; x < list.size(); ++x) {
            list.set(x, expressionRewriter.rewriteExpression(list.get(x), ssaIdentifiers, statementContainer, flags));
        }
    }

    public static void applyBackwards(List<Expression> list, ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        for (int x = list.size() - 1; x >= 0; --x) {
            list.set(x, expressionRewriter.rewriteExpression(list.get(x), ssaIdentifiers, statementContainer, flags));
        }
    }
}

