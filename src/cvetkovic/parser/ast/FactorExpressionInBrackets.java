// generated with ast extension for cup
// version 0.8
// 20/2/2020 20:30:18


package cvetkovic.parser.ast;

public class FactorExpressionInBrackets extends Factor {

    private Expr Expr;

    public FactorExpressionInBrackets(Expr Expr) {
        this.Expr = Expr;
        if (Expr != null) Expr.setParent(this);
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
        if (Expr != null) Expr.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Expr != null) Expr.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Expr != null) Expr.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("FactorExpressionInBrackets(\n");

        if (Expr != null)
            buffer.append(Expr.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FactorExpressionInBrackets]");
        return buffer.toString();
    }
}
