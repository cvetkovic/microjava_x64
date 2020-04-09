package cvetkovic.ir.optimizations.local;

import cvetkovic.ir.IRInstruction;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.ir.quadruple.QuadrupleObjVar;
import cvetkovic.structures.SymbolTable;
import rs.etf.pp1.symboltable.concepts.Obj;

public class AlgebraicIdentities {

    /**
     * Tries to do a algebraic simplification over quadruple and if succeeds returns non null value
     *
     * @param instruction Instruction quadruple
     * @param obj1        Redundant parameter, but needed for simplicity
     * @param obj2        Redundant parameter, but needed for simplicity
     * @param result      Redundant parameter, but needed for simplicity
     * @return Null if code removal is not needed or optimization is not applicable for provided instruction
     */
    public static Obj simplifyAlgebra(Quadruple instruction, Obj obj1, Obj obj2, Obj result) {
        if (obj1 == null || obj2 == null || result == null)
            return null;
        else if (instruction.getArg1() instanceof QuadrupleObjVar && ((QuadrupleObjVar) instruction.getArg1()).getObj() == obj1 ||
                instruction.getArg2() instanceof QuadrupleObjVar && ((QuadrupleObjVar) instruction.getArg2()).getObj() == obj2 ||
                instruction.getResult() instanceof QuadrupleObjVar && ((QuadrupleObjVar) instruction.getResult()).getObj() == result)
            throw new RuntimeException("Provided parameter does not match the quadruple argument.");

        switch (instruction.getInstruction()) {
            case ADD:
                if ((obj1.getKind() == Obj.Con && obj1.getAdr() == 0 && obj2.getKind() == Obj.Var) ||
                        (obj2.getKind() == Obj.Con && obj2.getAdr() == 0 && obj1.getKind() == Obj.Var)) {
                    // instruction: ADD var con || ADD con var
                    return (obj1.getKind() == Obj.Con ? obj2 : obj1);
                }
                else
                    return null;
            case SUB:
                if (obj1.getKind() == Obj.Var && obj2.getKind() == Obj.Con && obj2.getAdr() == 0) {
                    // instruction: SUB var 0
                    return obj1;
                }
                else if (obj1.getKind() == Obj.Var && obj2.getKind() == Obj.Var && obj1 == obj2) {
                    // instruction: SUB var var
                    Obj tmp = new Obj(Obj.Con, "const", SymbolTable.intType);
                    tmp.setAdr(0);
                    return tmp;
                }
                else
                    return null;
            case MUL:
                if ((obj1.getKind() == Obj.Con && obj1.getAdr() == 2 && obj2.getKind() == Obj.Var) ||
                        (obj2.getKind() == Obj.Con && obj2.getAdr() == 2 && obj1.getKind() == Obj.Var)) {
                    // instruction: MUL var 2 || MUL 2 var
                    instruction.setInstruction(IRInstruction.ADD);
                    if (obj1.getKind() == Obj.Con)
                        instruction.setArg1(new QuadrupleObjVar(obj2));
                    else
                        instruction.setArg2(new QuadrupleObjVar(obj1));
                    return null;
                }
                else if ((obj1.getKind() == Obj.Con && obj1.getAdr() == 1 && obj2.getKind() == Obj.Var) ||
                        (obj2.getKind() == Obj.Con && obj2.getAdr() == 1 && obj1.getKind() == Obj.Var)) {
                    // instruction: MUL 1 var || MUL var 1
                    return (obj1.getKind() == Obj.Con ? obj2 : obj1);
                }
                else if ((obj1.getKind() == Obj.Con && obj1.getAdr() == 0 && obj2.getKind() == Obj.Var) ||
                        (obj2.getKind() == Obj.Con && obj2.getAdr() == 0 && obj1.getKind() == Obj.Var)) {
                    // instruction: MUL 0 var || MUL var 0
                    Obj tmp = new Obj(Obj.Con, "const", SymbolTable.intType);
                    tmp.setAdr(0);
                    return tmp;
                }
                else
                    return null;
            case DIV:
                if (obj1.getKind() == Obj.Var && obj2.getKind() == Obj.Con && obj2.getAdr() == 1) {
                    // instruction: DIV var 1
                    return obj1;
                }
                else if (obj1.getKind() == Obj.Var && obj2.getKind() == Obj.Var && obj1 == obj2) {
                    // instruction: DIV var var
                    // TODO: maybe unsafe because of division with zero
                    Obj tmp = new Obj(Obj.Con, "const", SymbolTable.intType);
                    tmp.setAdr(1);
                    return tmp;
                }
                else
                    return null;
            default:
                return null;
        }
    }
}