// generated with ast extension for cup
// version 0.8
// 4/6/2020 14:32:26


package cvetkovic.parser.ast;

public class SingleForLoopCondition extends ForLoopCondition {

    private StartForCondition StartForCondition;
    private Condition Condition;

    public SingleForLoopCondition (StartForCondition StartForCondition, Condition Condition) {
        this.StartForCondition=StartForCondition;
        if(StartForCondition!=null) StartForCondition.setParent(this);
        this.Condition=Condition;
        if(Condition!=null) Condition.setParent(this);
    }

    public StartForCondition getStartForCondition() {
        return StartForCondition;
    }

    public void setStartForCondition(StartForCondition StartForCondition) {
        this.StartForCondition=StartForCondition;
    }

    public Condition getCondition() {
        return Condition;
    }

    public void setCondition(Condition Condition) {
        this.Condition=Condition;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(StartForCondition!=null) StartForCondition.accept(visitor);
        if(Condition!=null) Condition.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(StartForCondition!=null) StartForCondition.traverseTopDown(visitor);
        if(Condition!=null) Condition.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(StartForCondition!=null) StartForCondition.traverseBottomUp(visitor);
        if(Condition!=null) Condition.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("SingleForLoopCondition(\n");

        if(StartForCondition!=null)
            buffer.append(StartForCondition.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Condition!=null)
            buffer.append(Condition.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [SingleForLoopCondition]");
        return buffer.toString();
    }
}
