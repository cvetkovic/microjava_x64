package cvetkovic.x64.cpu;

import cvetkovic.x64.SystemV_ABI;
import rs.etf.pp1.symboltable.concepts.Obj;

public class MemoryDescriptor extends Descriptor {
    protected boolean isGlobalVar;
    protected RegisterDescriptor indexBy;

    public MemoryDescriptor(Obj holdsValueOf, boolean isGlobalVar) {
        this.holdsValueOf = holdsValueOf;
        this.isGlobalVar = isGlobalVar;
    }

    public void setIndexBy(RegisterDescriptor indexBy) {
        this.indexBy = indexBy;
    }

    @Override
    public String toString() {
        int objType = holdsValueOf.getKind();

        switch (objType) {
            case Obj.Con:
                return String.valueOf(new Integer(holdsValueOf.getAdr()));
            case Obj.Var:
                if (!isGlobalVar)
                    return SystemV_ABI.getPtrSpecifier(holdsValueOf.getType()) + " [RBP - " + holdsValueOf.getAdr() + "]";
                else
                    return holdsValueOf.getName();
            case Obj.Fld:
                return indexBy.toString() + " + " + holdsValueOf.getAdr();

            default:
                throw new RuntimeException("Not implemented address descriptor access.");
        }
    }
}
