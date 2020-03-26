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

    private ExpressionDAG expressionDAG;
    private Stack<ExpressionNode> expressionNodeStack = new Stack<>();

    private Stack<ParameterContainer> reverseParameterStack = new Stack<>();

    public IRCodeGenerator() {
        expressionDAG = new ExpressionDAG();
    }

    @Override
    public void visit(DesignatorAssign DesignatorAssign) {
        if (callWIthReturnValue) {
            callWIthReturnValue = false;
            return;
        }

        ExpressionNode src = expressionNodeStack.pop();
        ExpressionNode dest = expressionNodeStack.pop();

        System.out.println(expressionDAG);

        if (allocateArray) {
            allocateArray = false;

            code.addAll(expressionDAG.emitQuadruples());

            Quadruple instruction = new Quadruple(IRInstruction.MALLOC);
            instruction.setArg1(new QuadrupleObjVar(expressionDAG.getRootObj()));
            instruction.setResult(new QuadrupleObjVar(dest.getVariable()));

            code.add(instruction);
        }
        else {
            expressionDAG.getOrCreateNode(ExpressionNodeOperation.ASSIGNMENT, dest, src);

            code.addAll(expressionDAG.emitQuadruples());
        }

        expressionDAG = new ExpressionDAG();
    }

    @Override
    public void visit(DesignatorArrayAccess DesignatorArrayAccess) {
        ExpressionNode rightChild = expressionNodeStack.pop();
        ExpressionNode leftChild = expressionDAG.getOrCreateLeaf(DesignatorArrayAccess.obj);

        if (DesignatorArrayAccess.getParent() instanceof DesignatorAssign)
            // this means that array access is on the left side of '=' operator
            expressionNodeStack.push(expressionDAG.getOrCreateNode(ExpressionNodeOperation.ARRAY_STORE, leftChild, rightChild));
        else
            expressionNodeStack.push(expressionDAG.getOrCreateNode(ExpressionNodeOperation.ARRAY_LOAD, leftChild, rightChild));
    }

    @Override
    public void visit(UnaryExpression UnaryExpression) {
        if (UnaryExpression.getExprNegative() instanceof ExpressionNegative)
            expressionNodeStack.push(expressionDAG.getOrCreateNode(ExpressionNodeOperation.UNARY_MINUS, expressionNodeStack.pop()));
    }

    @Override
    public void visit(BinaryExpression BinaryExpression) {
        ExpressionNodeOperation operation = (BinaryExpression.getAddop() instanceof OperatorAddition ? ExpressionNodeOperation.ADDITION : ExpressionNodeOperation.SUBTRACTION);
        ExpressionNode rightChild = expressionNodeStack.pop();
        ExpressionNode leftChild = expressionNodeStack.pop();

        expressionNodeStack.push(expressionDAG.getOrCreateNode(operation, leftChild, rightChild));
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

        expressionNodeStack.push(expressionDAG.getOrCreateNode(operation, leftChild, rightChild));
    }

    private boolean callWIthReturnValue = false;

    @Override
    public void visit(FactorFunctionCall FactorFunctionCall) {
        if (FactorFunctionCall.getDesignator() instanceof DesignatorArrayAccess)
            return;

        if (FactorFunctionCall.getFactorFunctionCallParameters() instanceof NoFactorFunctionCallParameter) {
            // not a function call but variable access
            Obj var = FactorFunctionCall.getDesignator().obj;

            expressionNodeStack.push(expressionDAG.getOrCreateLeaf(var));
        }
        else
        {
            Obj methodToInvoke = FactorFunctionCall.getDesignator().obj;

            Quadruple instruction = new Quadruple(IRInstruction.CALL);
            instruction.setArg1(new QuadrupleObjVar(methodToInvoke));

            // function call with return value
            /*Struct returnType = FactorFunctionCall.getDesignator().obj.getType();
            Obj returnValue = new Obj(Obj.Var, "ttttt", returnType);

            expressionNodeStack.push(expressionDAG.getOrCreateLeaf(returnValue));*/

            ExpressionNode putResultTo =  expressionNodeStack.pop();
            instruction.setResult(new QuadrupleObjVar(putResultTo.getObj()));

            code.add(instruction);

            callWIthReturnValue = true;
        }
    }

    @Override
    public void visit(FactorNumericalConst FactorNumericalConst) {
        Obj constValue = new Obj(Obj.Con, "", SymbolTable.intType);
        constValue.setAdr(FactorNumericalConst.getFactorNumConst());

        expressionNodeStack.push(expressionDAG.getOrCreateLeaf(constValue));
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

                if (type.getKind() == Struct.Bool)
                    numberOfBytes += 1;
                else if (type.getKind() == Struct.Char)
                    numberOfBytes += 1;
                else if (type.getKind() == Struct.Int)
                    numberOfBytes += 4;
                else if (type.getKind() == Struct.Array)
                    numberOfBytes += 8; // sizeof(pointer) in x86-64 is 8 bytes
                else
                    throw new RuntimeException("Data type not supported for compilation into x86-64 machine code.");
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

    @Override
    public void visit(ExprReturnStatement ExprReturnStatement) {
        Quadruple instruction = new Quadruple(IRInstruction.RETURN);

        code.addAll(expressionDAG.emitQuadruples());
        instruction.setArg1(new QuadrupleObjVar(expressionDAG.getRootObj()));

        code.add(instruction);

        expressionDAG = new ExpressionDAG();
        expressionNodeStack.pop();
    }

    @Override
    public void visit(DesignatorInvoke DesignatorInvoke) {
        Obj methodToInvoke = DesignatorInvoke.getDesignator().obj;

        Quadruple instruction = new Quadruple(IRInstruction.CALL);
        instruction.setArg1(new QuadrupleObjVar(methodToInvoke));
        code.add(instruction);
    }



    //////////////////////////////////////////////////////////////////////////////////
    // ARRAYS
    //////////////////////////////////////////////////////////////////////////////////

    private boolean allocateArray = false;

    @Override
    public void visit(FactorArrayDeclaration FactorArrayDeclaration) {
        // 'malloc' will be done in 'Designator' visitor
        allocateArray = true;
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
        else if (targetObj.getType().getKind() == Struct.Bool)
            instruction.setArg2(new QuadrupleIOVar(QuadrupleIOVar.DataWidth.BYTE));     // shall print 0 or 1
        else
            throw new RuntimeException("IR instruction 'scanf' is not supported with other data types than integer, character or boolean.");

        instruction.setResult(new QuadrupleObjVar(targetObj));

        code.add(instruction);
    }

    @Override
    public void visit(PrintStatement PrintStatement) {
        code.addAll(expressionDAG.emitQuadruples());

        Quadruple instruction = new Quadruple(IRInstruction.PRINTF);

        Struct targetStruct = PrintStatement.getExpr().struct;
        if (targetStruct.getKind() == Struct.Int)
            instruction.setArg1(new QuadrupleIOVar(QuadrupleIOVar.DataWidth.WORD));
        else if (targetStruct.getKind() == Struct.Char)
            instruction.setArg1(new QuadrupleIOVar(QuadrupleIOVar.DataWidth.BYTE));
        else if (targetStruct.getKind() == Struct.Bool)
            instruction.setArg1(new QuadrupleIOVar(QuadrupleIOVar.DataWidth.BYTE)); // shall print 0 or 1
        else
            throw new RuntimeException("IR instruction 'printf' is not supported with other data types than integer, character or boolean.");

        instruction.setArg2(new QuadrupleObjVar(expressionDAG.getRootObj()));

        code.add(instruction);
    }

    @Override
    public void visit(MakeNewExpressionDAG MakeNewExpressionDAG) {
        // make a new DAG
        //expressionDAGStack.push(new ExpressionDAG());

        if (MakeNewExpressionDAG.getParent() instanceof ExprReturnStatement) {
            /* no need for action here */
        }
        else if (MakeNewExpressionDAG.getParent() instanceof PrintStatement) {
            /* no need for action here */
        }
        else if (MakeNewExpressionDAG.getParent() instanceof DesignatorAssign) {
            if (((DesignatorAssign)MakeNewExpressionDAG.getParent()).getDesignator() instanceof DesignatorArrayAccess)
                return;

            Obj destination = ((DesignatorAssign) MakeNewExpressionDAG.getParent()).getDesignator().obj;
            expressionNodeStack.push(expressionDAG.getOrCreateLeaf(destination));
        }
        else if (MakeNewExpressionDAG.getParent() instanceof ActParsSingle) {
            /* no need for action here */
        }
        else if (MakeNewExpressionDAG.getParent() instanceof ActParsMultiple) {
            /* no need for action here */
        }
        else if (MakeNewExpressionDAG.getParent() instanceof CondFactUnary) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof CondFactBinary) {

        }
        else if (MakeNewExpressionDAG.getParent() instanceof ArrayDeclaration) {
            /* no need for action here */
        }
        else if (MakeNewExpressionDAG.getParent() instanceof DesignatorArrayAccess) {

        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // ACTUAL PARAMETERS
    //////////////////////////////////////////////////////////////////////////////////

    private void resolveActualParameters() {
        ParameterContainer container = new ParameterContainer();

        container.instructions.addAll(expressionDAG.emitQuadruples());

        Quadruple instruction = new Quadruple(IRInstruction.PARAM);
        instruction.setArg1(new QuadrupleObjVar(expressionDAG.getRootObj()));

        expressionNodeStack.pop();

        container.instructions.add(instruction);
        reverseParameterStack.push(container);
        expressionDAG = new ExpressionDAG();
    }

    @Override
    public void visit(ConcludeCurrentParameter ConcludeCurrentParameter) {
        resolveActualParameters();
    }

    @Override
    public void visit(ActParsSingle ActParsSingle) {
        resolveActualParameters();
    }

    @Override
    public void visit(ActParsStart ActParsStart) {
        reverseParameterStack = new Stack<>();
    }

    @Override
    public void visit(ActParsEnd ActParsEnd) {
        while (!reverseParameterStack.empty())
            code.addAll(reverseParameterStack.pop().instructions);
    }

    private static class ParameterContainer {
        public List<Quadruple> instructions = new ArrayList<>();
    }

    //////////////////////////////////////////////////////////////////////////////////
    // DEBUG PURPOSE - FOR NOW
    //////////////////////////////////////////////////////////////////////////////////

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
