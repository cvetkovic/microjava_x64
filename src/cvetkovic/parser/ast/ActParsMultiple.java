// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public class ActParsMultiple extends ActPars {

    private Expr Expr;
    private ActPars ActPars;

    public ActParsMultiple(Expr Expr, ActPars ActPars) {
        this.Expr = Expr;
        if (Expr != null) Expr.setParent(this);
        this.ActPars = ActPars;
        if (ActPars != null) ActPars.setParent(this);
    }

    public Expr getExpr() {
        return Expr;
    }

    public void setExpr(Expr Expr) {
        this.Expr = Expr;
    }

    public ActPars getActPars() {
        return ActPars;
    }

    public void setActPars(ActPars ActPars) {
        this.ActPars = ActPars;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Expr != null) Expr.accept(visitor);
        if (ActPars != null) ActPars.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Expr != null) Expr.traverseTopDown(visitor);
        if (ActPars != null) ActPars.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Expr != null) Expr.traverseBottomUp(visitor);
        if (ActPars != null) ActPars.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("ActParsMultiple(\n");

        if (Expr != null)
            buffer.append(Expr.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (ActPars != null)
            buffer.append(ActPars.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ActParsMultiple]");
        return buffer.toString();
    }
}
