package cvetkovic.ir;

import cvetkovic.ir.expression.ExpressionNodeOperation;
import cvetkovic.parser.ast.*;

public enum IRInstruction {
    ADD,    // addition
    SUB,    // subtraction
    MUL,    // multiplication
    DIV,    // division (x86 -> IDIV with REM in DX)
    REM,    // division remainder
    NEG,    // negation

    LOAD,   // load from memory
    STORE,  // store to memory

    ENTER,  // allocate stack frame     => enter procedure
    LEAVE,  // deallocate stack frame   => exit procedure

    SCANF,  // scan from stdin
    PRINTF, // print to stdout

    MALLOC, // heap memory allocation

    PARAM,  // push parameter
    CALL,   // non-class static method invocation
    INVOKE_VIRTUAL, // class method invocation
    RETURN, // return from function

    ALOAD,  // array load
    ASTORE, // array store

    GET_PTR,// get pointer

    CMP,    // compare instruction
    JMP,    // jump unconditionally
    JL,     // jump if less
    JLE,    // jump if less or equal
    JG,     // jump if greater
    JGE,    // jump if greater or equal
    JE,     // jump if equal
    JNE,    // jump if not equal

    STORE_PHI,    // phi function (SSA)

    GEN_LABEL;      // generate label

    public static boolean isJumpInstruction(IRInstruction instruction) {
        switch (instruction) {
            case JMP:
            case JL:
            case JLE:
            case JG:
            case JGE:
            case JE:
            case JNE:
                return true;

            default:
                return false;
        }
    }

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
                return STORE;
            case UNARY_MINUS:
                return NEG;

            default:
                throw new RuntimeException("Expression node operation cannot be mapped into an intermediate language instruction.");
        }
    }

    public static IRInstruction negateJumpInstruction(IRInstruction instruction) {
        switch (instruction) {
            case JE:
                return JNE;
            case JNE:
                return JE;
            case JL:
                return JGE;
            case JLE:
                return JG;
            case JG:
                return JLE;
            case JGE:
                return JL;

            default:
                throw new RuntimeException("Provided argument is not a jump instruction and cannot be negated.");
        }
    }

    public static IRInstruction determineJumpInstruction(Relop relop) {
        if (relop instanceof OperatorEqual)
            return IRInstruction.JE;
        else if (relop instanceof OperatorNotEqual)
            return IRInstruction.JNE;
        else if (relop instanceof OperatorGreater)
            return IRInstruction.JG;
        else if (relop instanceof OperatorGreaterOrEqual)
            return IRInstruction.JGE;
        else if (relop instanceof OperatorLess)
            return IRInstruction.JL;
        else if (relop instanceof OperatorLessOrEqual)
            return IRInstruction.JLE;

        throw new RuntimeException("Not allowed relation operator.");
    }

    public static boolean isConditionalJumpInstruction(IRInstruction instruction) {
        switch (instruction) {
            case JL:
            case JLE:
            case JG:
            case JGE:
            case JE:
            case JNE:
                return true;

            default:
                return false;
        }
    }
}
