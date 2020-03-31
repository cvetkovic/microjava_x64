package cvetkovic.semantics;

import java.util.Stack;

public class ConditionalStatementActions {
    private static Stack<ConditionalStatement> stack = new Stack<>();
    private static boolean allPathsReturned = false;
    private static boolean inMethod = false;

    /**
     * This method should be called upon encountering new method declaration
     */
    public static void MethodBegin() {
        stack = new Stack<>();
        allPathsReturned = false;

        if (inMethod)
            throw new RuntimeException("Method nesting is not allowed.");
        else
            inMethod = true;
    }

    /**
     * This method should be called on closing method declaration
     */
    public static void MethodEnd() {
        if (!inMethod)
            throw new RuntimeException("Method end cannot be called before 'MethodBegin()'.");
        else
            inMethod = false;
    }

    public static boolean MethodReturnsOnAllPaths() {
        return allPathsReturned;
    }

    /**
     * This method should be invoked every time the RETURN statement is encountered
     */
    public static void ResolveReturnStatement() {
        if (stack.empty())
            allPathsReturned = true;
        else if (stack.peek().currentBranch == ConditionalStatement.OperatingBranch.IF)
            stack.peek().returnEncountered[ConditionalStatement.OperatingBranch.IF.getValue()] = true;
        else if (stack.peek().currentBranch == ConditionalStatement.OperatingBranch.ELSE)
            stack.peek().returnEncountered[ConditionalStatement.OperatingBranch.ELSE.getValue()] = true;
    }

    /**
     * This method should be invoked every time the IF statement is encountered
     */
    public static void IfBegin() {
        stack.push(new ConditionalStatement());
    }

    /**
     * This method should be invoked every time the IF statement is encountered
     */
    public static void ElseBegin() {
        // change current branch to else
        stack.peek().currentBranch = ConditionalStatement.OperatingBranch.ELSE;
        // change statement type to IF-ELSE because ELSE was detected
        stack.peek().statementType = ConditionalStatement.ConditionalType.IF_ELSE_STATEMENT;
    }

    public static void IfEnd() {
        ConditionalStatement statement = stack.pop();

        if (statement.statementType == ConditionalStatement.ConditionalType.IF_STATEMENT &&
                statement.returnEncountered[ConditionalStatement.OperatingBranch.IF.getValue()])
            ResolveReturnStatement();
        else if (statement.statementType == ConditionalStatement.ConditionalType.IF_ELSE_STATEMENT &&
                statement.returnEncountered[ConditionalStatement.OperatingBranch.IF.getValue()] &&
                statement.returnEncountered[ConditionalStatement.OperatingBranch.ELSE.getValue()])
            ResolveReturnStatement();
    }
}
