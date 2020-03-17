// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class CondTermRightt extends CondTermRight {

    private CondTermRight CondTermRight;
    private CondFact CondFact;

    public CondTermRightt(CondTermRight CondTermRight, CondFact CondFact) {
        this.CondTermRight = CondTermRight;
        if (CondTermRight != null) CondTermRight.setParent(this);
        this.CondFact = CondFact;
        if (CondFact != null) CondFact.setParent(this);
    }

    public CondTermRight getCondTermRight() {
        return CondTermRight;
    }

    public void setCondTermRight(CondTermRight CondTermRight) {
        this.CondTermRight = CondTermRight;
    }

    public CondFact getCondFact() {
        return CondFact;
    }

    public void setCondFact(CondFact CondFact) {
        this.CondFact = CondFact;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (CondTermRight != null) CondTermRight.accept(visitor);
        if (CondFact != null) CondFact.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (CondTermRight != null) CondTermRight.traverseTopDown(visitor);
        if (CondFact != null) CondFact.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (CondTermRight != null) CondTermRight.traverseBottomUp(visitor);
        if (CondFact != null) CondFact.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("CondTermRightt(\n");

        if (CondTermRight != null)
            buffer.append(CondTermRight.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (CondFact != null)
            buffer.append(CondFact.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [CondTermRightt]");
        return buffer.toString();
    }
}
