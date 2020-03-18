package rs.etf.pp1.symboltable.visitors;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;

public abstract class SymbolTableVisitor {

    public abstract void visitObjNode(Obj objToVisit);

    public abstract void visitScopeNode(Scope scopteToVisit);

    public abstract void visitStructNode(Struct structToVisit);

    public abstract String getOutput();

}
