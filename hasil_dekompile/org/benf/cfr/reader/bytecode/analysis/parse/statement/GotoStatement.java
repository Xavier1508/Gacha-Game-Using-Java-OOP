/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.loc.BytecodeLoc;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AnonBreakTarget;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ForIterStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ForStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.JumpingStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.WhileStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.JumpType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredAnonymousBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredContinue;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredGoto;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;

public class GotoStatement
extends JumpingStatement {
    private JumpType jumpType = JumpType.GOTO;

    public GotoStatement(BytecodeLoc loc) {
        super(loc);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        try {
            return dumper.print("" + (Object)((Object)this.jumpType) + " " + this.getJumpTarget().getContainer().getLabel() + ";").newln();
        }
        catch (Exception e) {
            return dumper.print("!!! " + (Object)((Object)this.jumpType) + " bad target");
        }
    }

    @Override
    public BytecodeLoc getCombinedLoc() {
        return this.getLoc();
    }

    @Override
    public Statement deepClone(CloneHelper cloneHelper) {
        GotoStatement res = new GotoStatement(this.getLoc());
        res.jumpType = this.jumpType;
        return res;
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public JumpType getJumpType() {
        return this.jumpType;
    }

    @Override
    public void setJumpType(JumpType jumpType) {
        this.jumpType = jumpType;
    }

    @Override
    public Statement getJumpTarget() {
        return this.getTargetStatement(0);
    }

    @Override
    public boolean isConditional() {
        return false;
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return false;
    }

    protected BlockIdentifier getTargetStartBlock() {
        Statement statement = this.getJumpTarget();
        if (statement instanceof WhileStatement) {
            WhileStatement whileStatement = (WhileStatement)statement;
            return whileStatement.getBlockIdentifier();
        }
        if (statement instanceof ForStatement) {
            ForStatement forStatement = (ForStatement)statement;
            return forStatement.getBlockIdentifier();
        }
        if (statement instanceof ForIterStatement) {
            ForIterStatement forStatement = (ForIterStatement)statement;
            return forStatement.getBlockIdentifier();
        }
        BlockIdentifier blockStarted = statement.getContainer().getBlockStarted();
        if (blockStarted != null) {
            switch (blockStarted.getBlockType()) {
                case UNCONDITIONALDOLOOP: {
                    return blockStarted;
                }
                case DOLOOP: {
                    return blockStarted;
                }
            }
        }
        throw new ConfusedCFRException("CONTINUE without a while " + statement.getClass());
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        switch (this.jumpType) {
            case END_BLOCK: 
            case GOTO_OUT_OF_TRY: {
                return StructuredComment.EMPTY_COMMENT;
            }
            case GOTO: 
            case GOTO_OUT_OF_IF: {
                return new UnstructuredGoto(this.getLoc());
            }
            case CONTINUE: {
                return new UnstructuredContinue(this.getLoc(), this.getTargetStartBlock());
            }
            case BREAK: {
                return new UnstructuredBreak(this.getLoc(), this.getJumpTarget().getContainer().getBlocksEnded());
            }
            case BREAK_ANONYMOUS: {
                Statement target = this.getJumpTarget();
                if (!(target instanceof AnonBreakTarget)) {
                    throw new IllegalStateException("Target of anonymous break unexpected.");
                }
                AnonBreakTarget anonBreakTarget = (AnonBreakTarget)target;
                BlockIdentifier breakFrom = anonBreakTarget.getBlockIdentifier();
                return new UnstructuredAnonymousBreak(this.getLoc(), breakFrom);
            }
        }
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        GotoStatement that = (GotoStatement)o;
        return this.jumpType == that.jumpType;
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        GotoStatement that = (GotoStatement)o;
        return constraint.equivalent((Object)this.jumpType, (Object)that.jumpType);
    }

    @Override
    public boolean fallsToNext() {
        return false;
    }
}

