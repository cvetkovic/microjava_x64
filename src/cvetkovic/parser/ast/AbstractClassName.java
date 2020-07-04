// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:26


package cvetkovic.parser.ast;

public class AbstractClassName implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    public rs.etf.pp1.symboltable.concepts.Obj obj = null;

    private String abstractClassIdent;

    public AbstractClassName (String abstractClassIdent) {
        this.abstractClassIdent=abstractClassIdent;
    }

    public String getAbstractClassIdent() {
        return abstractClassIdent;
    }

    public void setAbstractClassIdent(String abstractClassIdent) {
        this.abstractClassIdent=abstractClassIdent;
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
        buffer.append("AbstractClassName(\n");

        buffer.append(" "+tab+abstractClassIdent);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [AbstractClassName]");
        return buffer.toString();
    }
}
