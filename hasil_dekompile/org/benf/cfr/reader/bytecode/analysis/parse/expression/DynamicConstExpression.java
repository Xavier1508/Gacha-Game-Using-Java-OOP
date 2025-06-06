/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.loc.BytecodeLoc;
import org.benf.cfr.reader.bytecode.analysis.loc.HasByteCodeLoc;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.util.output.Dumper;

public class DynamicConstExpression
extends AbstractExpression {
    private Expression content;

    public DynamicConstExpression(BytecodeLoc loc, Expression content) {
        super(loc, content.getInferredJavaType());
        this.content = content;
    }

    @Override
    public BytecodeLoc getCombinedLoc() {
        return BytecodeLoc.combine((HasByteCodeLoc)this, this.content);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof DynamicConstExpression)) {
            return false;
        }
        return this.content.equals(((DynamicConstExpression)o).content);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.WEAKEST;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        d.print(" /* dynamic constant */ ").separator("(").dump(this.content.getInferredJavaType().getJavaTypeInstance()).separator(")").dump(this.content);
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.content = this.content.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        Expression newContent = this.content.applyExpressionRewriter(expressionRewriter, ssaIdentifiers, statementContainer, flags);
        if (newContent == this.content) {
            return this;
        }
        return new DynamicConstExpression(this.getLoc(), newContent);
    }

    @Override
    public Expression applyReverseExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        Expression newContent = this.content.applyReverseExpressionRewriter(expressionRewriter, ssaIdentifiers, statementContainer, flags);
        if (newContent == this.content) {
            return this;
        }
        return new DynamicConstExpression(this.getLoc(), newContent);
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.content.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof DynamicConstExpression)) {
            return false;
        }
        return this.content.equivalentUnder(((DynamicConstExpression)o).content, constraint);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new DynamicConstExpression(this.getLoc(), (Expression)this.content.deepClone(cloneHelper));
    }
}

