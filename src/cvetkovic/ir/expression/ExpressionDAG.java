package cvetkovic.ir.expression;

import cvetkovic.ir.Quadruple;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionDAG {

    //////////////////////////////////////////////////////////////////////////////////
    // ARRAY OF RECORDS
    //////////////////////////////////////////////////////////////////////////////////
    List<ExpressionNode> arrayOfRecords = new ArrayList<>();
    Map<ExpressionNode, Integer> accessByReference = new HashMap<>();
    Map<Integer, ExpressionNode> accessByIndex = new HashMap<>();

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
        int assignOperationId = ExpressionNode.uniqueId - 1;
        ExpressionNode.resetId();

        List<Quadruple> result = new ArrayList<>();
        int cnt = 1;

        ExpressionNode rootNode = accessByIndex.get(assignOperationId);

        // TODO: do post-order traversal and emit quadruples

        return result;
    }
}
