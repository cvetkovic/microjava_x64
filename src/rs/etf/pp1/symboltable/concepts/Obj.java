package rs.etf.pp1.symboltable.concepts;

import cvetkovic.x64.cpu.RegisterDescriptor;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Objekti u MikroJava tabeli simbola: Svaki imenovani objekat u programu se
 * skladisti u Obj cvoru. Svakom opsegu se pridruzuje hes tabela u kojoj se
 * nalaze sva imena deklarisana u okviru opsega.
 *
 * @author ETF
 */
public class Obj {
    public static final int Con = 0, Var = 1, Type = 2, Meth = 3, Fld = 4, Elem = 5, Prog = 6;

    public static final int NO_VALUE = -1;

    // aditional specifier
    public boolean tempVar = false;
    public boolean parameter = false;

    private String name;

    // Con, Var, Type, Meth, Fld, Prog
    private int kind;

    // tip pridruzen imenu
    private Struct type;

    // konstanta(Con): vrednost
    // Meth, Var, Fld: memorijski ofset
    private int adr;
    public RegisterDescriptor parameterDescriptor;

    // Var: nivo ugnezdavanja
    // Meth: broj formalnih argumenata
    private int level;


    // Meth: redni broj formalnog argumenta u definiciji metode
    private int fpPos;

    // Meth: kolekcija lokalnih promenljivih
    // Prog: kolekcija simbola programa
    private SymbolDataStructure locals;


    public int getKind() {
        return kind;
    }

    public Struct getType() {
        return type;
    }

    public Obj(int kind, String name, Struct type) {
        this(kind, name, type, NO_VALUE, NO_VALUE);
    }

    public Obj(int kind, String name, Struct type, int adr, int level) {
        this.name = name;
        this.kind = kind;
        this.type = type;
        this.adr = adr;
        this.level = level;
    }

    public Obj(int kind, String name, Struct type, boolean tempVar) {
        this.tempVar = tempVar;
        this.name = name;
        this.kind = kind;
        this.type = type;
    }

    /**
     * Getter and Setter methods.
     *
     * @return
     */

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getAdr() {
        return adr;
    }

    public void setAdr(int adr) {
        this.adr = adr;
    }

    public int getLevel() {
        return level;
    }

    public int getFpPos() {
        return fpPos;
    }

    public void setFpPos(int fpPos) {
        this.fpPos = fpPos;
    }

    public Collection<Obj> getLocalSymbols() {
        return (locals != null) ? locals.symbols() : Collections.emptyList();
    }

    public void setLocals(SymbolDataStructure locals) {
        this.locals = locals;
    }

    public boolean equals(Object o) {
        if (super.equals(o)) return true; // Podrazumevana implementacija: jednake reference

        if (!(o instanceof Obj)) return false;

        Obj other = (Obj) o;

        return kind == other.kind && name.equals(other.name)
                && type.equals(other.type) && adr == other.adr && level == other.level
                && equalsCompleteHash(locals, other.locals);
    }

    /**
     * Poredi dve hes tabele h1 i h2. Dve hes tabele h1 i h2 su jednake ako su: 1.
     * h1 i h2 jednake reference ILI 2. elementi koji se pri obilasku oba hesa, na
     * isti nacin, nalaze na istim pozicijama jednaki (to se proverava metodom
     * equals koja je redefinisana za klasu Obj).
     */
    public static boolean equalsCompleteHash(SymbolDataStructure h1, SymbolDataStructure h2) {
        if (h1 == h2)
            return true;

        if (h1 == null || h2 == null)
            return false;

        if (h1.numSymbols() == h2.numSymbols()) {
            Collection<Obj> h1Obj = h1.symbols(), h2Obj = h2.symbols();
            Iterator<Obj> itH1 = h1Obj.iterator(), itH2 = h2Obj.iterator();

            while (itH1.hasNext() && itH2.hasNext()) {
                if (!itH1.next().equals(itH2.next()))
                    return false;
            }
            return true;
        }
        else
            return false;
    }


    public void accept(SymbolTableVisitor stv) {
        stv.visitObjNode(this);
    }

    @Override
    public String toString() {
        if (kind == Con)
            return Integer.toString(adr);
        else
            return name;
    }

    public void changeKind(int newKind) {
        this.kind = newKind;
    }
}
