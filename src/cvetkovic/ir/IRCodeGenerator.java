package cvetkovic.ir;

import cvetkovic.ir.expression.ExpressionDAG;
import cvetkovic.ir.expression.ExpressionNode;
import cvetkovic.ir.expression.ExpressionNodeOperation;
import cvetkovic.ir.quadruple.*;
import cvetkovic.parser.ast.*;
import cvetkovic.util.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static cvetkovic.ir.ControlFlow.generateLabel;
import static cvetkovic.ir.ControlFlow.generateUniqueLabelName;
import static cvetkovic.ir.IRInstruction.*;

public class IRCodeGenerator extends VisitorAdaptor {
    private List<Quadruple> code = new ArrayList<>();

    private ExpressionDAG expressionDAG;
    private Stack<ExpressionNode> expressionNodeStack = new Stack<>();

    private Stack<Stack<ParameterContainer>> reverseParameterStack = new Stack<>();

    public IRCodeGenerator() {
        expressionDAG = new ExpressionDAG();
    }

    @Override
    public void visit(DesignatorArrayAccess DesignatorArrayAccess) {
        ExpressionNode rightChild = expressionNodeStack.pop();
        ExpressionNode leftChild = expressionDAG.getOrCreateLeaf(DesignatorArrayAccess.obj);

        if (!(DesignatorArrayAccess.getParent() instanceof DesignatorAssign))
            expressionNodeStack.push(expressionDAG.getOrCreateNode(ExpressionNodeOperation.ARRAY_LOAD, leftChild, rightChild));
        else {
            // this means that array access is on the left side of '=' operator
            expressionNodeStack.push(leftChild);
            expressionNodeStack.push(rightChild);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // ARITHMETIC OPERATIONS
    //////////////////////////////////////////////////////////////////////////////////

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

    private static final QuadrupleIntegerConst alwaysTrueConstant;

    @Override
    public void visit(FactorNumericalConst FactorNumericalConst) {
        Obj constValue = new Obj(Obj.Con, "", SymbolTable.intType);
        constValue.setAdr(FactorNumericalConst.getFactorNumConst());

        expressionNodeStack.push(expressionDAG.getOrCreateLeaf(constValue));
    }

    private boolean inForCondition = false;

    @Override
    public void visit(DesignatorIncrement DesignatorIncrement) {
        Quadruple instruction = new Quadruple(IRInstruction.ADD);
        resolveIncDec(DesignatorIncrement.getDesignator().obj, instruction);
    }

    @Override
    public void visit(DesignatorDecrement DesignatorDecrement) {
        Quadruple instruction = new Quadruple(IRInstruction.SUB);
        resolveIncDec(DesignatorDecrement.getDesignator().obj, instruction);
    }

    //////////////////////////////////////////////////////////////////////////////////
    // METHOD
    //////////////////////////////////////////////////////////////////////////////////

    static {
        alwaysTrueConstant = new QuadrupleIntegerConst(1);
    }

    private Stack<ControlFlow.IfFixPoint> ifFixPointStack = new Stack<>();

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

    private Stack<ControlFlow.ForStatementFixPoint> jumpForFixPoints = new Stack<>();

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
        reverseParameterStack.peek().push(container);
        //expressionDAG = new ExpressionDAG();
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
        reverseParameterStack.push(new Stack<>());
    }

    @Override
    public void visit(ActParsEnd ActParsEnd) {
        Stack<ParameterContainer> container = reverseParameterStack.pop();

        while (!container.empty())
            code.addAll(container.pop().instructions);
    }

    private static class ParameterContainer {
        public List<Quadruple> instructions = new ArrayList<>();
    }

    @Override
    public void visit(FactorFunctionCall FactorFunctionCall) {
        if (FactorFunctionCall.getDesignator() instanceof DesignatorArrayAccess)
            return;

        Obj var = FactorFunctionCall.getDesignator().obj;
        if (FactorFunctionCall.getFactorFunctionCallParameters() instanceof NoFactorFunctionCallParameter) {
            // not a function call but variable access

            expressionNodeStack.push(expressionDAG.getOrCreateLeaf(var));
        }
        else {
            Quadruple instruction = new Quadruple(IRInstruction.CALL);
            instruction.setArg1(new QuadrupleObjVar(var));

            // function call with return value
            Struct returnType = FactorFunctionCall.getDesignator().obj.getType();
            Obj returnValue = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), returnType);

            expressionNodeStack.push(expressionDAG.getOrCreateLeaf(returnValue));
            instruction.setResult(new QuadrupleObjVar(returnValue));

            /*ExpressionNode putResultTo = expressionNodeStack.pop();
            instruction.setResult(new QuadrupleObjVar(putResultTo.getObj()));*/

            code.add(instruction);

            callWIthReturnValue = true;
        }
    }

    private int forVarDeclFix = 0;

    //////////////////////////////////////////////////////////////////////////////////
    // COMMON FOR DAGs
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(MakeNewExpressionDAG MakeNewExpressionDAG) {
        if (MakeNewExpressionDAG.getParent() instanceof ExprReturnStatement) {
            /* no need for action here */
        }
        else if (MakeNewExpressionDAG.getParent() instanceof PrintStatement) {
            /* no need for action here */
        }
        else if (MakeNewExpressionDAG.getParent() instanceof DesignatorAssign) {
            if (((DesignatorAssign) MakeNewExpressionDAG.getParent()).getDesignator() instanceof DesignatorArrayAccess)
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
            /* no need for action here */
        }
    }

    private boolean inForLogicalOrCondition = false;
    private Stack<List<Quadruple>> forUpdateVarListInstructionStack = new Stack<>();

    private boolean postponeUpdateVarList = false;

    private void generateLabelForContinueStatement(List<Quadruple> buffer) {
        // need to remember the name of label because of CONTINUE statement
        String updateVarListLabel = generateUniqueLabelName();
        generateLabel(buffer, updateVarListLabel);
        jumpForFixPoints.peek().updateVarLabel = updateVarListLabel;
    }

    @Override
    public void visit(DesignatorAssign DesignatorAssign) {
        ExpressionNode src;
        ExpressionNode dest;

        //System.out.println(expressionDAG);

        if (allocateArray) {
            allocateArray = false;

            expressionNodeStack.pop();
            dest = expressionNodeStack.pop();

            code.addAll(expressionDAG.emitQuadruples());

            Quadruple instruction = new Quadruple(IRInstruction.MALLOC);
            instruction.setArg1(new QuadrupleObjVar(expressionDAG.getRootObj()));
            instruction.setResult(new QuadrupleObjVar(dest.getVariable()));

            code.add(instruction);
        }
        else if (DesignatorAssign.getDesignator() instanceof DesignatorArrayAccess) {
            Quadruple instruction = new Quadruple(IRInstruction.ASTORE);

            code.addAll(expressionDAG.emitQuadruples());

            ExpressionNode rightSide = expressionNodeStack.pop();
            ExpressionNode indexer = expressionNodeStack.pop();
            ExpressionNode array = expressionNodeStack.pop();

            instruction.setArg1(new QuadrupleObjVar(rightSide.getObj()));
            instruction.setArg2(new QuadrupleObjVar(indexer.getObj()));
            instruction.setResult(new QuadrupleObjVar(array.getObj()));

            code.add(instruction);
        }
        else {
            src = expressionNodeStack.pop();
            dest = expressionNodeStack.pop();

            expressionDAG.getOrCreateNode(ExpressionNodeOperation.ASSIGNMENT, dest, src);

            List<Quadruple> toAdd = expressionDAG.emitQuadruples();

            if (postponeUpdateVarList) {
                generateLabelForContinueStatement(toAdd);

                toAdd.addAll(toAdd);
                forUpdateVarListInstructionStack.push(toAdd);
            }
            else
                code.addAll(toAdd);
        }

        expressionDAG = new ExpressionDAG();
    }

    private void resolveIncDec(Obj var, Quadruple instruction) {
        instruction.setArg1(new QuadrupleObjVar(var));
        instruction.setArg2(new QuadrupleIntegerConst(1));
        instruction.setResult(new QuadrupleObjVar(var));

        if (postponeUpdateVarList) {
            List<Quadruple> toAdd = new ArrayList<>();

            generateLabelForContinueStatement(toAdd);
            toAdd.add(instruction);
            forUpdateVarListInstructionStack.push(toAdd);
        }
        else
            code.add(instruction);
    }

    @Override
    public void visit(DesignatorInvoke DesignatorInvoke) {
        Obj methodToInvoke = DesignatorInvoke.getDesignator().obj;

        Quadruple instruction = new Quadruple(IRInstruction.CALL);
        instruction.setArg1(new QuadrupleObjVar(methodToInvoke));

        if (postponeUpdateVarList) {
            List<Quadruple> toAdd = new ArrayList<>();

            generateLabelForContinueStatement(toAdd);
            toAdd.add(instruction);
            forUpdateVarListInstructionStack.push(toAdd);
        }
        else
            code.add(instruction);
    }

    @Override
    public void visit(MethodName MethodName) {
        generateLabel(code, MethodName.obj.getName());

        Quadruple instruction = new Quadruple(IRInstruction.ENTER);
        int numberOfBytes = 0;
        int i = 0;

        // determine number of bytes to allocate
        for (Obj current : MethodName.obj.getLocalSymbols()) {
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

    //////////////////////////////////////////////////////////////////////////////////
    // IF STATEMENT
    //////////////////////////////////////////////////////////////////////////////////
    private int ifStatementDepth = 0;
    private List<ControlFlow.IfFixPoint> compilerAddedInstructionsToFix = new ArrayList<>();

    @Override
    public void visit(IfKeyword IfKeyword) {
        ifStatementDepth++;
    }

    @Override
    public void visit(IfCondition IfCondition) {
        List<ControlFlow.IfFixPoint> toRemove = new ArrayList<>();
        String labelName = null;

        /* this means that all condition instructions are emitted and
           in IF branch compiler entered, so we need to fix all the
           unconditional jumps generated by OR logic condition (COMPILER_ADDED)
         */
        for (ControlFlow.IfFixPoint p : compilerAddedInstructionsToFix) {
            if (p.statementDepth == ifStatementDepth && p.fixType == ControlFlow.IfFixPoint.FixType.COMPILER_ADDED) {
                if (labelName == null) {
                    // generate label -> FIRST quadruple in IF true branch
                    labelName = ControlFlow.generateUniqueLabelName();
                    generateLabel(code, labelName);
                }

                toRemove.add(p);
                p.quadruple.setResult(new QuadrupleLabel(labelName));
            }
        }

        ifFixPointStack.removeAll(toRemove);
    }


    @Override
    public void visit(CondFactUnary CondFactUnary) {
        code.addAll(expressionDAG.emitQuadruples());
        Obj arg1Obj = expressionNodeStack.pop().getObj();

        IRInstruction operationCode = IRInstruction.JNE;

        Quadruple instruction = new Quadruple(operationCode);
        instruction.setArg1(new QuadrupleObjVar(arg1Obj));
        instruction.setArg2(alwaysTrueConstant);
        code.add(instruction);

        if (!inForCondition)
            ifFixPointStack.push(new ControlFlow.IfFixPoint(instruction, ifStatementDepth));
        else
            jumpForFixPoints.peek().forEndFixPoint.add(instruction);
    }

    @Override
    public void visit(CondFactBinary CondFactBinary) {
        code.addAll(expressionDAG.emitQuadruples());

        IRInstruction operationCode = negateJumpInstruction(determineJumpInstruction(CondFactBinary.getRelop()));

        Obj arg2 = expressionNodeStack.pop().getObj();
        Obj arg1 = expressionNodeStack.pop().getObj();

        Quadruple instruction = new Quadruple(operationCode);
        instruction.setArg2(new QuadrupleObjVar(arg2));
        instruction.setArg1(new QuadrupleObjVar(arg1));
        code.add(instruction);

        if (!inForCondition)
            ifFixPointStack.push(new ControlFlow.IfFixPoint(instruction, ifStatementDepth));
        else
            jumpForFixPoints.peek().forEndFixPoint.add(instruction);
    }

    @Override
    public void visit(IfStatement IfStatement) {
        // generate label -> first quadruple after whole IF-THEN-ELSE structure
        String labelName = null;

        // backpatching
        while (!ifFixPointStack.empty() && ifFixPointStack.peek().statementDepth == ifStatementDepth) {
            if (labelName == null) {
                labelName = ControlFlow.generateUniqueLabelName();
                generateLabel(code, labelName);
            }

            ControlFlow.IfFixPoint fix = ifFixPointStack.pop();
            fix.quadruple.setResult(new QuadrupleLabel(labelName));
        }

        ifStatementDepth--;
    }


    @Override
    public void visit(ElseStatementKeyword ElseStatementKeyword) {
        // still in IF-true branch -> put JMP to skip ELSE instruction
        Quadruple unconditionalJump = new Quadruple(IRInstruction.JMP);
        code.add(unconditionalJump);

        // generate label for beginning of else branch
        String labelName = null;

        // backpatch all conditionals in if-true branch
        while (!ifFixPointStack.empty() && ifFixPointStack.peek().statementDepth == ifStatementDepth) {
            if (labelName == null) {
                labelName = ControlFlow.generateUniqueLabelName();
                generateLabel(code, labelName);
            }

            ControlFlow.IfFixPoint fix = ifFixPointStack.pop();
            fix.quadruple.setResult(new QuadrupleLabel(labelName));
        }

        // schedule unconditional jump to be backpatched upon finishing current if-then-else structure
        ifFixPointStack.push(new ControlFlow.IfFixPoint(unconditionalJump, ifStatementDepth));
    }

    @Override
    public void visit(LogicalOrCondition LogicalOrCondition) {
        // emit JMP after Jxx is emitted for OR condition
        Quadruple unconditionalJump = new Quadruple(IRInstruction.JMP);
        code.add(unconditionalJump);

        if (inForLogicalOrCondition)
            jumpForFixPoints.peek().unconditionalJumpForOrCondition.add(unconditionalJump);

        String labelName = ControlFlow.generateUniqueLabelName();
        generateLabel(code, labelName);

        if (inForLogicalOrCondition) {
            int index = jumpForFixPoints.peek().forEndFixPoint.size() - 1;
            Quadruple q = jumpForFixPoints.peek().forEndFixPoint.get(index);
            q.setResult(new QuadrupleLabel(labelName));

            jumpForFixPoints.peek().forEndFixPoint.remove(q);
        }

        while (!ifFixPointStack.empty() && ifFixPointStack.peek().statementDepth == ifStatementDepth) {
            ControlFlow.IfFixPoint fix = ifFixPointStack.pop();
            fix.quadruple.setResult(new QuadrupleLabel(labelName));
        }

        compilerAddedInstructionsToFix.add(new ControlFlow.IfFixPoint(unconditionalJump, ifStatementDepth, ControlFlow.IfFixPoint.FixType.COMPILER_ADDED));
    }

    //////////////////////////////////////////////////////////////////////////////////
    // FOR STATEMENT
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ForVarDeclEnd ForVarDeclEnd) {
        // creates new FOR statement
        jumpForFixPoints.push(new ControlFlow.ForStatementFixPoint());

        // FOR var init list had already been emitted, so emit label to code
        String label = generateLabel(code);
        jumpForFixPoints.peek().conditionLabel = label;

        inForLogicalOrCondition = true;
    }

    @Override
    public void visit(SingleForLoopCondition SingleForLoopCondition) {
        inForCondition = false;
    }

    @Override
    public void visit(NoForLoopCondition NoForLoopCondition) {
        inForCondition = false;
    }

    @Override
    public void visit(ForLoopConditionEnd ForLoopConditionEnd) {
        // flag to postpone emission of update var instructions until the end of FOR statement
        postponeUpdateVarList = true;
        inForLogicalOrCondition = false;
    }

    private void forUpdate() {
        // fixing logical or inside for statement
        List<Quadruple> fixes = jumpForFixPoints.peek().unconditionalJumpForOrCondition;

        if (fixes.size() != 0) {
            String labelName = null;

            for (Quadruple q : fixes) {
                if (labelName == null) {
                    labelName = ControlFlow.generateUniqueLabelName();
                    generateLabel(code, labelName);
                }

                q.setResult(new QuadrupleLabel(labelName));
            }

            fixes.clear();
        }
    }

    @Override
    public void visit(SingleForUpdateVarList SingleForUpdateVarList) {
        forUpdate();
        postponeUpdateVarList = false;
    }

    @Override
    public void visit(NoForUpdateVarList NoForUpdateVarList) {
        forUpdate();
        postponeUpdateVarList = false;
    }

    @Override
    public void visit(EndOfForStatement EndOfForStatement) {
        ControlFlow.ForStatementFixPoint fixPoint = jumpForFixPoints.pop();

        // emit all postponed update var list statements if there is come quadruple
        if (!forUpdateVarListInstructionStack.empty())
            code.addAll(forUpdateVarListInstructionStack.pop());

        // add JMP instruction on end of whole FOR statement
        Quadruple instruction = new Quadruple(IRInstruction.JMP);
        instruction.setResult(new QuadrupleLabel(fixPoint.conditionLabel));
        code.add(instruction);

        // generate first instruction after FOR statement -> label
        String labelName = generateLabel(code);

        // resolve BREAK statement
        for (Quadruple fix : fixPoint.breakStatements)
            fix.setResult(new QuadrupleLabel(labelName));

        // resolve all FOR loop conditionals
        for (Quadruple fix : fixPoint.forEndFixPoint)
            fix.setResult(new QuadrupleLabel(labelName));
    }

    @Override
    public void visit(StartForCondition StartForCondition) {
        inForCondition = true;
    }

    @Override
    public void visit(BreakStatement BreakStatement) {
        if (!jumpForFixPoints.empty()) {
            Quadruple instruction = new Quadruple(IRInstruction.JMP);
            jumpForFixPoints.peek().breakStatements.add(instruction);

            code.add(instruction);
        }
    }

    @Override
    public void visit(ContinueStatement ContinueStatement) {
        if (!jumpForFixPoints.empty()) {
            Quadruple instruction = new Quadruple(IRInstruction.JMP);
            instruction.setResult(new QuadrupleLabel(jumpForFixPoints.peek().updateVarLabel));

            code.add(instruction);
        }
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
            if (instruction.getInstruction() == LEAVE)
                builder.append("\n");

            builder.append("\n");
        }

        return builder.toString();
    }
}
