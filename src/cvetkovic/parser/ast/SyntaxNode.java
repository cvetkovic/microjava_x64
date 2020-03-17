// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public interface SyntaxNode {

    public void accept(Visitor visitor);

    public void childrenAccept(Visitor visitor);

    public void traverseBottomUp(Visitor visitor);

    public void traverseTopDown(Visitor visitor);

    public SyntaxNode getParent();

    public void setParent(SyntaxNode parent);

    public int getLine();

    public void setLine(int line);
}
