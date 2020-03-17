package cvetkovic.util;

import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.structure.HashTableDataStructure;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Stack;

public class SymbolTable extends Tab {

    // immutable because of reference comparison later
    public static final Struct BooleanStruct = new Struct(5);
    public static final int AbstractMethodObject = 7;
    private static Stack<ScopeType> scopeKindStack = new Stack<>();
    private static HashMap<Struct, Struct> arrayStructs = new LinkedHashMap();
    private static HashMap<String, Struct> classStructs = new LinkedHashMap();

    public static void InitializeSymbolTable() {
        Tab.init();

        // bool is predefined type, but not natively supported by symbol table library
        Tab.insert(Obj.Type, "bool", BooleanStruct);
    }

    public static Struct getClassStruct(String key) {
        Struct result = classStructs.get(key);
        if (result == null) {
            result = new Struct(Struct.Class);
            classStructs.put(key, result);
        }

        return result;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // ARRAY SYMBOL TABLE STRUCT
    //////////////////////////////////////////////////////////////////////////////////

    public static Struct getArrayStruct(Struct elementaryType) {
        Struct result = arrayStructs.get(elementaryType);
        if (result == null) {
            result = new Struct(Struct.Array, elementaryType);
            arrayStructs.put(elementaryType, result);
        }

        return result;
    }

    /**
     * Open scope operation encapsulation with scope stack in order to know whether
     * too add the variable as global variable or as a field of class
     */
    public static void openScope(ScopeType kind) {
        Tab.openScope();
        scopeKindStack.push(kind);
    }

    //////////////////////////////////////////////////////////////////////////////////
    // CURRENT SCOPE - KIND STACK
    //////////////////////////////////////////////////////////////////////////////////

    public static int getCurrentScopeKind() {
        if (scopeKindStack.empty())
            throw new RuntimeException("Scope stack is empty.");

        return (scopeKindStack.peek() == ScopeType.IN_CLASS) ? Obj.Fld : Obj.Var;
    }

    public static void closeScope() {
        if (scopeKindStack.empty())
            throw new RuntimeException("Cannot close scope because kind scope stack is empty.");

        scopeKindStack.pop();
        Tab.closeScope();
    }

    public static Obj getFormalParameter(Obj method, int number) {
        if (method.getKind() != Obj.Meth && method.getKind() != SymbolTable.AbstractMethodObject)
            return null;

        Iterator<Obj> iterator = method.getLocalSymbols().iterator();
        while (iterator.hasNext()) {
            Obj current = iterator.next();
            if (current.getFpPos() == number)
                return current;
        }

        return null;
    }

    public static Struct getBasetype(Struct derivedType) {
        Iterator<Obj> iterator = derivedType.getMembers().iterator();
        while (iterator.hasNext()) {
            Obj current = iterator.next();

            if (current.getName().equals("extends"))
                return current.getType();
        }

        return null;
    }

    public static boolean assignmentPossible(Struct destination, Struct source) {
        if (destination.assignableTo(source))
            return true;

        if ((destination.getKind() == source.getKind()) && destination.getKind() == Struct.Array) {
            destination = destination.getElemType();
            source = source.getElemType();
        }

        if ((destination.getKind() == Struct.Class || destination.getKind() == Struct.Interface) && source.getKind() == Struct.Class) {
            Struct current = source;

            while (current != null) {
                Iterator<Obj> iterator = current.getMembers().iterator();
                while (iterator.hasNext()) {
                    Obj o = iterator.next();
                    if (o.getKind() == Obj.Type && o.getType().assignableTo(destination))
                        return true;
                }

                current = getBasetype(current);
            }

        }

        return false;
    }

    /**
     * Deep cloning of object node
     *
     * @param object An object to clone
     * @return Deep clone
     */
    private Obj cloneObjectNode(Obj object) {
        Obj result = new Obj(object.getKind(), object.getName(), object.getType());

        result.setAdr(object.getAdr());
        result.setFpPos(object.getFpPos());
        result.setLevel(object.getLevel());
        SymbolDataStructure dataStructure = new HashTableDataStructure();
        for (Obj local : object.getLocalSymbols())
            dataStructure.insertKey(cloneObjectNode(local));
        result.setLocals(dataStructure);

        return result;
    }

    public enum ScopeType {
        IN_CLASS,
        OUTSIDE_CLASS
    }
}