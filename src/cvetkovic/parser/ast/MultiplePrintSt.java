// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class MultiplePrintSt extends MultiplePrint {

    private Integer multiplePrintConst;

    public MultiplePrintSt(Integer multiplePrintConst) {
        this.multiplePrintConst = multiplePrintConst;
    }

    public Integer getMultiplePrintConst() {
        return multiplePrintConst;
    }

    public void setMultiplePrintConst(Integer multiplePrintConst) {
        this.multiplePrintConst = multiplePrintConst;
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
        buffer.append("MultiplePrintSt(\n");

        buffer.append(" " + tab + multiplePrintConst);
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MultiplePrintSt]");
        return buffer.toString();
    }
}
