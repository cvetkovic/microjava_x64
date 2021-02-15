package cvetkovic.ir.quadruple.arguments;

import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashSet;
import java.util.Set;

public class QuadruplePhi extends QuadrupleVariable {
    private Obj obj;
    private Set<Integer> varList = new HashSet<>();

    public QuadruplePhi(Obj obj) {
        this.obj = obj;
    }

    public void setPhiArg(int cnt) {
        varList.add(cnt);
    }

    public int getPhiArg(int index) {
        if (index >= varList.size() || index < 0)
            throw new RuntimeException("Invalid QuadruplePhi indexing argument.");

        int cnt = 0;
        for (Integer i : varList) {
            if (cnt == index)
                return i;
        }

        return -1;
    }

    public int size() {
        return varList.size();
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
