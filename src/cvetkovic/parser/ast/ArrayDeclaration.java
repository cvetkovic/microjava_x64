// generated with ast extension for cup
// version 0.8
// 25/2/2020 20:23:2


package cvetkovic.parser.ast;

public class ArrayDeclaration extends FactorArrayDecl {

    private MakeNewExpressionDAG MakeNewExpressionDAG;
    private Expr Expr;

    public ArrayDeclaration (MakeNewExpressionDAG MakeNewExpressionDAG, Expr Expr) {
        this.MakeNewExpressionDAG=MakeNewExpressionDAG;
        if(MakeNewExpressionDAG!=null) MakeNewExpressionDAG.setParent(this);
        this.Expr=Expr;
        if(Expr!=null) Expr.setParent(this);
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

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(MakeNewExpressionDAG!=null) MakeNewExpressionDAG.accept(visitor);
        if(Expr!=null) Expr.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(MakeNewExpressionDAG!=null) MakeNewExpressionDAG.traverseTopDown(visitor);
        if(Expr!=null) Expr.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(MakeNewExpressionDAG!=null) MakeNewExpressionDAG.traverseBottomUp(visitor);
        if(Expr!=null) Expr.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ArrayDeclaration(\n");

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

        buffer.append(tab);
        buffer.append(") [ArrayDeclaration]");
        return buffer.toString();
    }
}
