package cvetkovic.ir.quadruple;

public class QuadrupleIOVar extends QuadrupleVariable {
    protected DataWidth width;

    public QuadrupleIOVar(DataWidth width) {
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

    public enum DataWidth {
        BIT,        // 8-bit -> minimum allocation size
        BYTE,       // 8-bit
        WORD        // 32-bit
    }
}
