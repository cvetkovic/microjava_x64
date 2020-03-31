package cvetkovic.semantics;

public class ForStatementActions {
    private static int forStatementDepth = 0;

    public static void ForBegin() {
        forStatementDepth++;
    }

    public static boolean InForLoop() {
        return forStatementDepth != 0;
    }

    public static void ForEnd() {
        forStatementDepth--;
    }
}
