// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class IfCondition implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    private IfKeyword IfKeyword;
    private Condition Condition;

    public IfCondition(IfKeyword IfKeyword, Condition Condition) {
        this.IfKeyword = IfKeyword;
        if (IfKeyword != null) IfKeyword.setParent(this);
        this.Condition = Condition;
        if (Condition != null) Condition.setParent(this);
    }

    public IfKeyword getIfKeyword() {
        return IfKeyword;
    }

    public void setIfKeyword(IfKeyword IfKeyword) {
        this.IfKeyword = IfKeyword;
    }

    public Condition getCondition() {
        return Condition;
    }

    public void setCondition(Condition Condition) {
        this.Condition = Condition;
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
        if (IfKeyword != null) IfKeyword.accept(visitor);
        if (Condition != null) Condition.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (IfKeyword != null) IfKeyword.traverseTopDown(visitor);
        if (Condition != null) Condition.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (IfKeyword != null) IfKeyword.traverseBottomUp(visitor);
        if (Condition != null) Condition.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("IfCondition(\n");

        if (IfKeyword != null)
            buffer.append(IfKeyword.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (Condition != null)
            buffer.append(Condition.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [IfCondition]");
        return buffer.toString();
    }
}
