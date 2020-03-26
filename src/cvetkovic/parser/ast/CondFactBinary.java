// generated with ast extension for cup
// version 0.8
// 25/2/2020 20:23:2


package cvetkovic.parser.ast;

public class CondFactBinary extends CondFact {

    private MakeNewExpressionDAG MakeNewExpressionDAG;
    private Expr Expr;
    private Relop Relop;
    private MakeNewExpressionDAG MakeNewExpressionDAG1;
    private Expr Expr2;

    public CondFactBinary (MakeNewExpressionDAG MakeNewExpressionDAG, Expr Expr, Relop Relop, MakeNewExpressionDAG MakeNewExpressionDAG1, Expr Expr2) {
        this.MakeNewExpressionDAG=MakeNewExpressionDAG;
        if(MakeNewExpressionDAG!=null) MakeNewExpressionDAG.setParent(this);
        this.Expr=Expr;
        if(Expr!=null) Expr.setParent(this);
        this.Relop=Relop;
        if(Relop!=null) Relop.setParent(this);
        this.MakeNewExpressionDAG1=MakeNewExpressionDAG1;
        if(MakeNewExpressionDAG1!=null) MakeNewExpressionDAG1.setParent(this);
        this.Expr2=Expr2;
        if(Expr2!=null) Expr2.setParent(this);
    }

    public MakeNewExpressionDAG getMakeNewExpressionDAG() {
        return MakeNewExpressionDAG;
    }

    public void setMakeNewExpressionDAG(MakeNewExpressionDAG MakeNewExpressionDAG) {
        this.MakeNewExpressionDAG=MakeNewExpressionDAG;
    }

    public Expr getExpr() {
        return Expr;
    }

    public void setExpr(Expr Expr) {
        this.Expr=Expr;
    }

    public Relop getRelop() {
        return Relop;
    }

    public void setRelop(Relop Relop) {
        this.Relop=Relop;
    }

    public MakeNewExpressionDAG getMakeNewExpressionDAG1() {
        return MakeNewExpressionDAG1;
    }

    public void setMakeNewExpressionDAG1(MakeNewExpressionDAG MakeNewExpressionDAG1) {
        this.MakeNewExpressionDAG1=MakeNewExpressionDAG1;
    }

    public Expr getExpr2() {
        return Expr2;
    }

    public void setExpr2(Expr Expr2) {
        this.Expr2=Expr2;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MakeNewExpressionDAG!=null) MakeNewExpressionDAG.accept(visitor);
        if(Expr!=null) Expr.accept(visitor);
        if(Relop!=null) Relop.accept(visitor);
        if(MakeNewExpressionDAG1!=null) MakeNewExpressionDAG1.accept(visitor);
        if(Expr2!=null) Expr2.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MakeNewExpressionDAG!=null) MakeNewExpressionDAG.traverseTopDown(visitor);
        if(Expr!=null) Expr.traverseTopDown(visitor);
        if(Relop!=null) Relop.traverseTopDown(visitor);
        if(MakeNewExpressionDAG1!=null) MakeNewExpressionDAG1.traverseTopDown(visitor);
        if(Expr2!=null) Expr2.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MakeNewExpressionDAG!=null) MakeNewExpressionDAG.traverseBottomUp(visitor);
        if(Expr!=null) Expr.traverseBottomUp(visitor);
        if(Relop!=null) Relop.traverseBottomUp(visitor);
        if(MakeNewExpressionDAG1!=null) MakeNewExpressionDAG1.traverseBottomUp(visitor);
        if(Expr2!=null) Expr2.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("CondFactBinary(\n");

        if(MakeNewExpressionDAG!=null)
            buffer.append(MakeNewExpressionDAG.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Expr!=null)
            buffer.append(Expr.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Relop!=null)
            buffer.append(Relop.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MakeNewExpressionDAG1!=null)
            buffer.append(MakeNewExpressionDAG1.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Expr2!=null)
            buffer.append(Expr2.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [CondFactBinary]");
        return buffer.toString();
    }
}
