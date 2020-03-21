// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class NumericalConst extends ConstValue {

    private Integer constNumValue;

    public NumericalConst(Integer constNumValue) {
        this.constNumValue = constNumValue;
    }

    public Integer getConstNumValue() {
        return constNumValue;
    }

    public void setConstNumValue(Integer constNumValue) {
        this.constNumValue = constNumValue;
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
        buffer.append("NumericalConst(\n");

        buffer.append(" " + tab + constNumValue);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [NumericalConst]");
        return buffer.toString();
    }
}
