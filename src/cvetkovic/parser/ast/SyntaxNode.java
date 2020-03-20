// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public interface SyntaxNode {

    void accept(Visitor visitor);

    void childrenAccept(Visitor visitor);

    void traverseBottomUp(Visitor visitor);

    void traverseTopDown(Visitor visitor);

    SyntaxNode getParent();

    void setParent(SyntaxNode parent);

    int getLine();
    void setLine(int line);
}
