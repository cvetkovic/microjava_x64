// generated with ast extension for cup
// version 0.8
// 25/2/2020 20:23:2


package cvetkovic.parser.ast;

public class FactorFunctionCallParameterSingle extends FactorFunctionCallParametersSingle {

    private DesignatorInvokeMethodName DesignatorInvokeMethodName;
    private ActParsStart ActParsStart;
    private ActPars ActPars;
    private ActParsEnd ActParsEnd;
    private DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd;

    public FactorFunctionCallParameterSingle (DesignatorInvokeMethodName DesignatorInvokeMethodName, ActParsStart ActParsStart, ActPars ActPars, ActParsEnd ActParsEnd, DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd) {
        this.DesignatorInvokeMethodName=DesignatorInvokeMethodName;
        if(DesignatorInvokeMethodName!=null) DesignatorInvokeMethodName.setParent(this);
        this.ActParsStart=ActParsStart;
        if(ActParsStart!=null) ActParsStart.setParent(this);
        this.ActPars=ActPars;
        if(ActPars!=null) ActPars.setParent(this);
        this.ActParsEnd=ActParsEnd;
        if(ActParsEnd!=null) ActParsEnd.setParent(this);
        this.DesignatorInvokeMethodNameEnd=DesignatorInvokeMethodNameEnd;
        if(DesignatorInvokeMethodNameEnd!=null) DesignatorInvokeMethodNameEnd.setParent(this);
    }

    public DesignatorInvokeMethodName getDesignatorInvokeMethodName() {
        return DesignatorInvokeMethodName;
    }

    public void setDesignatorInvokeMethodName(DesignatorInvokeMethodName DesignatorInvokeMethodName) {
        this.DesignatorInvokeMethodName=DesignatorInvokeMethodName;
    }

    public ActParsStart getActParsStart() {
        return ActParsStart;
    }

    public void setActParsStart(ActParsStart ActParsStart) {
        this.ActParsStart=ActParsStart;
    }

    public ActPars getActPars() {
        return ActPars;
    }

    public void setActPars(ActPars ActPars) {
        this.ActPars=ActPars;
    }

    public ActParsEnd getActParsEnd() {
        return ActParsEnd;
    }

    public void setActParsEnd(ActParsEnd ActParsEnd) {
        this.ActParsEnd=ActParsEnd;
    }

    public DesignatorInvokeMethodNameEnd getDesignatorInvokeMethodNameEnd() {
        return DesignatorInvokeMethodNameEnd;
    }

    public void setDesignatorInvokeMethodNameEnd(DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd) {
        this.DesignatorInvokeMethodNameEnd=DesignatorInvokeMethodNameEnd;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(DesignatorInvokeMethodName!=null) DesignatorInvokeMethodName.accept(visitor);
        if(ActParsStart!=null) ActParsStart.accept(visitor);
        if(ActPars!=null) ActPars.accept(visitor);
        if(ActParsEnd!=null) ActParsEnd.accept(visitor);
        if(DesignatorInvokeMethodNameEnd!=null) DesignatorInvokeMethodNameEnd.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(DesignatorInvokeMethodName!=null) DesignatorInvokeMethodName.traverseTopDown(visitor);
        if(ActParsStart!=null) ActParsStart.traverseTopDown(visitor);
        if(ActPars!=null) ActPars.traverseTopDown(visitor);
        if(ActParsEnd!=null) ActParsEnd.traverseTopDown(visitor);
        if(DesignatorInvokeMethodNameEnd!=null) DesignatorInvokeMethodNameEnd.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(DesignatorInvokeMethodName!=null) DesignatorInvokeMethodName.traverseBottomUp(visitor);
        if(ActParsStart!=null) ActParsStart.traverseBottomUp(visitor);
        if(ActPars!=null) ActPars.traverseBottomUp(visitor);
        if(ActParsEnd!=null) ActParsEnd.traverseBottomUp(visitor);
        if(DesignatorInvokeMethodNameEnd!=null) DesignatorInvokeMethodNameEnd.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("FactorFunctionCallParameterSingle(\n");

        if(DesignatorInvokeMethodName!=null)
            buffer.append(DesignatorInvokeMethodName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ActParsStart!=null)
            buffer.append(ActParsStart.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ActPars!=null)
            buffer.append(ActPars.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ActParsEnd!=null)
            buffer.append(ActParsEnd.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(DesignatorInvokeMethodNameEnd!=null)
            buffer.append(DesignatorInvokeMethodNameEnd.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FactorFunctionCallParameterSingle]");
        return buffer.toString();
    }
}
