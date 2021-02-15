package cvetkovic.ir.quadruple.arguments;

import rs.etf.pp1.symboltable.concepts.Obj;

public class QuadruplePhi extends QuadrupleVariable {
    private Obj obj;
    private int[] varList;

    private int indexCnt = 0;

    public QuadruplePhi(Obj obj, int argc) {
        this.obj = obj;
        this.varList = new int[argc];
        for (int i = 0; i < argc; i++)
            this.varList[i] = -1;
    }

    public void setPhiArg(int cnt) {
        if (indexCnt >= varList.length)
            throw new RuntimeException("Operand of current PHI function cannot be replaced more than " + varList.length + " times.");

        varList[indexCnt++] = cnt;
    }

    public int getPhiArg(int index) {
        if (index >= varList.length || index < 0)
            throw new RuntimeException("Invalid QuadruplePhi indexing argument.");

        return varList[index];
    }

    public int size() {
        return varList.length;
    }

    public Obj getObj() {
        return obj;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("φ(");
        for (int cnt : varList)
            builder.append(obj).append("_").append(cnt).append(", ");

        return builder.substring(0, builder.length() - 2) + ")";
    }
}
