// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public abstract class SingleFormalParameter implements SyntaxNode {

    public rs.etf.pp1.symboltable.concepts.Obj obj = null;
    private SyntaxNode parent;
    private int line;

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

    public abstract void accept(Visitor visitor);

    public abstract void childrenAccept(Visitor visitor);

    public abstract void traverseTopDown(Visitor visitor);

    public abstract void traverseBottomUp(Visitor visitor);

    public String toString() {
        return toString("");
    }

    public abstract String toString(String tab);
}
