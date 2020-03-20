// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public class TermSingle extends Term {

    private Factor Factor;

    public TermSingle(Factor Factor) {
        this.Factor = Factor;
        if (Factor != null) Factor.setParent(this);
    }

    public Factor getFactor() {
        return Factor;
    }

    public void setFactor(Factor Factor) {
        this.Factor = Factor;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Factor != null) Factor.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Factor != null) Factor.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Factor != null) Factor.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("TermSingle(\n");

        if (Factor != null)
            buffer.append(Factor.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [TermSingle]");
        return buffer.toString();
    }
}
