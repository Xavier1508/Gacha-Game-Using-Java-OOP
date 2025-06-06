/*
 * Decompiled with CFR 0.152.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.loc.BytecodeLoc;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.LValueProp;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op3rewriters.TypeFilter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewAnonymousArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StackValue;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.ArrayVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.collections.Functional;
import org.benf.cfr.reader.util.collections.ListFactory;
import org.benf.cfr.reader.util.functors.Predicate;

public class AnonymousArray {
    private static boolean resugarAnonymousArray(Op03SimpleStatement newArray) {
        Statement stm = newArray.getStatement();
        if (!(stm instanceof AssignmentSimple)) {
            return false;
        }
        AssignmentSimple assignmentSimple = (AssignmentSimple)newArray.getStatement();
        WildcardMatch start = new WildcardMatch();
        if (!start.match(new AssignmentSimple(stm.getLoc(), start.getLValueWildCard("array"), start.getNewArrayWildCard("def")), assignmentSimple)) {
            throw new ConfusedCFRException("Expecting new array");
        }
        LValue arrayLValue = start.getLValueWildCard("array").getMatch();
        if (!(arrayLValue instanceof StackSSALabel) && !(arrayLValue instanceof LocalVariable)) {
            return false;
        }
        LValue array = arrayLValue;
        AbstractNewArray arrayDef = start.getNewArrayWildCard("def").getMatch();
        Expression dimSize0 = arrayDef.getDimSize(0);
        if (!(dimSize0 instanceof Literal)) {
            return false;
        }
        Literal lit = (Literal)dimSize0;
        if (lit.getValue().getType() != TypedLiteral.LiteralType.Integer) {
            return false;
        }
        int bound = (Integer)lit.getValue().getValue();
        if (bound < 0) {
            return false;
        }
        Op03SimpleStatement next = newArray;
        List<Expression> anon = ListFactory.newList();
        List<Op03SimpleStatement> anonAssigns = ListFactory.newList();
        AbstractExpression arrayExpression = array instanceof StackSSALabel ? new StackValue(stm.getCombinedLoc(), (StackSSALabel)array) : new LValueExpression(array);
        for (int x = 0; x < bound; ++x) {
            if (next.getTargets().size() != 1) {
                return false;
            }
            next = next.getTargets().get(0);
            WildcardMatch testAnon = new WildcardMatch();
            Literal idx = new Literal(TypedLiteral.getInt(x));
            if (!testAnon.match(new AssignmentSimple(BytecodeLoc.NONE, new ArrayVariable(new ArrayIndex(stm.getLoc(), arrayExpression, idx)), testAnon.getExpressionWildCard("val")), next.getStatement())) {
                return false;
            }
            anon.add(testAnon.getExpressionWildCard("val").getMatch());
            anonAssigns.add(next);
        }
        AssignmentSimple replacement = new AssignmentSimple(stm.getLoc(), assignmentSimple.getCreatedLValue(), new NewAnonymousArray(stm.getLoc(), arrayDef.getInferredJavaType(), arrayDef.getNumDims(), anon, false));
        newArray.replaceStatement(replacement);
        if (array instanceof StackSSALabel) {
            StackEntry arrayStackEntry = ((StackSSALabel)array).getStackEntry();
            for (Op03SimpleStatement ignored : anonAssigns) {
                arrayStackEntry.decrementUsage();
            }
        }
        SSAIdentifiers<LValue> arrayCreationSsa = newArray.getSSAIdentifiers();
        for (Op03SimpleStatement create : anonAssigns) {
            SSAIdentifiers<LValue> itemIdents = create.getSSAIdentifiers();
            arrayCreationSsa.consumeExit(itemIdents);
            create.nopOut();
        }
        return true;
    }

    public static void resugarAnonymousArrays(List<Op03SimpleStatement> statements) {
        boolean success;
        do {
            List<Op03SimpleStatement> assignments = Functional.filter(statements, new TypeFilter<AssignmentSimple>(AssignmentSimple.class));
            assignments = Functional.filter(assignments, new Predicate<Op03SimpleStatement>(){

                @Override
                public boolean test(Op03SimpleStatement in) {
                    AssignmentSimple assignmentSimple = (AssignmentSimple)in.getStatement();
                    WildcardMatch wildcardMatch = new WildcardMatch();
                    return wildcardMatch.match(new AssignmentSimple(BytecodeLoc.TODO, wildcardMatch.getLValueWildCard("array"), wildcardMatch.getNewArrayWildCard("def", 1, null)), assignmentSimple);
                }
            });
            success = false;
            for (Op03SimpleStatement assignment : assignments) {
                success |= AnonymousArray.resugarAnonymousArray(assignment);
            }
            if (!success) continue;
            LValueProp.condenseLValues(statements);
        } while (success);
    }
}

