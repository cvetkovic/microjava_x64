// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class DesignatorInvoke extends DesignatorStatement {

    private Designator Designator;
    private DesignatorParams DesignatorParams;

    public DesignatorInvoke(Designator Designator, DesignatorParams DesignatorParams) {
        this.Designator = Designator;
        if (Designator != null) Designator.setParent(this);
        this.DesignatorParams = DesignatorParams;
        if (DesignatorParams != null) DesignatorParams.setParent(this);
    }

    public Designator getDesignator() {
        return Designator;
    }

    public void setDesignator(Designator Designator) {
        this.Designator = Designator;
    }

    public DesignatorParams getDesignatorParams() {
        return DesignatorParams;
    }

    public void setDesignatorParams(DesignatorParams DesignatorParams) {
        this.DesignatorParams = DesignatorParams;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Designator != null) Designator.accept(visitor);
        if (DesignatorParams != null) DesignatorParams.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Designator != null) Designator.traverseTopDown(visitor);
        if (DesignatorParams != null) DesignatorParams.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Designator != null) Designator.traverseBottomUp(visitor);
        if (DesignatorParams != null) DesignatorParams.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorInvoke(\n");

        if (Designator != null)
            buffer.append(Designator.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (DesignatorParams != null)
            buffer.append(DesignatorParams.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorInvoke]");
        return buffer.toString();
    }
}
