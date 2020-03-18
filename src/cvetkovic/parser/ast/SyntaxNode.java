// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


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
