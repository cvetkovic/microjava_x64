package cvetkovic.ir;

import cvetkovic.ir.expression.ExpressionDAG;
import cvetkovic.ir.expression.ExpressionNode;
import cvetkovic.ir.expression.ExpressionNodeOperation;
import cvetkovic.parser.ast.*;
import cvetkovic.util.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.Stack;

public class IRCodeGenerator extends VisitorAdaptor {
    private ExpressionDAG currentExpressionDAG;
    private Stack<ExpressionNode> expressionNodeStack = new Stack<>();

    @Override
    public void visit(DesignatorAssignMakeLeaf DesignatorAssignMakeLeaf) {
        // make a new DAG
        currentExpressionDAG = new ExpressionDAG();

        Obj destination = ((DesignatorAssign) DesignatorAssignMakeLeaf.getParent()).getDesignator().obj;
        expressionNodeStack.push(currentExpressionDAG.getOrCreateLeaf(destination));
    }

    @Override
    public void visit(DesignatorAssign DesignatorAssign) {
        ExpressionNode src = expressionNodeStack.pop();
        ExpressionNode dest = expressionNodeStack.pop();

        currentExpressionDAG.getOrCreateNode(ExpressionNodeOperation.ASSIGNMENT, dest, src);

        currentExpressionDAG.emitQuadruples();
        System.out.println(currentExpressionDAG);
    }

    @Override
    public void visit(UnaryExpression UnaryExpression) {
        if (UnaryExpression.getExprNegative() instanceof ExpressionNegative)
            expressionNodeStack.push(currentExpressionDAG.getOrCreateNode(ExpressionNodeOperation.UNARY_MINUS, expressionNodeStack.pop()));

        /*else
            UnaryExpression.struct.setExpressionNode(currentExpressionDAG.getOrCreateLeaf(UnaryExpression.getTerm().struct.getExpressionNode()));*/
    }

    @Override
    public void visit(BinaryExpression BinaryExpression) {
        ExpressionNodeOperation operation = (BinaryExpression.getAddop() instanceof OperatorAddition ? ExpressionNodeOperation.ADDITION : ExpressionNodeOperation.SUBTRACTION);
        ExpressionNode rightChild = expressionNodeStack.pop();
        ExpressionNode leftChild = expressionNodeStack.pop();

        expressionNodeStack.push(currentExpressionDAG.getOrCreateNode(operation, leftChild, rightChild));
    }

    @Override
    public void visit(TermMultiple TermMultiple) {
        ExpressionNodeOperation operation;
        if (TermMultiple.getMulop() instanceof OperatorMultiplication)
            operation = ExpressionNodeOperation.MULTIPLICATION;
        else if (TermMultiple.getMulop() instanceof OperatorDivision)
            operation = ExpressionNodeOperation.DIVISION;
        else
            operation = ExpressionNodeOperation.MODULO;

        ExpressionNode rightChild = expressionNodeStack.pop();
        ExpressionNode leftChild = expressionNodeStack.pop();

        expressionNodeStack.push(currentExpressionDAG.getOrCreateNode(operation, leftChild, rightChild));
    }

    @Override
    public void visit(FactorFunctionCall FactorFunctionCall) {
        // not a function call but variable access
        if (FactorFunctionCall.getFactorFunctionCallParameters() instanceof NoFactorFunctionCallParameter) {
            Obj var = FactorFunctionCall.getDesignator().obj;

            expressionNodeStack.push(currentExpressionDAG.getOrCreateLeaf(var));
        }
    }

    @Override
    public void visit(FactorNumericalConst FactorNumericalConst) {
        Obj constValue = new Obj(Obj.Con, "", SymbolTable.intType);
        constValue.setAdr(FactorNumericalConst.getFactorNumConst());

        expressionNodeStack.push(currentExpressionDAG.getOrCreateLeaf(constValue));
    }
}
