package cvetkovic.ir;

import cvetkovic.ir.expression.ExpressionDAG;
import cvetkovic.ir.expression.ExpressionNode;
import cvetkovic.ir.expression.ExpressionNodeOperation;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.arguments.*;
import cvetkovic.parser.ast.*;
import cvetkovic.structures.SymbolTable;
import cvetkovic.x64.SystemV_ABI;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static cvetkovic.ir.ControlFlow.generateLabel;
import static cvetkovic.ir.ControlFlow.generateUniqueLabelName;
import static cvetkovic.ir.IRInstruction.*;

public class IRCodeGenerator extends VisitorAdaptor {

    //////////////////////////////////////////////////////////////////////////////////
    // LOCAL VARIABLES
    //////////////////////////////////////////////////////////////////////////////////

    private static final QuadrupleIntegerConst alwaysTrueConstant;

    private Stack<ControlFlow.IfFixPoint> ifFixPointStack = new Stack<>();
    private int ifStatementDepth = 0;
    private List<ControlFlow.IfFixPoint> compilerAddedInstructionsToFix = new ArrayList<>();

    private boolean inForCondition = false;

    private List<Quadruple> code;
    private List<List<Quadruple>> outputCode = new ArrayList<>();

    private ExpressionDAG expressionDAG;
    private Stack<ExpressionNode> expressionNodeStack = new Stack<>();

    private Stack<List<ParameterContainer>> reverseParameterStack = new Stack<>();

    private Stack<ControlFlow.ForStatementFixPoint> jumpForFixPoints = new Stack<>();

    private boolean allocateArray = false;
    private boolean allocateClass = false;
    private boolean storeToPtr = false;

    private boolean inForLogicalOrCondition = false;
    private Stack<List<Quadruple>> forUpdateVarListInstructionStack = new Stack<>();

    private boolean postponeUpdateVarList = false;

    // NOTE: if function contains multiple return statements then they all need to point to unique leave instruction
    private List<Quadruple> returnStatementJMPFixPoint = new ArrayList<>();

    private Obj currentMethod = null;
    private boolean cancelFactorFunctionCall = false;

    private List<Obj> functionsObj = new ArrayList<>();

    //////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR & STATIC INITIALIZATION
    //////////////////////////////////////////////////////////////////////////////////

    static {
        alwaysTrueConstant = new QuadrupleIntegerConst(1);
    }

    public IRCodeGenerator() {
        expressionDAG = new ExpressionDAG();
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

        cancelFactorFunctionCall = false;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(FactorNumericalConst FactorNumericalConst) {
        Obj constValue = new Obj(Obj.Con, "", SymbolTable.intType);
        constValue.setAdr(FactorNumericalConst.getFactorNumConst());

        expressionNodeStack.push(expressionDAG.getOrCreateLeaf(constValue));
    }

    @Override
    public void visit(FactorCharConst FactorCharConst) {
        Obj constValue = new Obj(Obj.Con, "", SymbolTable.charType);
        constValue.setAdr(FactorCharConst.getFactorChar());

        expressionNodeStack.push(expressionDAG.getOrCreateLeaf(constValue));
    }

    @Override
    public void visit(FactorBoolConst FactorBoolConst) {
        Obj constValue = new Obj(Obj.Con, "", SymbolTable.BooleanStruct);
        constValue.setAdr(FactorBoolConst.getFactorBoolean() ? 1 : 0);

        expressionNodeStack.push(expressionDAG.getOrCreateLeaf(constValue));
    }

    //////////////////////////////////////////////////////////////////////////////////
    // DESIGNATOR
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(DesignatorIncrement DesignatorIncrement) {
        Quadruple instruction = new Quadruple(IRInstruction.ADD);

        Obj ptrDest = expressionNodeStack.pop().getObj();
        if (expressionNodeStack.empty())    // not a PTR access
            resolveIncDec(ptrDest, instruction, null);
        else
            resolveIncDec(expressionNodeStack.pop().getObj(), instruction, ptrDest);
    }

    @Override
    public void visit(DesignatorDecrement DesignatorDecrement) {
        Quadruple instruction = new Quadruple(IRInstruction.SUB);

        Obj ptrDest = expressionNodeStack.pop().getObj();
        if (expressionNodeStack.empty())    // not a PTR access
            resolveIncDec(ptrDest, instruction, null);
        else
            resolveIncDec(expressionNodeStack.pop().getObj(), instruction, ptrDest);
    }

    @Override
    public void visit(DesignatorArrayAccess DesignatorArrayAccess) {
        ExpressionNode rightChild = expressionNodeStack.pop();
        ExpressionNode leftChild;
        if (expressionNodeStack.size() > 0 &&
                expressionNodeStack.peek().getObj() != null &&
                expressionNodeStack.peek().getObj().getType() == SymbolTable.classType) {
            Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), SymbolTable.classType, true);

            Quadruple load = new Quadruple(LOAD);
            load.setArg1(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));
            load.setResult(new QuadrupleObjVar(tmp));
            code.add(load);

            leftChild = new ExpressionNode(tmp);
            //expressionNodeStack.push(leftChild);
        } else
            leftChild = expressionDAG.getOrCreateLeaf(DesignatorArrayAccess.obj);

        if (!(DesignatorArrayAccess.getParent() instanceof DesignatorAssign) &&
                !(DesignatorArrayAccess.getParent() instanceof ReadStatement))
            expressionNodeStack.push(expressionDAG.getOrCreateNode(ExpressionNodeOperation.ARRAY_LOAD, leftChild, rightChild));
        else {
            // this means that array access is on the left side of '=' operator
            if (!(DesignatorArrayAccess.getDesignator() instanceof DesignatorNonArrayAccess))
                expressionNodeStack.push(leftChild);
            expressionNodeStack.push(rightChild);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // METHOD
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(MethodDecl MethodDecl) {
        if (returnStatementJMPFixPoint.size() != 0) {
            String leavePoint = generateLabel(code);

            for (Quadruple q : returnStatementJMPFixPoint)
                q.setResult(new QuadrupleLabel(leavePoint));

            returnStatementJMPFixPoint.clear();
        }

        // no more new basic block creation
        if (code.get(code.size() - 1).getInstruction() != LEAVE) {
            Quadruple instruction = new Quadruple(IRInstruction.LEAVE);
            code.add(instruction);
        }

        if (code != null) {
            outputCode.add(code);
            functionsObj.add(MethodDecl.obj);
            code = new ArrayList<>();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // ARRAYS
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(FactorArrayDeclaration FactorArrayDeclaration) {
        // 'malloc' will be done in 'Designator' visitor
        if (FactorArrayDeclaration.getFactorArrayDecl() instanceof NoArrayDeclaration) {
            allocateClass = true;

            String dataTypeName = ((DataType) FactorArrayDeclaration.getType()).getTypeIdent();
            Obj objNode = SymbolTable.find(dataTypeName);

            expressionNodeStack.push(new ExpressionNode(objNode));

            if (code.size() > 0 && code.get(code.size() - 1).getInstruction() == GET_PTR)
                storeToPtr = true;
        } else {
            allocateArray = true;

            Obj result = new Obj(Obj.Con, "size", SymbolTable.intType);
            result.setAdr(SystemV_ABI.getX64VariableSize(FactorArrayDeclaration.struct));
            //expressionNodeStack.push(new ExpressionNode(result));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // INPUT / OUTPUT
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ReadStatement ReadStatement) {
        Quadruple instruction = new Quadruple(IRInstruction.SCANF);

        Obj targetObj = ReadStatement.getDesignator().obj;
        Struct type = targetObj.getType().getElemType() == null ? targetObj.getType() : targetObj.getType().getElemType();

        if (type.getKind() == Struct.Int)
            instruction.setArg2(new QuadrupleIODataWidth(QuadrupleIODataWidth.DataWidth.WORD));
        else if (type.getKind() == Struct.Char)
            instruction.setArg2(new QuadrupleIODataWidth(QuadrupleIODataWidth.DataWidth.BYTE));
        else if (type.getKind() == Struct.Bool)
            instruction.setArg2(new QuadrupleIODataWidth(QuadrupleIODataWidth.DataWidth.BIT));
        else
            throw new RuntimeException("IR instruction 'scanf' is not supported with other data types than integer, character or boolean.");

        if (ReadStatement.getDesignator() instanceof DesignatorArrayAccess) {
            Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), ((QuadrupleIODataWidth) instruction.getArg2()).ioVarToStruct(), true);
            instruction.setResult(new QuadrupleObjVar(tmp));

            Quadruple astoreInstruction = new Quadruple(ASTORE);
            astoreInstruction.setArg1(new QuadrupleObjVar(tmp));
            astoreInstruction.setArg2(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));
            astoreInstruction.setResult(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));

            code.add(instruction);
            code.add(astoreInstruction);
        } else if (ReadStatement.getDesignator() instanceof DesignatorNonArrayAccess || expressionNodeStack.size() > 1) {
            Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), ((QuadrupleIODataWidth) instruction.getArg2()).ioVarToStruct(), true);
            instruction.setResult(new QuadrupleObjVar(tmp));

            if (expressionNodeStack.size() > 1)
                expressionNodeStack.pop();

            Quadruple astoreInstruction = new Quadruple(STORE);
            astoreInstruction.setArg1(new QuadrupleObjVar(tmp));
            astoreInstruction.setArg2(new QuadruplePTR());
            astoreInstruction.setResult(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));

            code.add(instruction);
            code.add(astoreInstruction);
        } else {
            instruction.setResult(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));
            code.add(instruction);
        }
    }

    @Override
    public void visit(PrintStatement PrintStatement) {
        code.addAll(expressionDAG.emitQuadruples());

        Quadruple instruction = new Quadruple(IRInstruction.PRINTF);

        Struct targetStruct = (PrintStatement.getExpr().struct.getElemType() == null ? PrintStatement.getExpr().struct : PrintStatement.getExpr().struct.getElemType());
        if (targetStruct.getKind() == Struct.Int)
            instruction.setArg1(new QuadrupleIODataWidth(QuadrupleIODataWidth.DataWidth.WORD));
        else if (targetStruct.getKind() == Struct.Char)
            instruction.setArg1(new QuadrupleIODataWidth(QuadrupleIODataWidth.DataWidth.BYTE));
        else if (targetStruct.getKind() == Struct.Bool)
            instruction.setArg1(new QuadrupleIODataWidth(QuadrupleIODataWidth.DataWidth.BIT));
        else
            throw new RuntimeException("IR instruction 'printf' is not supported with other data types than integer, character or boolean.");

        instruction.setArg2(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));

        code.add(instruction);

        expressionDAG = new ExpressionDAG();
    }

    //////////////////////////////////////////////////////////////////////////////////
    // ACTUAL PARAMETERS
    //////////////////////////////////////////////////////////////////////////////////

    private void resolveActualParameters() {
        ParameterContainer container = new ParameterContainer();

        code.addAll(expressionDAG.emitQuadruples());
        //container.instructions.addAll(expressionDAG.emitQuadruples());

        Quadruple instruction = new Quadruple(IRInstruction.PARAM);
        instruction.setArg1(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));

        container.instructions.add(instruction);
        reverseParameterStack.peek().add(container);
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
        reverseParameterStack.push(new Stack<>());
    }

    private void pushImplicitThisForFunctionCall() {
        // for case -> shapes[i].toString();
        if (expressionDAG.getLast() != null && expressionDAG.getLast().getOperation() == ExpressionNodeOperation.ARRAY_LOAD) {
            code.addAll(expressionDAG.emitQuadruples());
            expressionDAG = new ExpressionDAG();
        }

        // pushing implicit 'this'
        if (!expressionNodeStack.empty()) {
            Quadruple implicitThis = new Quadruple(PARAM);
            implicitThis.setArg1(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));

            //reverseParameterStack.peek().get(0).instructions.add(implicitThis);
            code.add(implicitThis);
        }
    }

    private void endFunctionCall() {
        if (!reverseParameterStack.empty()) {
            List<ParameterContainer> container = reverseParameterStack.pop();

            container.forEach(p -> code.addAll(p.instructions));
        }
    }

    public List<Obj> getFunctionsObj() {
        return functionsObj;
    }

    private static class ParameterContainer {
        public List<Quadruple> instructions = new ArrayList<>();
    }

    @Override
    public void visit(FactorFunctionCall FactorFunctionCall) {
        if (FactorFunctionCall.getDesignator() instanceof DesignatorArrayAccess)
            return;
        else if (FactorFunctionCall.getDesignator() instanceof DesignatorNonArrayAccess && insideClass)
            return;
        else if (cancelFactorFunctionCall) {
            Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), FactorFunctionCall.struct, true);

            Quadruple load = new Quadruple(LOAD);
            load.setArg1(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));
            load.setResult(new QuadrupleObjVar(tmp));
            code.add(load);

            expressionNodeStack.push(new ExpressionNode(tmp));

            cancelFactorFunctionCall = false;
            storeToPtr = false;
            return;
        }

        Obj var = FactorFunctionCall.getDesignator().obj;
        if (FactorFunctionCall.getFactorFunctionCallParameters() instanceof NoFactorFunctionCallParameter) {
            // not a function call but variable access
            if (FactorFunctionCall.getDesignator() instanceof DesignatorNonArrayAccess && !insideClass)
                return;

            expressionNodeStack.push(expressionDAG.getOrCreateLeaf(var));
        } else {
            if (!(FactorFunctionCall.getDesignator() instanceof DesignatorRoot))
                pushImplicitThisForFunctionCall();
            endFunctionCall();

            boolean invokeVirtual = false;
            if (FactorFunctionCall.getDesignator().obj.getLocalSymbols().stream().filter(p -> p.getName().equals("this")).count() > 0)
                invokeVirtual = true;

            Quadruple instruction = new Quadruple(!invokeVirtual ? IRInstruction.CALL : INVOKE_VIRTUAL);
            instruction.setArg1(new QuadrupleObjVar(var));

            // generate temp variable for storing the result of a CALL
            Struct returnType = FactorFunctionCall.getDesignator().obj.getType();
            Obj returnValue = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), returnType, true);

            expressionNodeStack.push(expressionDAG.getOrCreateLeaf(returnValue));
            instruction.setResult(new QuadrupleObjVar(returnValue));

            code.add(instruction);
        }
    }

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

        if (allocateArray) {
            allocateArray = false;

            src = expressionNodeStack.pop();
            //expressionNodeStack.pop();
            dest = expressionNodeStack.pop();

            code.addAll(expressionDAG.emitQuadruples());

            Quadruple instruction = new Quadruple(IRInstruction.MALLOC);
            instruction.setArg1(new QuadrupleIntegerConst(src.getObj().getAdr()));
            instruction.setArg2(new QuadrupleARR());
            if (dest.getVariable().getType().getKind() == Struct.Class) {
                // need to store via STORE PTR
                Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), SymbolTable.classType, true);
                instruction.setResult(new QuadrupleObjVar(tmp));

                Quadruple storePtrInstruction = new Quadruple(STORE);
                storePtrInstruction.setArg1(new QuadrupleObjVar(tmp));
                storePtrInstruction.setArg2(new QuadruplePTR());
                storePtrInstruction.setResult(new QuadrupleObjVar(dest.getVariable()));

                code.add(instruction);
                code.add(storePtrInstruction);
            } else {
                instruction.setResult(new QuadrupleObjVar(dest.getVariable()));
                code.add(instruction);
            }
        } else if (allocateClass) {
            allocateClass = false;

            src = expressionNodeStack.pop();
            dest = expressionNodeStack.pop();

            Quadruple instruction = new Quadruple(IRInstruction.MALLOC);
            instruction.setArg1(new QuadrupleObjVar(src.getObj()));

            Quadruple astore = null;

            if (DesignatorAssign.getDesignator() instanceof DesignatorArrayAccess) {
                Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), SymbolTable.classType, true);

                //instruction.setArg2(new QuadruplePTR());
                instruction.setResult(new QuadrupleObjVar(tmp));

                astore = new Quadruple(ASTORE);
                astore.setArg1(new QuadrupleObjVar(tmp));
                astore.setArg2(new QuadrupleObjVar(dest.getObj()));
                astore.setResult(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));
            } else
                instruction.setResult(new QuadrupleObjVar(dest.getVariable()));

            // store to ptr
            if (storeToPtr) {
                instruction.setArg2(new QuadruplePTR());
                storeToPtr = false;
            }

            code.add(instruction);
            if (astore != null)
                code.add(astore);
        } else if (DesignatorAssign.getDesignator() instanceof DesignatorArrayAccess) {
            Quadruple instruction = new Quadruple(IRInstruction.ASTORE);

            code.addAll(expressionDAG.emitQuadruples());

            ExpressionNode rightSide = expressionNodeStack.pop();
            ExpressionNode indexer = expressionNodeStack.pop();
            ExpressionNode array = expressionNodeStack.pop();

            instruction.setArg1(new QuadrupleObjVar(rightSide.getObj()));
            instruction.setArg2(new QuadrupleObjVar(indexer.getObj()));
            instruction.setResult(new QuadrupleObjVar(array.getObj()));

            code.add(instruction);
        } else {
            src = expressionNodeStack.pop();
            if (expressionNodeStack.size() > 1) {
                expressionNodeStack.pop();
                storeToPtr = true;
            } else if (expressionNodeStack.peek().getObj().getType() == SymbolTable.classType) {
                storeToPtr = true;
            }
            dest = expressionNodeStack.pop();

            expressionDAG.getOrCreateNode(ExpressionNodeOperation.ASSIGNMENT, dest, src);
            List<Quadruple> toAdd = expressionDAG.emitQuadruples();

            // store to ptr
            if (storeToPtr) {
                toAdd.get(toAdd.size() - 1).setArg2(new QuadruplePTR());
                storeToPtr = false;
            }

            if (postponeUpdateVarList) {
                generateLabelForContinueStatement(toAdd);

                toAdd.addAll(toAdd);
                forUpdateVarListInstructionStack.push(toAdd);
            } else
                code.addAll(toAdd);
        }

        expressionDAG = new ExpressionDAG();
    }

    private void resolveIncDec(Obj var, Quadruple instruction, Obj ptrDestination) {
        Obj constValue = new Obj(Obj.Con, "", SymbolTable.intType);
        constValue.setAdr(1);

        instruction.setArg1(new QuadrupleObjVar(var));
        instruction.setArg2(new QuadrupleObjVar(constValue));

        Quadruple storeInstruction = null;
        if (storeToPtr) {
            storeInstruction = new Quadruple(STORE);
            storeInstruction.setArg2(new QuadruplePTR());
            storeInstruction.setResult(new QuadrupleObjVar(ptrDestination));

            Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), SymbolTable.intType, true);
            instruction.setResult(new QuadrupleObjVar(tmp));
            storeInstruction.setArg1(new QuadrupleObjVar(tmp));

            storeToPtr = false;
        } else
            instruction.setResult(new QuadrupleObjVar(var));

        if (postponeUpdateVarList) {
            List<Quadruple> toAdd = new ArrayList<>();

            generateLabelForContinueStatement(toAdd);
            toAdd.add(instruction);
            if (storeInstruction != null)
                toAdd.add(storeInstruction);
            forUpdateVarListInstructionStack.push(toAdd);
        } else {
            code.add(instruction);
            if (storeInstruction != null)
                code.add(storeInstruction);
        }
    }

    @Override
    public void visit(DesignatorInvoke DesignatorInvoke) {
        Obj methodToInvoke = DesignatorInvoke.getDesignator().obj;

        if (!(DesignatorInvoke.getDesignator() instanceof DesignatorRoot))
            pushImplicitThisForFunctionCall();
        endFunctionCall();

        Obj var = (expressionNodeStack.empty() ? methodToInvoke : expressionNodeStack.pop().getObj());

        boolean invokeVirtual = false;
        if (var.getLocalSymbols().stream().filter(p -> p.getName().equals("this")).count() > 0)
            invokeVirtual = true;

        Quadruple instruction = new Quadruple(!invokeVirtual ? IRInstruction.CALL : INVOKE_VIRTUAL);
        instruction.setArg1(new QuadrupleObjVar(var));

        if (postponeUpdateVarList) {
            List<Quadruple> toAdd = new ArrayList<>();

            generateLabelForContinueStatement(toAdd);
            toAdd.add(instruction);
            forUpdateVarListInstructionStack.push(toAdd);
        } else
            code.add(instruction);
    }

    @Override
    public void visit(AbstractMethodName AbstractMethodName) {
        // do nothing here
    }

    @Override
    public void visit(AbstractMethodDecl AbstractMethodDecl) {
        // do nothing here
    }

    @Override
    public void visit(MethodName MethodName) {
        code = new ArrayList<>();
        expressionNodeStack = new Stack<>();
        expressionDAG = new ExpressionDAG();

        generateLabel(code, MethodName.obj.getName());
        currentMethod = MethodName.obj;

        Quadruple instruction = new Quadruple(IRInstruction.ENTER);
        int i = 0;

        // determine number of bytes to allocate
        int max = 0;
        for (Obj current : MethodName.obj.getLocalSymbols()) {
            // skip all parameters because space only for local variables shall be allocated
            if (i >= MethodName.obj.getLevel()) {
                Struct type = current.getType();
                if (current.getAdr() > max)
                    max = current.getAdr();
            }

            i++;
        }

        instruction.setArg1(new QuadrupleIntegerConst(max));

        code.add(instruction);
    }

    @Override
    public void visit(TermSingle TermSingle) {
        cancelFactorFunctionCall = false;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // IF STATEMENT
    //////////////////////////////////////////////////////////////////////////////////

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

        compilerAddedInstructionsToFix.removeAll(toRemove);
        ifFixPointStack.removeAll(toRemove);
    }


    @Override
    public void visit(CondFactUnary CondFactUnary) {
        code.addAll(expressionDAG.emitQuadruples());
        Obj arg1Obj = expressionNodeStack.pop().getObj();

        IRInstruction operationCode = IRInstruction.JNE;

        Quadruple instruction = new Quadruple(operationCode);
        instruction.setArg1(new QuadrupleObjVar(arg1Obj));

        Obj alwaysTrue = new Obj(Obj.Con, "const", arg1Obj.getType());
        alwaysTrue.setAdr(1);
        instruction.setArg2(new QuadrupleObjVar(alwaysTrue));

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

        expressionDAG = new ExpressionDAG();
    }

    @Override
    public void visit(IfStatement IfStatement) {
        // generate label -> first quadruple after whole IF-THEN-ELSE structure
        String labelName = null;

        Quadruple jumpAfterElse = null;
        if (code.get(code.size() - 1).getInstruction() != JMP) {
            jumpAfterElse = new Quadruple(JMP);
            code.add(jumpAfterElse);
        }

        // backpatching
        while (!ifFixPointStack.empty() && ifFixPointStack.peek().statementDepth == ifStatementDepth) {
            if (labelName == null) {
                labelName = ControlFlow.generateUniqueLabelName();
                generateLabel(code, labelName);
            }

            ControlFlow.IfFixPoint fix = ifFixPointStack.pop();
            fix.quadruple.setResult(new QuadrupleLabel(labelName));

            if (jumpAfterElse != null)
                jumpAfterElse.setResult(new QuadrupleLabel(labelName));
        }

        ifStatementDepth--;

        if (labelName == null && jumpAfterElse != null) {
            labelName = ControlFlow.generateUniqueLabelName();
            generateLabel(code, labelName);
            jumpAfterElse.setResult(new QuadrupleLabel(labelName));
        }
    }

    @Override
    public void visit(ElseStatementKeyword ElseStatementKeyword) {
        boolean waiveFixPointPush = true;
        // still in IF-true branch -> put JMP to skip ELSE instruction
        Quadruple unconditionalJump = new Quadruple(IRInstruction.JMP);
        if (code.get(code.size() - 1).getInstruction() != JMP) {
            code.add(unconditionalJump);
            waiveFixPointPush = false;
        }

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
        if (!waiveFixPointPush)
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
        String updateVarListLabel = null;

        // emit all postponed update var list statements if there is come quadruple
        if (!forUpdateVarListInstructionStack.empty()) {
            List<Quadruple> list = forUpdateVarListInstructionStack.pop();
            updateVarListLabel = list.get(0).getArg1().toString();

            Quadruple jumpToUpdateVarList = new Quadruple(JMP);
            jumpToUpdateVarList.setResult(new QuadrupleLabel(updateVarListLabel));
            code.add(jumpToUpdateVarList);

            // add update var list instructions
            code.addAll(list);
        }

        // add JMP instruction on end of whole FOR statement
        Quadruple instruction = new Quadruple(IRInstruction.JMP);
        instruction.setResult(new QuadrupleLabel(fixPoint.conditionLabel));
        code.add(instruction);

        // generate first instruction after FOR statement -> label
        String labelName = generateLabel(code);

        // resolve BREAK statements
        for (Quadruple fix : fixPoint.breakStatements)
            fix.setResult(new QuadrupleLabel(labelName));

        // resolve CONTINUE statements
        // NOTE: update var list could be null so jump to conditionLabel right away
        if (updateVarListLabel == null)
            updateVarListLabel = fixPoint.conditionLabel;
        for (Quadruple fix : fixPoint.continueStatements)
            fix.setResult(new QuadrupleLabel(updateVarListLabel));

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
            jumpForFixPoints.peek().continueStatements.add(instruction);

            code.add(instruction);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // DESIGNATOR
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(DesignatorRoot DesignatorRoot) {
        SyntaxNode parent = DesignatorRoot.getParent();
        cancelFactorFunctionCall = false;

        // adding implicit this
        if (insideClass) {
            Obj obj = DesignatorRoot.obj;

            // maybe put this.method() invocation -> for regular and abstract methods
            if (obj.getKind() == Obj.Fld || obj.getKind() == Obj.Meth || obj.getKind() == SymbolTable.AbstractMethodObject) {
                Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), SymbolTable.classType, true);

                Obj thisPointer = null;
                for (Obj ptr : currentMethod.getLocalSymbols()) {
                    if (ptr.getName().equals("this")) {
                        thisPointer = ptr;
                        break;
                    }
                }
                if (thisPointer == null)
                    throw new RuntimeException("Invalid function declaration inside class. No 'this' implicit parameter found.");

                boolean exitMethod = false;
                boolean cancelPush = false;

                if (!(obj.getKind() == Obj.Meth || obj.getKind() == SymbolTable.AbstractMethodObject)) {
                    Quadruple getPtr = new Quadruple(GET_PTR);
                    getPtr.setArg1(new QuadrupleObjVar(thisPointer));
                    getPtr.setArg2(new QuadrupleObjVar(obj));
                    getPtr.setResult(new QuadrupleObjVar(tmp));

                    // passing the size of the field
                    tmp.getType().setElementType(obj.getType());

                    storeToPtr = true;

                    code.add(getPtr);
                    exitMethod = true;
                } else {
                    Quadruple thisPtr = new Quadruple(PARAM);
                    thisPtr.setArg1(new QuadrupleObjVar(thisPointer));
                    code.add(thisPtr);
                    //cancelFactorFunctionCall = false;
                    cancelPush = true;
                }

                if (!cancelPush)
                    expressionNodeStack.push(new ExpressionNode(tmp));

                if (!(obj.getKind() == Obj.Meth || obj.getKind() == SymbolTable.AbstractMethodObject))
                    cancelFactorFunctionCall = true;

                if (exitMethod)
                    return;
            }
        }

        if (parent instanceof DesignatorAssign && ((DesignatorAssign) parent).getDesignator() instanceof DesignatorArrayAccess)
            return;
        else if (parent instanceof ExprReturnStatement)
            return;
        else if (parent instanceof ActParsSingle)
            return;
        else if (parent instanceof ActParsMultiple)
            return;
        else if (parent instanceof CondFactUnary)
            return;
        else if (parent instanceof CondFactBinary)
            return;
        else if (parent instanceof ArrayDeclaration)
            return;
        else if (parent instanceof DesignatorArrayAccess)
            return;
        else if (parent instanceof FactorFunctionCall)// && !DesignatorRoot.obj.getName().equals("this") && DesignatorRoot.obj.getType().getKind() != Obj.Fld)
            return;

        Obj destination = DesignatorRoot.obj;
        expressionNodeStack.push(expressionDAG.getOrCreateLeaf(DesignatorRoot.obj));
    }

    @Override
    public void visit(DesignatorNonArrayAccess DesignatorNonArrayAccess) {
        if ((DesignatorNonArrayAccess.obj.getKind() == Obj.Meth ||
                DesignatorNonArrayAccess.obj.getKind() == SymbolTable.AbstractMethodObject) && !cancelFactorFunctionCall)
            return;
        else if (cancelFactorFunctionCall) {
            Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), SymbolTable.classType, true);

            Quadruple load = new Quadruple(LOAD);
            load.setArg1(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));
            load.setResult(new QuadrupleObjVar(tmp));
            code.add(load);

            expressionNodeStack.push(new ExpressionNode(tmp));

            cancelFactorFunctionCall = false;
            storeToPtr = false;
            return;
        }

        Quadruple getPtr = new Quadruple(GET_PTR);
        if (expressionDAG.getLast() != null && expressionDAG.getLast().getOperation() == ExpressionNodeOperation.ARRAY_LOAD) {
            code.addAll(expressionDAG.emitQuadruples());
            expressionDAG = new ExpressionDAG();
        }

        Obj tmp = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), SymbolTable.classType, true);
        Obj tmp2 = null;

        getPtr.setArg1(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));
        getPtr.setArg2(new QuadrupleObjVar(DesignatorNonArrayAccess.obj));
        getPtr.setResult(new QuadrupleObjVar(tmp));

        // passing the size of the field
        tmp.getType().setElementType(DesignatorNonArrayAccess.obj.getType());

        Quadruple load = null;

        if (DesignatorNonArrayAccess.getParent() instanceof ReadStatement) {
            expressionNodeStack.push(new ExpressionNode(tmp));
            code.add(getPtr);

            return;
        }

        // if not a left side of designator assign statement
        if (!(DesignatorNonArrayAccess.getParent() instanceof DesignatorAssign) &&
                !(DesignatorNonArrayAccess.getParent() instanceof DesignatorInvoke) &&
                !(DesignatorNonArrayAccess.obj.getType().getKind() == Struct.Array)) {

            tmp2 = new Obj(Obj.Var, ExpressionDAG.generateTempVarOutside(), SymbolTable.intType, true);

            load = new Quadruple(LOAD);
            load.setArg1(new QuadrupleObjVar(tmp));
            load.setResult(new QuadrupleObjVar(tmp2));

            expressionNodeStack.push(new ExpressionNode(tmp2));
        }

        // STORE PTR for designator increment and decrement
        if (DesignatorNonArrayAccess.getParent() instanceof DesignatorIncrement ||
                DesignatorNonArrayAccess.getParent() instanceof DesignatorDecrement) {
            storeToPtr = true;
            expressionNodeStack.push(new ExpressionNode(tmp));
        }

        code.add(getPtr);
        if (load != null)
            code.add(load);
        else {
            expressionNodeStack.push(new ExpressionNode(tmp));
            storeToPtr = true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // RETURN STATEMENT
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(BlankReturnStatement BlankReturnStatement) {
        Quadruple instruction = new Quadruple(IRInstruction.JMP);
        code.add(instruction);

        returnStatementJMPFixPoint.add(instruction);
    }

    @Override
    public void visit(ExprReturnStatement ExprReturnStatement) {
        Quadruple instruction = new Quadruple(IRInstruction.RETURN);

        code.addAll(expressionDAG.emitQuadruples());
        instruction.setArg1(new QuadrupleObjVar(expressionNodeStack.pop().getObj()));

        code.add(instruction);

        expressionDAG = new ExpressionDAG();

        Quadruple jmp = new Quadruple(IRInstruction.JMP);
        code.add(jmp);
        returnStatementJMPFixPoint.add(jmp);
    }

    //////////////////////////////////////////////////////////////////////////////////
    // CLASS
    //////////////////////////////////////////////////////////////////////////////////

    private boolean insideClass = false;

    @Override
    public void visit(AbstractClassName AbstractClassName) {
        insideClass = true;
    }

    @Override
    public void visit(AbstractClassDecl AbstractClassDecl) {
        insideClass = false;
    }

    @Override
    public void visit(ClassName ClassName) {
        insideClass = true;
    }

    @Override
    public void visit(ClassDeclaration ClassDeclaration) {
        insideClass = false;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // LINK WITH FRONT-END
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (List<Quadruple> list : outputCode) {
            int i = 0;

            for (Quadruple instruction : list) {
                builder.append(String.format("%-4d | %s\n", i++, instruction));
            }

            builder.append("\n");
        }

        return builder.toString();
    }

    public List<List<Quadruple>> getIRCodeOutput() {
        return outputCode;
    }
}
