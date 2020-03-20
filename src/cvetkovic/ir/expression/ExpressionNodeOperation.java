package cvetkovic.ir.expression;

public enum ExpressionNodeOperation {
    // binary operators
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    MODULO,
    ASSIGNMENT,

    // unary operators
    UNARY_MINUS,

    // leaf node
    VARIABLE
}