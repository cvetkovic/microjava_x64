package cvetkovic.ir.quadruple.arguments;

import rs.etf.pp1.symboltable.concepts.Obj;

public class QuadruplePhi extends QuadrupleVariable {
    private Obj[] varList;

    public QuadruplePhi(int argc) {
        this.varList = new Obj[argc];
    }

    public Obj getPhiArgument(int index) {
        if (index >= varList.length || index < 0)
            throw new IllegalArgumentException("Invalid index for setting phi function argument.");

        return varList[index];
    }

    public void setPhiArg(int index, Obj var) {
        if (index >= varList.length || index < 0)
            throw new IllegalArgumentException("Invalid index for setting phi function argument.");

        varList[index] = var;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("φ(");
        for (Obj obj : varList)
            builder.append(obj).append(", ");

        return builder.substring(0, builder.length() - 2) + ")";
    }
}
