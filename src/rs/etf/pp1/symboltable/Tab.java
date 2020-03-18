package rs.etf.pp1.symboltable;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;


/**
 * MikroJava tabela simbola
 *
 * @author ETF
 */
public class Tab {


    // standardni tipovi
    public static final Struct noType = new Struct(Struct.None),
            intType = new Struct(Struct.Int), charType = new Struct(Struct.Char),
            nullType = new Struct(Struct.Class);
    /*
        static SymbolDataStructure loc = new HashTableDataStructure();
        static {
            loc.insertKey(new Obj(Obj.Fld, "x", intType, 0, -1), false);
            loc.insertKey(new Obj(Obj.Fld, "y", intType, 1, -1), false);
            nullType = new Struct(Struct.Class, null, loc);
        }
        */
    public static final Obj noObj = new Obj(Obj.Var, "noObj", noType);
    public static Obj chrObj, ordObj, lenObj;

    public static Scope currentScope; // tekuci opseg
    private static int currentLevel; // nivo ugnezdavanja tekuceg opsega

    /**
     * Inicijalizacija universe opsega, tj. njegovo popunjavanje Obj cvorovima,
     * kao sto je izlozeno na vezbama i predavanjima. Razlika je sto se Obj
     * cvorovu umecu u hes tabelu.
     */
    public static void init() {
        Scope universe = currentScope = new Scope(null);

        universe.addToLocals(new Obj(Obj.Type, "int", intType));
        universe.addToLocals(new Obj(Obj.Type, "char", charType));
        universe.addToLocals(new Obj(Obj.Con, "eol", charType, 10, 0));
        universe.addToLocals(new Obj(Obj.Con, "null", nullType, 0, 0));

        universe.addToLocals(chrObj = new Obj(Obj.Meth, "chr", charType, 0, 1));
        {
            openScope();
            currentScope.addToLocals(new Obj(Obj.Var, "i", intType, 0, 1));
            chrObj.setLocals(currentScope.getLocals());
            closeScope();
        }

        universe.addToLocals(ordObj = new Obj(Obj.Meth, "ord", intType, 0, 1));
        {
            openScope();
            currentScope.addToLocals(new Obj(Obj.Var, "ch", charType, 0, 1));
            ordObj.setLocals(currentScope.getLocals());
            closeScope();
        }


        universe.addToLocals(lenObj = new Obj(Obj.Meth, "len", intType, 0, 1));
        {
            openScope();
            currentScope.addToLocals(new Obj(Obj.Var, "arr", new Struct(Struct.Array, noType), 0, 1));
            lenObj.setLocals(currentScope.getLocals());
            closeScope();
        }

        currentLevel = -1;
    }

    public static void chainLocalSymbols(Obj outerScopeObj) {
        outerScopeObj.setLocals(currentScope.getLocals());
    }

    public static void chainLocalSymbols(Struct innerClass) {
        innerClass.setMembers(currentScope.getLocals());
    }

    /**
     * Otvaranje novog opsega
     */
    public static void openScope() {
        currentScope = new Scope(currentScope);
        currentLevel++;
    }

    /**
     * Zatvaranje opsega
     */
    public static void closeScope() {
        currentScope = currentScope.getOuter();
        currentLevel--;
    }

    /**
     * Pravi se novi Obj cvor sa prosledjenim atributima kind, name i type, pa se
     * zatim ubacuje u tabelu simbola. Povratna vrednost: - novostvoreni cvor, ako
     * cvor sa tim imenom nije vec postojao u tabeli simbola. - postojeci cvor iz
     * tabele simbola, ako je doslo do greske jer smo pokusali da u tabelu simbola
     * za opseg ubacimo cvor sa imenom koje vec postoji.
     */
    public static Obj insert(int kind, String name, Struct type) {
        // create a new Object node with kind, name, type
        Obj newObj = new Obj(kind, name, type, 0, ((currentLevel != 0) ? 1 : 0));

        // append the node to the end of the symbol list
        if (!currentScope.addToLocals(newObj)) {
            Obj res = currentScope.findSymbol(name);
            return (res != null) ? res : noObj;
        }
        else
            return newObj;
    }

    /**
     * U hes tabeli opsega trazi Obj cvor sa imenom name, pocevsi od
     * najugnezdenijeg opsega, pa redom kroz opsege na nizim nivoima. Povratna
     * vrednost: - pronadjeni Obj cvor, ako je pretrazivanje bilo uspesno. -
     * Tab.noObj objekat, ako je pretrazivanje bilo neuspesno.
     */
    public static Obj find(String name) {
        Obj resultObj = null;
        for (Scope s = currentScope; s != null; s = s.getOuter()) {
            if (s.getLocals() != null) {
                resultObj = s.getLocals().searchKey(name);
                if (resultObj != null) break;
            }
        }
        return (resultObj != null) ? resultObj : noObj;
    }

    public static Scope currentScope() {
        return currentScope;
    }

    public static void dump(SymbolTableVisitor stv) {
        System.out.println("=====================SYMBOL TABLE DUMP=========================");
        if (stv == null)
            stv = new DumpSymbolTableVisitor();
        for (Scope s = currentScope; s != null; s = s.getOuter()) {
            s.accept(stv);
        }
        System.out.println(stv.getOutput());
    }

    /**
     * Stampa sadrzaj tabele simbola.
     */
    public static void dump() {
        dump(null);
    }
}