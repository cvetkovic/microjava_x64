package cvetkovic.ir;

import cvetkovic.ir.expression.ExpressionNodeOperation;

public enum IRInstruction {
    ADD,    // addition
    SUB,    // subtraction
    MUL,    // multiplication
    DIV,    // division (x86 -> IDIV with REM in DX)
    REM,    // division remainder
    NEG,    // negation

    COPY,   // copy

    ENTER,  // allocate stack frame     => enter procedure
    LEAVE,  // deallocate stack frame   => exit procedure

    SCANF,   // scan from stdin
    PRINTF,  // print to stdout

    MALLOC,  // heap memory allocation

    PARAM,          // push parameter
    CALL,           // function invocation
    RETURN,         // return from function

    ALOAD,     // array laod
    ASTORE,    // array store

    JMP,    // jump unconditionally
    JL,     // jump if less
    JLE,    // jump if less or equal
    JG,     // jump if greater
    JGE,    // jump if greater or equal
    JE,     // jump if equal
    JNE;    // jump if not equal

    public static IRInstruction dagToQuadrupleInstruction(ExpressionNodeOperation operation) {
        switch (operation) {
            case ADDITION:
                return ADD;
            case SUBTRACTION:
                return SUB;
            case MULTIPLICATION:
                return MUL;
            case DIVISION:
                return DIV;
            case MODULO:
                return REM;

            case ARRAY_LOAD:
                return ALOAD;
            case ARRAY_STORE:
                return ASTORE;

            case ASSIGNMENT:
                return COPY;
            case UNARY_MINUS:
                return NEG;

            default:
                throw new RuntimeException("Expression node operation cannot be mapped into an intermediate language instruction.");
        }
    }
}
