// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class MultipleFormalParameterr extends MultipleFormalParameter {

    private MultipleFormalParameter MultipleFormalParameter;
    private SingleFormalParameter SingleFormalParameter;

    public MultipleFormalParameterr(MultipleFormalParameter MultipleFormalParameter, SingleFormalParameter SingleFormalParameter) {
        this.MultipleFormalParameter = MultipleFormalParameter;
        if (MultipleFormalParameter != null) MultipleFormalParameter.setParent(this);
        this.SingleFormalParameter = SingleFormalParameter;
        if (SingleFormalParameter != null) SingleFormalParameter.setParent(this);
    }

    public MultipleFormalParameter getMultipleFormalParameter() {
        return MultipleFormalParameter;
    }

    public void setMultipleFormalParameter(MultipleFormalParameter MultipleFormalParameter) {
        this.MultipleFormalParameter = MultipleFormalParameter;
    }

    public SingleFormalParameter getSingleFormalParameter() {
        return SingleFormalParameter;
    }

    public void setSingleFormalParameter(SingleFormalParameter SingleFormalParameter) {
        this.SingleFormalParameter = SingleFormalParameter;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (MultipleFormalParameter != null) MultipleFormalParameter.accept(visitor);
        if (SingleFormalParameter != null) SingleFormalParameter.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (MultipleFormalParameter != null) MultipleFormalParameter.traverseTopDown(visitor);
        if (SingleFormalParameter != null) SingleFormalParameter.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (MultipleFormalParameter != null) MultipleFormalParameter.traverseBottomUp(visitor);
        if (SingleFormalParameter != null) SingleFormalParameter.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("MultipleFormalParameterr(\n");

        if (MultipleFormalParameter != null)
            buffer.append(MultipleFormalParameter.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (SingleFormalParameter != null)
            buffer.append(SingleFormalParameter.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MultipleFormalParameterr]");
        return buffer.toString();
    }
}
