package cvetkovic.x64;

import rs.etf.pp1.symboltable.concepts.Struct;

public class DataStructures {
    public static int getX64VariableSize(Struct type) {
        if (type.getKind() == Struct.Bool)
            return 1;
        else if (type.getKind() == Struct.Char)
            return 1;
        else if (type.getKind() == Struct.Int)
            return 4;
        else if (type.getKind() == Struct.Array || type.getKind() == Struct.Class)
            return 8; // sizeof(pointer) in x86-64 is 8 bytes
        else
            throw new RuntimeException("Data type not supported for compilation into x86-64 machine code.");
    }
}