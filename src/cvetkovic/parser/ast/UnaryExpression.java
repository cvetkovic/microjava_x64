// generated with ast extension for cup
// version 0.8
// 27/2/2020 19:41:35


package cvetkovic.parser.ast;

public class UnaryExpression extends Expr {

    private ExprNegative ExprNegative;
    private Term Term;

    public UnaryExpression (ExprNegative ExprNegative, Term Term) {
        this.ExprNegative=ExprNegative;
        if(ExprNegative!=null) ExprNegative.setParent(this);
        this.Term=Term;
        if(Term!=null) Term.setParent(this);
    }

    public ExprNegative getExprNegative() {
        return ExprNegative;
    }

    public void setExprNegative(ExprNegative ExprNegative) {
        this.ExprNegative=ExprNegative;
    }

    public Term getTerm() {
        return Term;
    }

    public void setTerm(Term Term) {
        this.Term=Term;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ExprNegative!=null) ExprNegative.accept(visitor);
        if(Term!=null) Term.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ExprNegative!=null) ExprNegative.traverseTopDown(visitor);
        if(Term!=null) Term.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ExprNegative!=null) ExprNegative.traverseBottomUp(visitor);
        if(Term!=null) Term.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("UnaryExpression(\n");

        if(ExprNegative!=null)
            buffer.append(ExprNegative.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Term!=null)
            buffer.append(Term.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [UnaryExpression]");
        return buffer.toString();
    }
}
