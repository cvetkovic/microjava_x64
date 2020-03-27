// generated with ast extension for cup
// version 0.8
// 27/2/2020 19:41:35


package cvetkovic.parser.ast;

public class EndOfForStatement implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public EndOfForStatement () {
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
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
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("EndOfForStatement(\n");

        buffer.append(tab);
        buffer.append(") [EndOfForStatement]");
        return buffer.toString();
    }
}
