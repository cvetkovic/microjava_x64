package cvetkovic.x64;

import rs.etf.pp1.symboltable.concepts.Struct;

public class SystemV_ABI {
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

    public static String getPtrSpecifier(Struct type) {
        if (type.getKind() == Struct.Bool)
            return "BYTE PTR";
        else if (type.getKind() == Struct.Char)
            return "BYTE PTR";
        else if (type.getKind() == Struct.Int)
            return "DWORD PTR";
        else if (type.getKind() == Struct.Array || type.getKind() == Struct.Class)
            return "QWORD PTR"; // sizeof(pointer) in x86-64 is 8 bytes
        else
            throw new RuntimeException("Data type not supported for compilation into x86-64 machine code.");
    }

    /**
     * Compliant with GNU assembly
     * @param size
     * @return
     */
    public static String getAssemblyDirectiveForAllocation(int size) {
        switch (size)
        {
            case 1:
                return ".byte";
            case 4:
                return ".long";
            case 8:
                return ".quad";

            default:
                throw new RuntimeException("Data type not supported for compilation into x86-64 machine code.");
        }
    }

    /**
     * Finds next value that is divisible by 16 because all stack frames have to
     * be aligned on a 16 byte boundary
     * @param num
     * @return
     */
    public static int alignTo16(int num) {
        return ((num >> 4) + 1) << 4;
    }
}
