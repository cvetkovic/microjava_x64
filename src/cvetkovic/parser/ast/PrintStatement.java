// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class PrintStatement extends Statement {

    private Expr Expr;
    private MultiplePrint MultiplePrint;

    public PrintStatement(Expr Expr, MultiplePrint MultiplePrint) {
        this.Expr = Expr;
        if (Expr != null) Expr.setParent(this);
        this.MultiplePrint = MultiplePrint;
        if (MultiplePrint != null) MultiplePrint.setParent(this);
    }

    public Expr getExpr() {
        return Expr;
    }

    public void setExpr(Expr Expr) {
        this.Expr = Expr;
    }

    public MultiplePrint getMultiplePrint() {
        return MultiplePrint;
    }

    public void setMultiplePrint(MultiplePrint MultiplePrint) {
        this.MultiplePrint = MultiplePrint;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Expr != null) Expr.accept(visitor);
        if (MultiplePrint != null) MultiplePrint.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Expr != null) Expr.traverseTopDown(visitor);
        if (MultiplePrint != null) MultiplePrint.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Expr != null) Expr.traverseBottomUp(visitor);
        if (MultiplePrint != null) MultiplePrint.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("PrintStatement(\n");

        if (Expr != null)
            buffer.append(Expr.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (MultiplePrint != null)
            buffer.append(MultiplePrint.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [PrintStatement]");
        return buffer.toString();
    }
}
