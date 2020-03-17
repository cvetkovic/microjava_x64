// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public class FactorArrayDeclaration extends Factor {

    private Type Type;
    private FactorArrayDecl FactorArrayDecl;

    public FactorArrayDeclaration(Type Type, FactorArrayDecl FactorArrayDecl) {
        this.Type = Type;
        if (Type != null) Type.setParent(this);
        this.FactorArrayDecl = FactorArrayDecl;
        if (FactorArrayDecl != null) FactorArrayDecl.setParent(this);
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type = Type;
    }

    public FactorArrayDecl getFactorArrayDecl() {
        return FactorArrayDecl;
    }

    public void setFactorArrayDecl(FactorArrayDecl FactorArrayDecl) {
        this.FactorArrayDecl = FactorArrayDecl;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (Type != null) Type.accept(visitor);
        if (FactorArrayDecl != null) FactorArrayDecl.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (Type != null) Type.traverseTopDown(visitor);
        if (FactorArrayDecl != null) FactorArrayDecl.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (Type != null) Type.traverseBottomUp(visitor);
        if (FactorArrayDecl != null) FactorArrayDecl.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("FactorArrayDeclaration(\n");

        if (Type != null)
            buffer.append(Type.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        if (FactorArrayDecl != null)
            buffer.append(FactorArrayDecl.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [FactorArrayDeclaration]");
        return buffer.toString();
    }
}
