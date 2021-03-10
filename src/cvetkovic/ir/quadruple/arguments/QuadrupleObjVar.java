package cvetkovic.ir.quadruple.arguments;

import cvetkovic.misc.Config;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.util.HashMap;
import java.util.Map;

public class QuadrupleObjVar extends QuadrupleVariable {
    protected final Obj obj;

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

    public static final Map<Obj, Obj> clonedRefs = new HashMap<>();

    @Override
    public QuadrupleVariable makeClone() {
        try {
            Obj cloned;

            if (clonedRefs.containsKey(obj))
                cloned = clonedRefs.get(obj);
            else {
                cloned = (Obj) obj.clone();
                if (cloned.getKind() == Obj.Var || cloned.getKind() == Obj.Fld)
                    cloned.setAdr(cloned.getAdr() + Config.inlinedAddressOffset);
                cloned.setName(cloned.getName() + "_i_" + Config.inlinedCounter);

                clonedRefs.put(obj, cloned);
            }

            return new QuadrupleObjVar(cloned);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
