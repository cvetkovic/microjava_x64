package rs.etf.pp1.symboltable.factory;

import rs.etf.pp1.symboltable.structure.HashTableDataStructure;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;

public class SymbolTableFactory {

    private static SymbolTableFactory inst = new SymbolTableFactory();

    public static SymbolTableFactory instance() {
        return inst;
    }

    public SymbolDataStructure createSymbolTableDataStructure() {
        return new HashTableDataStructure();
    }

}
