// generated with ast extension for cup
// version 0.8
// 21/2/2020 13:38:34


package cvetkovic.parser.ast;

public class SingleForVarDecl extends ForVarDecl {

    private DesignatorStatement DesignatorStatement;

    public SingleForVarDecl(DesignatorStatement DesignatorStatement) {
        this.DesignatorStatement = DesignatorStatement;
        if (DesignatorStatement != null) DesignatorStatement.setParent(this);
    }

    public DesignatorStatement getDesignatorStatement() {
        return DesignatorStatement;
    }

    public void setDesignatorStatement(DesignatorStatement DesignatorStatement) {
        this.DesignatorStatement = DesignatorStatement;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if (DesignatorStatement != null) DesignatorStatement.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if (DesignatorStatement != null) DesignatorStatement.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if (DesignatorStatement != null) DesignatorStatement.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(tab);
        buffer.append("SingleForVarDecl(\n");

        if (DesignatorStatement != null)
            buffer.append(DesignatorStatement.toString("  " + tab));
        else
            buffer.append(tab + "  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [SingleForVarDecl]");
        return buffer.toString();
    }
}
