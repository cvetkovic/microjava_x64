// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:26


package cvetkovic.parser.ast;

public class ClassDeclarationErrorInExtends extends ClassDecl {

    private ClassName ClassName;
    private ClassVarList ClassVarList;
    private ClassMethodList ClassMethodList;

    public ClassDeclarationErrorInExtends (ClassName ClassName, ClassVarList ClassVarList, ClassMethodList ClassMethodList) {
        this.ClassName=ClassName;
        if(ClassName!=null) ClassName.setParent(this);
        this.ClassVarList=ClassVarList;
        if(ClassVarList!=null) ClassVarList.setParent(this);
        this.ClassMethodList=ClassMethodList;
        if(ClassMethodList!=null) ClassMethodList.setParent(this);
    }

    public ClassName getClassName() {
        return ClassName;
    }

    public void setClassName(ClassName ClassName) {
        this.ClassName=ClassName;
    }

    public ClassVarList getClassVarList() {
        return ClassVarList;
    }

    public void setClassVarList(ClassVarList ClassVarList) {
        this.ClassVarList=ClassVarList;
    }

    public ClassMethodList getClassMethodList() {
        return ClassMethodList;
    }

    public void setClassMethodList(ClassMethodList ClassMethodList) {
        this.ClassMethodList=ClassMethodList;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(ClassName!=null) ClassName.accept(visitor);
        if(ClassVarList!=null) ClassVarList.accept(visitor);
        if(ClassMethodList!=null) ClassMethodList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ClassName!=null) ClassName.traverseTopDown(visitor);
        if(ClassVarList!=null) ClassVarList.traverseTopDown(visitor);
        if(ClassMethodList!=null) ClassMethodList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ClassName!=null) ClassName.traverseBottomUp(visitor);
        if(ClassVarList!=null) ClassVarList.traverseBottomUp(visitor);
        if(ClassMethodList!=null) ClassMethodList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassDeclarationErrorInExtends(\n");

        if(ClassName!=null)
            buffer.append(ClassName.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ClassVarList!=null)
            buffer.append(ClassVarList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ClassMethodList!=null)
            buffer.append(ClassMethodList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [ClassDeclarationErrorInExtends]");
        return buffer.toString();
    }
}
