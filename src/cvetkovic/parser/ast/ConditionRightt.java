// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class ConditionRightt extends ConditionRight {

    private ConditionRight ConditionRight;
    private LogicalOr LogicalOr;
    private CondTerm CondTerm;

    public ConditionRightt(ConditionRight ConditionRight, LogicalOr LogicalOr, CondTerm CondTerm) {
        this.ConditionRight = ConditionRight;
        if (ConditionRight != null) ConditionRight.setParent(this);
        this.LogicalOr = LogicalOr;
        if (LogicalOr != null) LogicalOr.setParent(this);
        this.CondTerm = CondTerm;
        if (CondTerm != null) CondTerm.setParent(this);
    }

    public ConditionRight getConditionRight() {
        return ConditionRight;
    }

    public void setConditionRight(ConditionRight ConditionRight) {
        this.ConditionRight = ConditionRight;
    }

    public LogicalOr getLogicalOr() {
        return LogicalOr;
    }

    public void setLogicalOr(LogicalOr LogicalOr) {
        this.LogicalOr = LogicalOr;
    }

    public CondTerm getCondTerm() {
        return CondTerm;
    }

    public void setCondTerm(CondTerm CondTerm) {
        this.CondTerm = CondTerm;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (ConditionRight != null) ConditionRight.accept(visitor);
        if (LogicalOr != null) LogicalOr.accept(visitor);
        if (CondTerm != null) CondTerm.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (ConditionRight != null) ConditionRight.traverseTopDown(visitor);
        if (LogicalOr != null) LogicalOr.traverseTopDown(visitor);
        if (CondTerm != null) CondTerm.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (ConditionRight != null) ConditionRight.traverseBottomUp(visitor);
        if (LogicalOr != null) LogicalOr.traverseBottomUp(visitor);
        if (CondTerm != null) CondTerm.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("ConditionRightt(\n");

        if (ConditionRight != null)
            buffer.append(ConditionRight.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (LogicalOr != null)
            buffer.append(LogicalOr.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (CondTerm != null)
            buffer.append(CondTerm.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ConditionRightt]");
        return buffer.toString();
    }
}
