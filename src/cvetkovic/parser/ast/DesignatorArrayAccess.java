// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class DesignatorArrayAccess extends Designator {

    private Designator Designator;
    private Expr Expr;

    public DesignatorArrayAccess(Designator Designator, Expr Expr) {
        this.Designator = Designator;
        if (Designator != null) Designator.setParent(this);
        this.Expr = Expr;
        if (Expr != null) Expr.setParent(this);
    }

    public Designator getDesignator() {
        return Designator;
    }

    public void setDesignator(Designator Designator) {
        this.Designator = Designator;
    }

    public Expr getExpr() {
        return Expr;
    }

    public void setExpr(Expr Expr) {
        this.Expr = Expr;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Designator != null) Designator.accept(visitor);
        if (Expr != null) Expr.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Designator != null) Designator.traverseTopDown(visitor);
        if (Expr != null) Expr.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Designator != null) Designator.traverseBottomUp(visitor);
        if (Expr != null) Expr.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorArrayAccess(\n");

        if (Designator != null)
            buffer.append(Designator.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (Expr != null)
            buffer.append(Expr.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorArrayAccess]");
        return buffer.toString();
    }
}
