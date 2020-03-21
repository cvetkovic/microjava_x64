package cvetkovic.ir.quadruple;

import rs.etf.pp1.symboltable.concepts.Obj;

public class QuadrupleObjVar extends QuadrupleVariable {
    protected Obj obj;

    public QuadrupleObjVar(Obj obj) {
        this.obj = obj;
    }

    public Obj getObj() {
        return obj;
    }

    @Override
    public String toString() {
        if (obj.getName().startsWith("ArrayAccess_"))
            return obj.getName().substring(12);
        else
            return obj.toString();
    }
}
