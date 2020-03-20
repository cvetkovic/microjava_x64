// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public class ForStatement extends Statement {

    private ForKeyword ForKeyword;
    private ForVarDecl ForVarDecl;
    private ForVarDeclEnd ForVarDeclEnd;
    private ForLoopCondition ForLoopCondition;
    private ForLoopConditionEnd ForLoopConditionEnd;
    private ForUpdateVarList ForUpdateVarList;
    private Statement Statement;
    private EndOfForStatement EndOfForStatement;

    public ForStatement(ForKeyword ForKeyword, ForVarDecl ForVarDecl, ForVarDeclEnd ForVarDeclEnd, ForLoopCondition ForLoopCondition, ForLoopConditionEnd ForLoopConditionEnd, ForUpdateVarList ForUpdateVarList, Statement Statement, EndOfForStatement EndOfForStatement) {
        this.ForKeyword = ForKeyword;
        if (ForKeyword != null) ForKeyword.setParent(this);
        this.ForVarDecl = ForVarDecl;
        if (ForVarDecl != null) ForVarDecl.setParent(this);
        this.ForVarDeclEnd = ForVarDeclEnd;
        if (ForVarDeclEnd != null) ForVarDeclEnd.setParent(this);
        this.ForLoopCondition = ForLoopCondition;
        if (ForLoopCondition != null) ForLoopCondition.setParent(this);
        this.ForLoopConditionEnd = ForLoopConditionEnd;
        if (ForLoopConditionEnd != null) ForLoopConditionEnd.setParent(this);
        this.ForUpdateVarList = ForUpdateVarList;
        if (ForUpdateVarList != null) ForUpdateVarList.setParent(this);
        this.Statement = Statement;
        if (Statement != null) Statement.setParent(this);
        this.EndOfForStatement = EndOfForStatement;
        if (EndOfForStatement != null) EndOfForStatement.setParent(this);
    }

    public ForKeyword getForKeyword() {
        return ForKeyword;
    }

    public void setForKeyword(ForKeyword ForKeyword) {
        this.ForKeyword = ForKeyword;
    }

    public ForVarDecl getForVarDecl() {
        return ForVarDecl;
    }

    public void setForVarDecl(ForVarDecl ForVarDecl) {
        this.ForVarDecl = ForVarDecl;
    }

    public ForVarDeclEnd getForVarDeclEnd() {
        return ForVarDeclEnd;
    }

    public void setForVarDeclEnd(ForVarDeclEnd ForVarDeclEnd) {
        this.ForVarDeclEnd = ForVarDeclEnd;
    }

    public ForLoopCondition getForLoopCondition() {
        return ForLoopCondition;
    }

    public void setForLoopCondition(ForLoopCondition ForLoopCondition) {
        this.ForLoopCondition = ForLoopCondition;
    }

    public ForLoopConditionEnd getForLoopConditionEnd() {
        return ForLoopConditionEnd;
    }

    public void setForLoopConditionEnd(ForLoopConditionEnd ForLoopConditionEnd) {
        this.ForLoopConditionEnd = ForLoopConditionEnd;
    }

    public ForUpdateVarList getForUpdateVarList() {
        return ForUpdateVarList;
    }

    public void setForUpdateVarList(ForUpdateVarList ForUpdateVarList) {
        this.ForUpdateVarList = ForUpdateVarList;
    }

    public Statement getStatement() {
        return Statement;
    }

    public void setStatement(Statement Statement) {
        this.Statement = Statement;
    }

    public EndOfForStatement getEndOfForStatement() {
        return EndOfForStatement;
    }

    public void setEndOfForStatement(EndOfForStatement EndOfForStatement) {
        this.EndOfForStatement = EndOfForStatement;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (ForKeyword != null) ForKeyword.accept(visitor);
        if (ForVarDecl != null) ForVarDecl.accept(visitor);
        if (ForVarDeclEnd != null) ForVarDeclEnd.accept(visitor);
        if (ForLoopCondition != null) ForLoopCondition.accept(visitor);
        if (ForLoopConditionEnd != null) ForLoopConditionEnd.accept(visitor);
        if (ForUpdateVarList != null) ForUpdateVarList.accept(visitor);
        if (Statement != null) Statement.accept(visitor);
        if (EndOfForStatement != null) EndOfForStatement.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (ForKeyword != null) ForKeyword.traverseTopDown(visitor);
        if (ForVarDecl != null) ForVarDecl.traverseTopDown(visitor);
        if (ForVarDeclEnd != null) ForVarDeclEnd.traverseTopDown(visitor);
        if (ForLoopCondition != null) ForLoopCondition.traverseTopDown(visitor);
        if (ForLoopConditionEnd != null) ForLoopConditionEnd.traverseTopDown(visitor);
        if (ForUpdateVarList != null) ForUpdateVarList.traverseTopDown(visitor);
        if (Statement != null) Statement.traverseTopDown(visitor);
        if (EndOfForStatement != null) EndOfForStatement.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (ForKeyword != null) ForKeyword.traverseBottomUp(visitor);
        if (ForVarDecl != null) ForVarDecl.traverseBottomUp(visitor);
        if (ForVarDeclEnd != null) ForVarDeclEnd.traverseBottomUp(visitor);
        if (ForLoopCondition != null) ForLoopCondition.traverseBottomUp(visitor);
        if (ForLoopConditionEnd != null) ForLoopConditionEnd.traverseBottomUp(visitor);
        if (ForUpdateVarList != null) ForUpdateVarList.traverseBottomUp(visitor);
        if (Statement != null) Statement.traverseBottomUp(visitor);
        if (EndOfForStatement != null) EndOfForStatement.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("ForStatement(\n");

        if (ForKeyword != null)
            buffer.append(ForKeyword.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ForVarDecl != null)
            buffer.append(ForVarDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ForVarDeclEnd != null)
            buffer.append(ForVarDeclEnd.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ForLoopCondition != null)
            buffer.append(ForLoopCondition.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ForLoopConditionEnd != null)
            buffer.append(ForLoopConditionEnd.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ForUpdateVarList != null)
            buffer.append(ForUpdateVarList.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (Statement != null)
            buffer.append(Statement.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (EndOfForStatement != null)
            buffer.append(EndOfForStatement.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ForStatement]");
        return buffer.toString();
    }
}
