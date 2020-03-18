// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class FactorFunctionCall extends Factor {

    private Designator Designator;
    private FactorFunctionCallParameters FactorFunctionCallParameters;

    public FactorFunctionCall(Designator Designator, FactorFunctionCallParameters FactorFunctionCallParameters) {
        this.Designator = Designator;
        if (Designator != null) Designator.setParent(this);
        this.FactorFunctionCallParameters = FactorFunctionCallParameters;
        if (FactorFunctionCallParameters != null) FactorFunctionCallParameters.setParent(this);
    }

    public Designator getDesignator() {
        return Designator;
    }

    public void setDesignator(Designator Designator) {
        this.Designator = Designator;
    }

    public FactorFunctionCallParameters getFactorFunctionCallParameters() {
        return FactorFunctionCallParameters;
    }

    public void setFactorFunctionCallParameters(FactorFunctionCallParameters FactorFunctionCallParameters) {
        this.FactorFunctionCallParameters = FactorFunctionCallParameters;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Designator != null) Designator.accept(visitor);
        if (FactorFunctionCallParameters != null) FactorFunctionCallParameters.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Designator != null) Designator.traverseTopDown(visitor);
        if (FactorFunctionCallParameters != null) FactorFunctionCallParameters.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Designator != null) Designator.traverseBottomUp(visitor);
        if (FactorFunctionCallParameters != null) FactorFunctionCallParameters.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("FactorFunctionCall(\n");

        if (Designator != null)
            buffer.append(Designator.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (FactorFunctionCallParameters != null)
            buffer.append(FactorFunctionCallParameters.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FactorFunctionCall]");
        return buffer.toString();
    }
}
