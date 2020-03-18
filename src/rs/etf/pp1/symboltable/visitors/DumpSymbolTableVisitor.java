/**
 *
 */
package rs.etf.pp1.symboltable.visitors;

import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.concepts.Struct;

/**
 * @author nemanja.kojic
 *
 */
public class DumpSymbolTableVisitor extends SymbolTableVisitor {

    protected StringBuilder output = new StringBuilder();
    protected final String indent = "   ";
    protected StringBuilder currentIndent = new StringBuilder();

    protected void nextIndentationLevel() {
        currentIndent.append(indent);
    }

    protected void previousIndentationLevel() {
        if (currentIndent.length() > 0)
            currentIndent.setLength(currentIndent.length() - indent.length());
    }


    /* (non-Javadoc)
     * @see rs.etf.pp1.symboltable.test.SymbolTableVisitor#visitObjNode(symboltable.Obj)
     */
    @Override
    public void visitObjNode(Obj objToVisit) {
        //output.append("[");
        switch (objToVisit.getKind()) {
            case Obj.Con:
                output.append("Con ");
                break;
            case Obj.Var:
                output.append("Var ");
                break;
            case Obj.Type:
                output.append("Type ");
                break;
            case Obj.Meth:
                output.append("Meth ");
                break;
            case Obj.Fld:
                output.append("Fld ");
                break;
            case Obj.Prog:
                output.append("Prog ");
                break;
        }

        output.append(objToVisit.getName());
        output.append(": ");

        if ((Obj.Var == objToVisit.getKind()) && "this".equalsIgnoreCase(objToVisit.getName())) {
        }
        else
            objToVisit.getType().accept(this);

        output.append(", ");
        output.append(objToVisit.getAdr());
        output.append(", ");
        output.append(objToVisit.getLevel() + " ");

        if (objToVisit.getKind() == Obj.Prog || objToVisit.getKind() == Obj.Meth) {
            output.append("\n");
            nextIndentationLevel();
        }


        for (Obj o : objToVisit.getLocalSymbols()) {
            output.append(currentIndent.toString());
            o.accept(this);
            output.append("\n");
        }

        if (objToVisit.getKind() == Obj.Prog || objToVisit.getKind() == Obj.Meth)
            previousIndentationLevel();

        //output.append("]");

    }

    /* (non-Javadoc)
     * @see rs.etf.pp1.symboltable.test.SymbolTableVisitor#visitScopeNode(symboltable.Scope)
     */
    @Override
    public void visitScopeNode(Scope scope) {
        for (Obj o : scope.values()) {
            o.accept(this);
            output.append("\n");
        }
    }

    /* (non-Javadoc)
     * @see rs.etf.pp1.symboltable.test.SymbolTableVisitor#visitStructNode(symboltable.Struct)
     */
    @Override
    public void visitStructNode(Struct structToVisit) {
        switch (structToVisit.getKind()) {
            case Struct.None:
                output.append("notype");
                break;
            case Struct.Int:
                output.append("int");
                break;
            case Struct.Char:
                output.append("char");
                break;
            case Struct.Array:
                output.append("Arr of ");

                switch (structToVisit.getElemType().getKind()) {
                    case Struct.None:
                        output.append("notype");
                        break;
                    case Struct.Int:
                        output.append("int");
                        break;
                    case Struct.Char:
                        output.append("char");
                        break;
                    case Struct.Class:
                        output.append("Class");
                        break;
                }
                break;
            case Struct.Class:
                output.append("Class [");
                for (Obj obj : structToVisit.getMembers()) {
                    obj.accept(this);
                }
                output.append("]");
                break;
        }

    }

    public String getOutput() {
        return output.toString();
    }


}
