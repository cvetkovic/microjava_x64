package cvetkovic.ir.expression;

import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ExpressionDAG {

    //////////////////////////////////////////////////////////////////////////////////
    // ARRAY OF RECORDS
    //////////////////////////////////////////////////////////////////////////////////
    HashMap<ExpressionNode, ExpressionNode> records = new LinkedHashMap<>();

    public ExpressionNode getOrCreateLeaf(Obj variable) {
        ExpressionNode tmp = new ExpressionNode(variable);

        if (records.containsKey(tmp))
            return records.get(tmp);

        tmp.assignId();
        records.put(tmp, tmp);
        return tmp;
    }

    public ExpressionNode getOrCreateNode(ExpressionNodeOperation operation, ExpressionNode leftChild) {
        ExpressionNode tmp = new ExpressionNode(operation, leftChild);

        if (records.containsKey(tmp))
            return records.get(tmp);

        tmp.assignId();
        records.put(tmp, tmp);
        return tmp;
    }

    public ExpressionNode getOrCreateNode(ExpressionNodeOperation operation, ExpressionNode leftChild, ExpressionNode rightChild) {
        ExpressionNode tmp = new ExpressionNode(operation, leftChild, rightChild);

        if (records.containsKey(tmp))
            return records.get(tmp);

        tmp.assignId();
        records.put(tmp, tmp);
        return tmp;
    }

    @Override
    public String toString() {
        ExpressionNode.resetId();

        StringBuilder instructionOutput = new StringBuilder();

        for (ExpressionNode node : records.values()) {
            instructionOutput.append(node);
            instructionOutput.append(System.lineSeparator());
        }

        return instructionOutput.toString();
    }
}
