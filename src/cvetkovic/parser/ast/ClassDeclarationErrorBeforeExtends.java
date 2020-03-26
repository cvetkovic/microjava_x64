// generated with ast extension for cup
// version 0.8
// 25/2/2020 20:23:2


package cvetkovic.parser.ast;

public class ClassDeclarationErrorBeforeExtends extends ClassDecl {

    private ClassDeclExtends ClassDeclExtends;
    private ClassVarList ClassVarList;
    private ClassMethodList ClassMethodList;

    public ClassDeclarationErrorBeforeExtends (ClassDeclExtends ClassDeclExtends, ClassVarList ClassVarList, ClassMethodList ClassMethodList) {
        this.ClassDeclExtends=ClassDeclExtends;
        if(ClassDeclExtends!=null) ClassDeclExtends.setParent(this);
        this.ClassVarList=ClassVarList;
        if(ClassVarList!=null) ClassVarList.setParent(this);
        this.ClassMethodList=ClassMethodList;
        if(ClassMethodList!=null) ClassMethodList.setParent(this);
    }

    public ClassDeclExtends getClassDeclExtends() {
        return ClassDeclExtends;
    }

    public void setClassDeclExtends(ClassDeclExtends ClassDeclExtends) {
        this.ClassDeclExtends=ClassDeclExtends;
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
        if(ClassDeclExtends!=null) ClassDeclExtends.accept(visitor);
        if(ClassVarList!=null) ClassVarList.accept(visitor);
        if(ClassMethodList!=null) ClassMethodList.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(ClassDeclExtends!=null) ClassDeclExtends.traverseTopDown(visitor);
        if(ClassVarList!=null) ClassVarList.traverseTopDown(visitor);
        if(ClassMethodList!=null) ClassMethodList.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(ClassDeclExtends!=null) ClassDeclExtends.traverseBottomUp(visitor);
        if(ClassVarList!=null) ClassVarList.traverseBottomUp(visitor);
        if(ClassMethodList!=null) ClassMethodList.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("ClassDeclarationErrorBeforeExtends(\n");

        if(ClassDeclExtends!=null)
            buffer.append(ClassDeclExtends.toString("  "+tab));
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
        buffer.append(") [ClassDeclarationErrorBeforeExtends]");
        return buffer.toString();
    }
}
