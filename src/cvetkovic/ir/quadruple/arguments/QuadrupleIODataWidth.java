package cvetkovic.ir.quadruple.arguments;

import cvetkovic.structures.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Struct;

public class QuadrupleIODataWidth extends QuadrupleVariable {
    protected DataWidth width;

    public QuadrupleIODataWidth(DataWidth width) {
        this.width = width;
    }

    public DataWidth getWidth() {
        return width;
    }

    @Override
    public String toString() {
        switch (width)
        {
            case BIT:
                return "%b";
            case BYTE:
                return "%c";
            case WORD:
                return "%d";

            default:
                throw new RuntimeException("Not supported I/O data width.");
        }
    }

    public Struct ioVarToStruct() {
        switch (width)
        {
            case BIT:
                return SymbolTable.BooleanStruct;
            case BYTE:
                return SymbolTable.charType;
            case WORD:
                return SymbolTable.intType;

            default:
                throw new RuntimeException("Not supported I/O data width.");
        }
    }

    public enum DataWidth {
        BIT,        // 8-bit -> minimum allocation size
        BYTE,       // 8-bit
        WORD        // 32-bit
    }
}
