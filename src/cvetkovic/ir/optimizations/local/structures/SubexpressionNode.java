package cvetkovic.ir.optimizations.local.structures;

import cvetkovic.ir.IRInstruction;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.ArrayList;
import java.util.List;

public class SubexpressionNode {

    public IRInstruction instruction;

    public List<Obj> aliases = new ArrayList<>();
    public boolean deadNode = false;

    public SubexpressionNode leftChild;
    public SubexpressionNode rightChild;

    public boolean isLeaf() {
        return instruction == null;
    }

    public boolean isRoot() {
        return instruction != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SubexpressionNode))
            throw new RuntimeException("Invalid type to do equality comparison.");

        SubexpressionNode node = (SubexpressionNode) obj;

        if (instruction == node.instruction && (instruction == IRInstruction.ADD || instruction == IRInstruction.MUL)) {
            return (leftChild == node.leftChild && rightChild == node.rightChild) ||
                    (leftChild == node.rightChild && rightChild == node.leftChild);
        }
        else {
            return instruction == node.instruction &&
                    leftChild == node.leftChild &&
                    rightChild == node.rightChild;
        }
    }

    @Override
    public int hashCode() {
        return 1;
    }
}
