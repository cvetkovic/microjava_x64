package cvetkovic.ir;

import cvetkovic.ir.expression.ExpressionDAG;
import cvetkovic.ir.expression.ExpressionNode;
import cvetkovic.ir.expression.ExpressionNodeOperation;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleIOVar;
import cvetkovic.ir.quadruple.QuadrupleIntegerConst;
import cvetkovic.ir.quadruple.QuadrupleObjVar;
import cvetkovic.parser.ast.*;
import cvetkovic.util.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class IRCodeGenerator extends VisitorAdaptor {
    private List<Quadruple> code = new ArrayList<>();

    private ExpressionDAG currentExpressionDAG;
    private Stack<ExpressionNode> expressionNodeStack = new Stack<>();

    @Override
    public void visit(MakeNewExpressionDAG MakeNewExpressionDAG) {
        // make a new DAG
        currentExpressionDAG = new ExpressionDAG();


        if (MakeNewExpressionDAG.getParent() instanceof ExprReturnStatement) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof PrintStatement) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof DesignatorAssign) {
            Obj destination = ((DesignatorAssign) MakeNewExpressionDAG.getParent()).getDesignator().obj;
            expressionNodeStack.push(currentExpressionDAG.getOrCreateLeaf(destination));
        }
        else if (MakeNewExpressionDAG.getParent() instanceof ActParsSingle) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof ActParsMultiple) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof CondFactUnary) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof CondFactBinary) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof ArrayDeclaration) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof DesignatorArrayAccess) {

        }
    }

    @Override
    public void visit(DesignatorAssign DesignatorAssign) {
        ExpressionNode src = expressionNodeStack.pop();
        ExpressionNode dest = expressionNodeStack.pop();

        currentExpressionDAG.getOrCreateNode(ExpressionNodeOperation.ASSIGNMENT, dest, src);

        code.addAll(currentExpressionDAG.emitQuadruples());
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

    //////////////////////////////////////////////////////////////////////////////////
    // METHOD
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(MethodName MethodName) {
        Quadruple instruction = new Quadruple(IRInstruction.ENTER);
        int numberOfBytes = 0;
        int i = 0;

        // determine number of bytes to allocate
        Iterator<Obj> methodObj = MethodName.obj.getLocalSymbols().iterator();
        while (methodObj.hasNext()) {
            Obj current = methodObj.next();

            // skip all parameters because space only for local variables shall be allocated
            if (i >= MethodName.obj.getLevel()) {
                Struct type = current.getType();

                if (type.getKind() == Struct.Int)
                    numberOfBytes += 4;
                else if (type.getKind() == Struct.Char)
                    numberOfBytes += 1;
            }

            i++;
        }

        instruction.setArg1(new QuadrupleIntegerConst(numberOfBytes));

        code.add(instruction);
    }

    @Override
    public void visit(MethodDecl MethodDecl) {
        Quadruple instruction = new Quadruple(IRInstruction.LEAVE);
        code.add(instruction);
    }

    //////////////////////////////////////////////////////////////////////////////////
    // INPUT / OUTPUT
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ReadStatement ReadStatement) {
        Quadruple instruction = new Quadruple(IRInstruction.SCANF);

        Obj targetObj = ReadStatement.getDesignator().obj;

        if (targetObj.getType().getKind() == Struct.Int)
            instruction.setArg2(new QuadrupleIOVar(QuadrupleIOVar.DataWidth.WORD));
        else if (targetObj.getType().getKind() == Struct.Char)
            instruction.setArg2(new QuadrupleIOVar(QuadrupleIOVar.DataWidth.BYTE));
        else
            throw new RuntimeException("IR instruction 'scanf' not supported with other data types than integer or character types.");

        instruction.setResult(new QuadrupleObjVar(targetObj));

        code.add(instruction);
    }

    @Override
    public void visit(PrintStatement PrintStatement) {
        Quadruple instruction = new Quadruple(IRInstruction.PRINTF);

        Struct targetStruct = PrintStatement.getExpr().struct;
        if (targetStruct.getKind() == Struct.Int)
            instruction.setArg1(new QuadrupleIOVar(QuadrupleIOVar.DataWidth.WORD));
        else if (targetStruct.getKind() == Struct.Char)
            instruction.setArg2(new QuadrupleIOVar(QuadrupleIOVar.DataWidth.BYTE));
        else
            throw new RuntimeException("IR instruction 'printf' not supported with other data types than integer or character types.");

        instruction.setArg2(new QuadrupleObjVar(currentExpressionDAG.getRootObj()));

        code.add(instruction);
    }


    @Override
    public void visit(Program Program) {
        // TODO: for DEBUG only (remove in production)
        System.out.println(this);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (Quadruple instruction : code) {
            builder.append(instruction);
            builder.append("\n");
        }

        return builder.toString();
    }
}
