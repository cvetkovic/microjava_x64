// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public class SingleVarArray implements SyntaxNode {

    private SyntaxNode parent;
    private int line;

    public SingleVarArray() {
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent = parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("SingleVarArray(\n");

        buffer.append(tab);
        buffer.append(") [SingleVarArray]");
        return buffer.toString();
    }
}
