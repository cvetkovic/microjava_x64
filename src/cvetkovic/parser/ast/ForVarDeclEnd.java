// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class ForVarDeclEnd implements SyntaxNode {

    private SyntaxNode parent;
    private int line;

    public ForVarDeclEnd() {
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
        buffer.append("ForVarDeclEnd(\n");

        buffer.append(tab);
        buffer.append(") [ForVarDeclEnd]");
        return buffer.toString();
    }
}
