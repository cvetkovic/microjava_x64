package cvetkovic.x64.cpu;

import rs.etf.pp1.symboltable.concepts.Obj;

public class AddressDescriptor extends Descriptor {
    protected boolean isGlobalVar;
    protected RegisterDescriptor indexBy;

    public AddressDescriptor(Obj holdsValueOf, boolean isGlobalVar) {
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
                    return "EBP - " + holdsValueOf.getAdr();
                else
                    return holdsValueOf.getName();
            case Obj.Fld:
                return indexBy.toString() + " + " + holdsValueOf.getAdr();

            default:
                throw new RuntimeException("Not implemented address descriptor access.");
        }
    }
}
