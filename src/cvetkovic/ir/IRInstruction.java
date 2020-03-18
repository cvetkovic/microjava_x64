package cvetkovic.ir;

public enum IRInstruction {
    ADD,    // addition
    SUB,    // subtraction
    MUL,    // multiplication
    DIV,    // division (x86 -> IDIV with REM in DX)
    REM,    // division remainder
    NEG,    // negation

    COPY,   // copy

    SCANF,   // scan from stdin
    PRINTF,  // print to stdout

    MALLOC,  // heap memory allocation

    PARAMETER,      // push parameter
    CALL,           // function invocation
    RETURN,         // return from function

    ARRAY_LOAD,     // array laod
    ARRAY_STORE,    // array store

    JMP,    // jump unconditionally
    JL,     // jump if less
    JLE,    // jump if less or equal
    JG,     // jump if greater
    JGE,    // jump if greater or equal
    JE,     // jump if equal
    JNE     // jump if not equal
}
