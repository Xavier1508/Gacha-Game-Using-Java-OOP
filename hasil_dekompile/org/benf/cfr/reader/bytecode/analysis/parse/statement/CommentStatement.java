/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.loc.BytecodeLoc;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.output.Dumper;

public class CommentStatement
extends AbstractStatement {
    private final Expression text;

    private CommentStatement(Expression expression) {
        super(BytecodeLoc.NONE);
        this.text = expression;
    }

    public CommentStatement(String text) {
        this(new Literal(TypedLiteral.getString(text)));
    }

    public CommentStatement(Statement statement) {
        this(new StatementExpression(statement));
    }

    @Override
    public BytecodeLoc getCombinedLoc() {
        return this.getLoc();
    }

    @Override
    public Statement deepClone(CloneHelper cloneHelper) {
        return new CommentStatement(cloneHelper.replaceOrClone(this.text));
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.dump(this.text);
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        this.text.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.text.applyExpressionRewriter(expressionRewriter, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.text.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new StructuredComment(this.text);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && this.getClass() == o.getClass();
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (this == o) {
            return true;
        }
        return o != null && this.getClass() == o.getClass();
    }

    private static class StatementExpression
    extends AbstractExpression {
        private Statement statement;
        private static InferredJavaType javaType = new InferredJavaType(RawJavaType.VOID, InferredJavaType.Source.EXPRESSION);

        private StatementExpression(Statement statement) {
            super(BytecodeLoc.NONE, javaType);
            this.statement = statement;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public BytecodeLoc getCombinedLoc() {
            return this.getLoc();
        }

        @Override
        public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
            this.statement.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers);
            return this;
        }

        @Override
        public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            this.statement.rewriteExpressions(expressionRewriter, ssaIdentifiers);
            return this;
        }

        @Override
        public Expression applyReverseExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            return this.applyExpressionRewriter(expressionRewriter, ssaIdentifiers, statementContainer, flags);
        }

        @Override
        public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
            this.statement.collectLValueUsage(lValueUsageCollector);
        }

        @Override
        public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
            return false;
        }

        @Override
        public Expression deepClone(CloneHelper cloneHelper) {
            return this;
        }

        @Override
        public Precedence getPrecedence() {
            return Precedence.WEAKEST;
        }

        @Override
        public Dumper dumpInner(Dumper d) {
            return d.dump(this.statement);
        }
    }
}

