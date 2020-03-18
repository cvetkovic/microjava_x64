package rs.etf.pp1.symboltable.concepts;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.structure.HashTableDataStructure;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Struktura tipa u MikroJavi.
 *
 * @author ETF
 */
public class Struct {

    // kodiranje tipova
    public static final int None = 0;
    public static final int Int = 1;
    public static final int Char = 2;
    public static final int Array = 3;
    public static final int Class = 4;
    public static final int Bool = 5;
    public static final int Enum = 6;
    public static final int Interface = 7;

    private int kind; // None, Int, Char, Array, Class, Bool, Enum, Interface

    // niz: tip elementa niza
    // klasa: tip roditeljske klase
    private Struct elemType;

    // klasa: lista implementiranih interfejsa
    private Collection<Struct> implementedInterfaceList;

    // klasa: broj bolja klase
    private int numOfFields;

    // klasa i interfejs: referenca na hes tabelu u kojoj se nalaze polja klase
    // enum: referenca na hes tabelu u kojoj se nalaze konstante nabrajanja
    private SymbolDataStructure members;

    public void setElementType(Struct type) {
        elemType = type;
    }

    public void setMembers(SymbolDataStructure symbols) {
        members = symbols;
        numOfFields = 0;
        if (symbols != null) {
            for (Obj s : symbols.symbols()) {
                if (s.getKind() == Obj.Fld)
                    numOfFields++;
            }
        }
    }

    public Struct(int kind) {
        this.kind = kind;
    }

    public Struct(int kind, Struct elemType) {
        this.kind = kind;
        if (kind == Array) this.elemType = elemType;
    }

    public Struct(int kind, SymbolDataStructure members) {
        this.kind = kind;
        setMembers(members);
    }

    public int getKind() {
        return kind;
    }

    public Struct getElemType() {
        return elemType;
    }

    public int getNumberOfFields() {
        return numOfFields;
    }

    public void addImplementedInterface(Struct interfaceStruct) {
        if (implementedInterfaceList == null) {
            implementedInterfaceList = new ArrayList<Struct>();
        }
        implementedInterfaceList.add(interfaceStruct);
    }

    public Collection<Struct> getImplementedInterfaces() {
        if (implementedInterfaceList == null) {
            implementedInterfaceList = new ArrayList<Struct>();
        }
        return implementedInterfaceList;
    }

    /**
     * Retrieves the internal symbol data structure.
     *
     * @return
     */
    public SymbolDataStructure getMembersTable() {
        if (members == null)
            members = new HashTableDataStructure();
        return members;
    }

    /**
     * Retrieves a collection of all Obj nodes in the list of local symbols of the given type. <br/>
     * Invokes {@link Struct#getMembersTable()}.
     *
     * @return A collection of Obj nodes.
     */
    public Collection<Obj> getMembers() {
        return getMembersTable().symbols();
    }

    public boolean equals(Object o) {
        // najpre provera da li su reference jednake
        if (super.equals(o)) return true;

        if (!(o instanceof Struct)) return false;

        return equals((Struct) o);
    }

    public boolean isRefType() {
        return kind == Class || kind == Array;
    }

    public boolean equals(Struct other) {
        if (kind == Array) return other.kind == Array
                && elemType.equals(other.elemType);

        if (kind == Class) return other.kind == Class && numOfFields == other.numOfFields
                && Obj.equalsCompleteHash(members, other.members);

        // mora biti isti Struct cvor
        return this == other;
    }

    public boolean compatibleWith(Struct other) {
        return this.equals(other) || this == Tab.nullType && other.isRefType()
                || other == Tab.nullType && this.isRefType();
    }

    public boolean assignableTo(Struct dest) {
        return this.equals(dest)
                ||
                (this == Tab.nullType && dest.isRefType())
                ||
                (this.kind == Array && dest.kind == Array && dest.elemType == Tab.noType);
    }

    public void accept(SymbolTableVisitor stv) {
        stv.visitStructNode(this);
    }

}
