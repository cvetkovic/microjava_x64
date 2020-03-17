// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class FactorNumericalConst extends Factor {

    private Integer factorNumConst;

    public FactorNumericalConst(Integer factorNumConst) {
        this.factorNumConst = factorNumConst;
    }

    public Integer getFactorNumConst() {
        return factorNumConst;
    }

    public void setFactorNumConst(Integer factorNumConst) {
        this.factorNumConst = factorNumConst;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("FactorNumericalConst(\n");

        buffer.append(" " + tab + factorNumConst);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FactorNumericalConst]");
        return buffer.toString();
    }
}
