package cvetkovic.x64.cpu;

import rs.etf.pp1.symboltable.concepts.Obj;

public class AddressDescriptor extends Descriptor {
    public Obj getHoldsValueOf() {
        return holdsValueOf;
    }

    public void setHoldsValueOf(Obj holdsValueOf) {
        this.holdsValueOf = holdsValueOf;
    }
}
