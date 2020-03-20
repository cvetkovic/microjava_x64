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
        if (width == DataWidth.BYTE)
            return "%c";
        else
            return "%d";
    }

    public enum DataWidth {
        BYTE,       // 8-bit
        WORD        // 32-bit
    }
}
