// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public class FactorFunctionCallParameter extends FactorFunctionCallParameters {

    private FactorFunctionCallParametersSingle FactorFunctionCallParametersSingle;

    public FactorFunctionCallParameter(FactorFunctionCallParametersSingle FactorFunctionCallParametersSingle) {
        this.FactorFunctionCallParametersSingle = FactorFunctionCallParametersSingle;
        if (FactorFunctionCallParametersSingle != null) FactorFunctionCallParametersSingle.setParent(this);
    }

    public FactorFunctionCallParametersSingle getFactorFunctionCallParametersSingle() {
        return FactorFunctionCallParametersSingle;
    }

    public void setFactorFunctionCallParametersSingle(FactorFunctionCallParametersSingle FactorFunctionCallParametersSingle) {
        this.FactorFunctionCallParametersSingle = FactorFunctionCallParametersSingle;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (FactorFunctionCallParametersSingle != null) FactorFunctionCallParametersSingle.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (FactorFunctionCallParametersSingle != null) FactorFunctionCallParametersSingle.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (FactorFunctionCallParametersSingle != null) FactorFunctionCallParametersSingle.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("FactorFunctionCallParameter(\n");

        if (FactorFunctionCallParametersSingle != null)
            buffer.append(FactorFunctionCallParametersSingle.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FactorFunctionCallParameter]");
        return buffer.toString();
    }
}
