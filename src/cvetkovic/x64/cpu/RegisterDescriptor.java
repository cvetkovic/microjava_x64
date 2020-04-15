package cvetkovic.x64.cpu;

import cvetkovic.x64.SystemV_ABI;

public class RegisterDescriptor extends Descriptor {
    protected String ISA_1_ByteName;
    protected String ISA_4_ByteName;
    protected String ISA_8_ByteName;

    protected int printWidth = -1;

    public RegisterDescriptor(String ISA_1_ByteName, String ISA_4_ByteName, String ISA_8_ByteName) {
        this.ISA_1_ByteName = ISA_1_ByteName;
        this.ISA_4_ByteName = ISA_4_ByteName;
        this.ISA_8_ByteName = ISA_8_ByteName;
    }

    public void setPrintWidth(int width) {
        this.printWidth = width;
    }

    public String getNameBySize(int size) {
        if (size == 1)
            return ISA_1_ByteName;
        else if (size == 4)
            return ISA_4_ByteName;
        else if (size == 8)
            return ISA_8_ByteName;
        else
            throw new RuntimeException("Data width not supported by ISA.");
    }

    /*@Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RegisterDescriptor))
            throw new RuntimeException("Register descriptor cannot be compared");

        RegisterDescriptor descriptor = (RegisterDescriptor)obj;
        return
    }*/

    @Override
    public String toString() {
        if (printWidth == 1) {
            printWidth = -1;
            return ISA_1_ByteName;
        }
        else if (printWidth == 4) {
            printWidth = -1;
            return ISA_4_ByteName;
        }
        else if (printWidth == 8) {
            printWidth = -1;
            return ISA_8_ByteName;
        }

        if (holdsValueOf == null)
            return ISA_8_ByteName;

        int x64Width = SystemV_ABI.getX64VariableSize(holdsValueOf.getType());

        if (printWidth == 1 || x64Width == 1)
            return ISA_1_ByteName;
        else if (printWidth == 4 || x64Width == 4)
            return ISA_4_ByteName;
        else if (printWidth == 8 || x64Width == 8)
            return ISA_8_ByteName;
        else
            throw new RuntimeException("Data width not supported by ISA.");
    }
}
