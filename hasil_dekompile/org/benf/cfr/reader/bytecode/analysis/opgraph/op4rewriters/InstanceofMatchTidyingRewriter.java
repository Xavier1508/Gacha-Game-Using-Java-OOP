/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.InstanceOfExpressionDefining;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredDefinition;
import org.benf.cfr.reader.util.collections.ListFactory;
import org.benf.cfr.reader.util.collections.MapFactory;
import org.benf.cfr.reader.util.collections.SetFactory;

public class InstanceofMatchTidyingRewriter {
    private final Map<LocalVariable, Integer> locals = MapFactory.newMap();
    private final Set<LocalVariable> removeCandidates = SetFactory.newOrderedSet();
    private final Map<LValue, List<StructuredStatement>> definitions = MapFactory.newOrderedMap();
    private StructuredStatement last;

    public static void rewrite(Op04StructuredStatement block) {
        new InstanceofMatchTidyingRewriter().doRewrite(block);
    }

    private void doRewrite(Op04StructuredStatement block) {
        ExpressionRewriterTransformer et = new SearchPass(new SearchPassRewriter());
        et.transform(block);
        this.removeCandidates.removeAll(this.locals.keySet());
        this.removeCandidates.retainAll(this.definitions.keySet());
        if (this.removeCandidates.isEmpty()) {
            return;
        }
        et = new ExpressionRewriterTransformer(new AssignRemover());
        et.transform(block);
        for (List<StructuredStatement> definitionList : this.definitions.values()) {
            for (StructuredStatement definition : definitionList) {
                definition.getContainer().nopOut();
            }
        }
    }

    private void addDefinition(StructuredStatement in, LValue lvalue) {
        List<StructuredStatement> defl = this.definitions.get(lvalue);
        if (defl == null) {
            defl = ListFactory.newList();
            this.definitions.put(lvalue, defl);
        }
        defl.add(in);
    }

    private class AssignRemover
    extends AbstractExpressionRewriter {
        private AssignRemover() {
        }

        @Override
        public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            InstanceOfExpressionDefining defining;
            Expression lhs;
            if (expression instanceof InstanceOfExpressionDefining && (lhs = (defining = (InstanceOfExpressionDefining)expression).getLhs()) instanceof AssignmentExpression && InstanceofMatchTidyingRewriter.this.removeCandidates.contains(((AssignmentExpression)lhs).getlValue())) {
                return defining.withReplacedExpression(((AssignmentExpression)lhs).getrValue());
            }
            return super.rewriteExpression(expression, ssaIdentifiers, statementContainer, flags);
        }
    }

    private class SearchPassRewriter
    extends AbstractExpressionRewriter {
        private SearchPassRewriter() {
        }

        @Override
        public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            if (expression instanceof InstanceOfExpressionDefining) {
                InstanceOfExpressionDefining expressionDefining = (InstanceOfExpressionDefining)expression;
                Expression lhs = expressionDefining.getLhs();
                if (lhs instanceof AssignmentExpression && ((AssignmentExpression)lhs).getlValue() instanceof LocalVariable) {
                    ((AssignmentExpression)lhs).getrValue().applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
                    InstanceofMatchTidyingRewriter.this.removeCandidates.add((LocalVariable)((AssignmentExpression)lhs).getlValue());
                    return expression;
                }
                if (InstanceofMatchTidyingRewriter.this.last != null && lhs instanceof LValueExpression && ((LValueExpression)lhs).getLValue() instanceof LocalVariable) {
                    StructuredAssignment assigment;
                    Expression rhs;
                    LocalVariable lValue = (LocalVariable)((LValueExpression)lhs).getLValue();
                    if (InstanceofMatchTidyingRewriter.this.last instanceof StructuredAssignment && (rhs = (assigment = (StructuredAssignment)InstanceofMatchTidyingRewriter.this.last).getRvalue()) instanceof LValueExpression && ((LValueExpression)rhs).getLValue() instanceof LocalVariable && (Integer)InstanceofMatchTidyingRewriter.this.locals.get(lValue) == 1 && lValue.equals(assigment.getLvalue())) {
                        InstanceofMatchTidyingRewriter.this.removeCandidates.add(lValue);
                        InstanceofMatchTidyingRewriter.this.locals.remove(lValue);
                        InstanceofMatchTidyingRewriter.this.addDefinition(InstanceofMatchTidyingRewriter.this.last, lValue);
                        InstanceofMatchTidyingRewriter.this.last = null;
                        return ((InstanceOfExpressionDefining)expression).withReplacedExpression(rhs);
                    }
                }
            }
            return super.rewriteExpression(expression, ssaIdentifiers, statementContainer, flags);
        }

        @Override
        public LValue rewriteExpression(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            if (lValue instanceof LocalVariable) {
                Integer prev = (Integer)InstanceofMatchTidyingRewriter.this.locals.get(lValue);
                InstanceofMatchTidyingRewriter.this.locals.put((LocalVariable)lValue, prev == null ? 1 : prev + 1);
            }
            return super.rewriteExpression(lValue, ssaIdentifiers, statementContainer, flags);
        }
    }

    private class SearchPass
    extends ExpressionRewriterTransformer {
        SearchPass(ExpressionRewriter expressionRewriter) {
            super(expressionRewriter);
        }

        @Override
        public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
            if (in instanceof StructuredDefinition) {
                LValue lvalue = ((StructuredDefinition)in).getLvalue();
                InstanceofMatchTidyingRewriter.this.addDefinition(in, lvalue);
            }
            StructuredStatement res = super.transform(in, scope);
            InstanceofMatchTidyingRewriter.this.last = res;
            return res;
        }
    }
}

