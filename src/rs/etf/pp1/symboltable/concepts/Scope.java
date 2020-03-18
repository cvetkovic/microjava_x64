package rs.etf.pp1.symboltable.concepts;

import rs.etf.pp1.symboltable.factory.SymbolTableFactory;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

import java.util.Collection;
import java.util.Collections;


/**
 * Opseg u MikroJava tabeli simbola
 *
 * @author ETF
 */
public class Scope {
    // referenca na okruzujuci opseg
    private Scope outer;

    // stablo (tabela simbola) za ovaj opseg
    private SymbolDataStructure locals;

    // broj simbola deklarisanih u opsegu
    private int nVars = 0;


    public Scope(Scope outer) {
        this.outer = outer;
    }

    public boolean addToLocals(Obj o) {
        if (locals == null)
            locals = SymbolTableFactory.instance().createSymbolTableDataStructure();

        boolean isOK = locals.insertKey(o);
        if (isOK && (o.getKind() == Obj.Var || o.getKind() == Obj.Fld)) {
            o.setAdr(nVars++);
        }
        return isOK;
    }

    public Obj findSymbol(String objName) {
        return (locals != null) ? locals.searchKey(objName) : null;
    }

    public Scope getOuter() {
        return outer;
    }

    public int getnVars() {
        return nVars;
    }

    public SymbolDataStructure getLocals() {
        return locals;
    }

    public Collection<Obj> values() {
        return (locals != null) ? locals.symbols() : Collections.emptyList();
    }

    public void accept(SymbolTableVisitor stv) {
        stv.visitScopeNode(this);
    }
}
