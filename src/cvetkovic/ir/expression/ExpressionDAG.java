package cvetkovic.ir.expression;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleObjVar;
import cvetkovic.structures.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionDAG {

    private static int tmpObjCounter = 1;
    private static String tmpVarPrefix = "t";

    //////////////////////////////////////////////////////////////////////////////////
    // ARRAY OF RECORDS
    //////////////////////////////////////////////////////////////////////////////////
    private List<ExpressionNode> arrayOfRecords = new ArrayList<>();
    private Map<ExpressionNode, Integer> accessByReference = new HashMap<>();
    private Map<Integer, ExpressionNode> accessByIndex = new HashMap<>();
    private ExpressionNode rootNode;

    public ExpressionDAG() {
        ExpressionNode.resetId();
    }

    public ExpressionNode getOrCreateLeaf(Obj variable) {
        ExpressionNode tmp = new ExpressionNode(variable);

        if (accessByReference.containsKey(tmp))
            return arrayOfRecords.get(accessByReference.get(tmp));

        return addRecord(tmp);
    }

    public ExpressionNode getOrCreateNode(ExpressionNodeOperation operation, ExpressionNode leftChild) {
        ExpressionNode tmp = new ExpressionNode(operation, leftChild);

        return addRecord(tmp);
    }

    public ExpressionNode getOrCreateNode(ExpressionNodeOperation operation, ExpressionNode leftChild, ExpressionNode rightChild) {
        ExpressionNode tmp = new ExpressionNode(operation, leftChild, rightChild);

        return addRecord(tmp);
    }

    private ExpressionNode addRecord(ExpressionNode tmp) {
        if (accessByReference.containsKey(tmp))
            return arrayOfRecords.get(accessByReference.get(tmp));

        arrayOfRecords.add(tmp);
        int id = tmp.assignId();
        accessByReference.put(tmp, id);
        accessByIndex.put(id, tmp);

        if (tmp.isBinaryOperator() && tmp.operation == ExpressionNodeOperation.ASSIGNMENT)
            rootNode = tmp;

        return tmp;
    }

    @Override
    public String toString() {
        StringBuilder instructionOutput = new StringBuilder();

        for (ExpressionNode node : arrayOfRecords) {
            instructionOutput.append(node);
            instructionOutput.append(System.lineSeparator());
        }

        return instructionOutput.toString();
    }

    public List<Quadruple> emitQuadruples() {
        List<Quadruple> result = new ArrayList<>();

        for (ExpressionNode node : arrayOfRecords) {
            // only non-leaf nodes shall be added as quadruples because only they are IR instructions
            if (node.isInnerNode()) {
                IRInstruction irInstruction = IRInstruction.dagToQuadrupleInstruction(node.operation);
                Quadruple instruction = new Quadruple(irInstruction);

                if (node.operation == ExpressionNodeOperation.ASSIGNMENT) {
                    Obj resultVar;
                    if (node.getLeftChild().isInnerNode())
                        resultVar = node.getLeftChild().destinationVariable;
                    else
                        resultVar = node.getLeftChild().variable;

                    instruction.setResult(new QuadrupleObjVar(resultVar));

                    ExpressionNode arg1 = node.getRightChild();
                    if (arg1.isLeaf())
                        instruction.setArg1(new QuadrupleObjVar(arg1.getVariable()));
                    else
                        instruction.setArg1(new QuadrupleObjVar(arg1.getDestinationVariable()));
                }
                else {
                    int tmpId = tmpObjCounter++;

                    Obj var = SymbolTable.insert(Obj.Var, tmpVarPrefix + tmpId, SymbolTable.intType);
                    instruction.setResult(new QuadrupleObjVar(var));
                    node.setDestinationVariable(var);

                    ExpressionNode arg1 = node.getLeftChild();
                    if (arg1.isLeaf())
                        instruction.setArg1(new QuadrupleObjVar(arg1.getVariable()));
                    else
                        instruction.setArg1(new QuadrupleObjVar(arg1.getDestinationVariable()));

                    if (node.isBinaryOperator()) {
                        ExpressionNode arg2 = node.getRightChild();
                        if (arg2.isLeaf())
                            instruction.setArg2(new QuadrupleObjVar(arg2.getVariable()));
                        else
                            instruction.setArg2(new QuadrupleObjVar(arg2.getDestinationVariable()));
                    }
                }

                result.add(instruction);
            }
        }

        return result;
    }

    public static String generateTempVarOutside()
    {
        return tmpVarPrefix + tmpObjCounter++;
    }

    public ExpressionNode getLast()
    {
        if (arrayOfRecords.size() > 0)
            return arrayOfRecords.get(arrayOfRecords.size() - 1);
        else
            return null;
    }
}
