package cvetkovic.ir.expression;

import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;

public class StructNode extends Struct {
    protected ExpressionNode expressionNode;

    public StructNode(int kind) {
        super(kind);
    }

    public StructNode(int kind, Struct elemType) {
        super(kind, elemType);
    }

    public StructNode(int kind, SymbolDataStructure members) {
        super(kind, members);
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public void setExpressionNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }
}
