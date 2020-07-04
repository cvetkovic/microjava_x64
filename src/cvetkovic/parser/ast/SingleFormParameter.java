// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:26


package cvetkovic.parser.ast;

public class SingleFormParameter extends SingleFormalParameter {

    private Type Type;
    private String formParamName;
    private FormParamArray FormParamArray;

    public SingleFormParameter (Type Type, String formParamName, FormParamArray FormParamArray) {
        this.Type=Type;
        if(Type!=null) Type.setParent(this);
        this.formParamName=formParamName;
        this.FormParamArray=FormParamArray;
        if(FormParamArray!=null) FormParamArray.setParent(this);
    }

    public Type getType() {
        return Type;
    }

    public void setType(Type Type) {
        this.Type=Type;
    }

    public String getFormParamName() {
        return formParamName;
    }

    public void setFormParamName(String formParamName) {
        this.formParamName=formParamName;
    }

    public FormParamArray getFormParamArray() {
        return FormParamArray;
    }

    public void setFormParamArray(FormParamArray FormParamArray) {
        this.FormParamArray=FormParamArray;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Type!=null) Type.accept(visitor);
        if(FormParamArray!=null) FormParamArray.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Type!=null) Type.traverseTopDown(visitor);
        if(FormParamArray!=null) FormParamArray.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Type!=null) Type.traverseBottomUp(visitor);
        if(FormParamArray!=null) FormParamArray.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("SingleFormParameter(\n");

        if(Type!=null)
            buffer.append(Type.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(" "+tab+formParamName);
        buffer.append("\n");

        if(FormParamArray!=null)
            buffer.append(FormParamArray.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [SingleFormParameter]");
        return buffer.toString();
    }
}
