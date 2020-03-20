// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public class DesignatorMethodCallParameters extends DesignatorParams {

    private DesignatorInvokeMethodName DesignatorInvokeMethodName;
    private ActPars ActPars;
    private DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd;

    public DesignatorMethodCallParameters(DesignatorInvokeMethodName DesignatorInvokeMethodName, ActPars ActPars, DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd) {
        this.DesignatorInvokeMethodName = DesignatorInvokeMethodName;
        if (DesignatorInvokeMethodName != null) DesignatorInvokeMethodName.setParent(this);
        this.ActPars = ActPars;
        if (ActPars != null) ActPars.setParent(this);
        this.DesignatorInvokeMethodNameEnd = DesignatorInvokeMethodNameEnd;
        if (DesignatorInvokeMethodNameEnd != null) DesignatorInvokeMethodNameEnd.setParent(this);
    }

    public DesignatorInvokeMethodName getDesignatorInvokeMethodName() {
        return DesignatorInvokeMethodName;
    }

    public void setDesignatorInvokeMethodName(DesignatorInvokeMethodName DesignatorInvokeMethodName) {
        this.DesignatorInvokeMethodName = DesignatorInvokeMethodName;
    }

    public ActPars getActPars() {
        return ActPars;
    }

    public void setActPars(ActPars ActPars) {
        this.ActPars = ActPars;
    }

    public DesignatorInvokeMethodNameEnd getDesignatorInvokeMethodNameEnd() {
        return DesignatorInvokeMethodNameEnd;
    }

    public void setDesignatorInvokeMethodNameEnd(DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd) {
        this.DesignatorInvokeMethodNameEnd = DesignatorInvokeMethodNameEnd;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (DesignatorInvokeMethodName != null) DesignatorInvokeMethodName.accept(visitor);
        if (ActPars != null) ActPars.accept(visitor);
        if (DesignatorInvokeMethodNameEnd != null) DesignatorInvokeMethodNameEnd.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (DesignatorInvokeMethodName != null) DesignatorInvokeMethodName.traverseTopDown(visitor);
        if (ActPars != null) ActPars.traverseTopDown(visitor);
        if (DesignatorInvokeMethodNameEnd != null) DesignatorInvokeMethodNameEnd.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (DesignatorInvokeMethodName != null) DesignatorInvokeMethodName.traverseBottomUp(visitor);
        if (ActPars != null) ActPars.traverseBottomUp(visitor);
        if (DesignatorInvokeMethodNameEnd != null) DesignatorInvokeMethodNameEnd.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorMethodCallParameters(\n");

        if (DesignatorInvokeMethodName != null)
            buffer.append(DesignatorInvokeMethodName.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ActPars != null)
            buffer.append(ActPars.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (DesignatorInvokeMethodNameEnd != null)
            buffer.append(DesignatorInvokeMethodNameEnd.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorMethodCallParameters]");
        return buffer.toString();
    }
}
