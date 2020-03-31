package cvetkovic.semantics;

/**
 * Class that represents single IF/ELSE statement.
 */
public class ConditionalStatement {

    /**
     * Currently parsing what branch
     */
    public OperatingBranch currentBranch = OperatingBranch.IF;
    /**
     * If has ELSE or not
     */
    public ConditionalType statementType = ConditionalType.IF_STATEMENT;
    /**
     * RETURN statements encountered for
     */
    public boolean[] returnEncountered = new boolean[2];

    /**
     * Represent what branch of statements is currently parsed
     */
    public enum OperatingBranch {
        IF(0),
        ELSE(1);

        private int val;

        OperatingBranch(int val) {
            this.val = val;
        }

        public int getValue() {
            return val;
        }
    }

    /**
     * Represents whether the statement has optional ELSE statement
     */
    public enum ConditionalType {
        IF_STATEMENT(0),
        IF_ELSE_STATEMENT(1);

        private int val;

        ConditionalType(int val) {
            this.val = val;
        }

        public int getValue() {
            return val;
        }
    }
}
