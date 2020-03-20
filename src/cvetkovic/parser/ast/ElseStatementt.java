// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public class ElseStatementt extends ElseStatement {

    private ElseStatementKeyword ElseStatementKeyword;
    private Statement Statement;

    public ElseStatementt(ElseStatementKeyword ElseStatementKeyword, Statement Statement) {
        this.ElseStatementKeyword = ElseStatementKeyword;
        if (ElseStatementKeyword != null) ElseStatementKeyword.setParent(this);
        this.Statement = Statement;
        if (Statement != null) Statement.setParent(this);
    }

    public ElseStatementKeyword getElseStatementKeyword() {
        return ElseStatementKeyword;
    }

    public void setElseStatementKeyword(ElseStatementKeyword ElseStatementKeyword) {
        this.ElseStatementKeyword = ElseStatementKeyword;
    }

    public Statement getStatement() {
        return Statement;
    }

    public void setStatement(Statement Statement) {
        this.Statement = Statement;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (ElseStatementKeyword != null) ElseStatementKeyword.accept(visitor);
        if (Statement != null) Statement.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (ElseStatementKeyword != null) ElseStatementKeyword.traverseTopDown(visitor);
        if (Statement != null) Statement.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (ElseStatementKeyword != null) ElseStatementKeyword.traverseBottomUp(visitor);
        if (Statement != null) Statement.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("ElseStatementt(\n");

        if (ElseStatementKeyword != null)
            buffer.append(ElseStatementKeyword.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (Statement != null)
            buffer.append(Statement.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ElseStatementt]");
        return buffer.toString();
    }
}
