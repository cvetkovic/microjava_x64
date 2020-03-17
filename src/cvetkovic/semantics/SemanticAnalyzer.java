package cvetkovic.semantics;

import cvetkovic.misc.ConditionalStatementActions;
import cvetkovic.misc.ForStatementActions;
import cvetkovic.parser.ast.*;
import cvetkovic.util.SymbolTable;
import org.apache.log4j.Logger;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

import java.util.*;

public class SemanticAnalyzer extends VisitorAdaptor {

    //////////////////////////////////////////////////////////////////////////////////
    // LOGGER VARIABLES
    //////////////////////////////////////////////////////////////////////////////////

    private static final int GLOBAL_VARIABLES_MAX = 65536;

    //////////////////////////////////////////////////////////////////////////////////
    // SEMANTIC ANALYZER VARIABLES
    //////////////////////////////////////////////////////////////////////////////////
    private static final int LOCAL_VARIABLES_MAX = 256;
    private static final int CLASS_FIELDS_MAX = 65536;
    private Logger log = Logger.getLogger(getClass());
    private DataType currentDataType = null;
    private boolean errorDetected = false;
    private int numberOfGlobalVariables = 0;

    //////////////////////////////////////////////////////////////////////////////////
    // SHARED DATA STRUCTURES BETWEEN SEMANTIC ANALYZER AND CODE GENERATOR
    //////////////////////////////////////////////////////////////////////////////////
    private SharedData sharedData;
    private HashMap<String, String> classInstances = new LinkedHashMap<>();

    //////////////////////////////////////////////////////////////////////////////////
    // AUXILIARY INTERNAL STRUCTURES
    //////////////////////////////////////////////////////////////////////////////////
    private String currentMethodName = "";

    //////////////////////////////////////////////////////////////////////////////////
    // ERROR REPORTING METHODS
    //////////////////////////////////////////////////////////////////////////////////
    private Obj designatorAccessObject = null;

    //////////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    //////////////////////////////////////////////////////////////////////////////////
    private String currentClassName;
    private Obj currentClass;
    private Obj currentClassExtends;

    //////////////////////////////////////////////////////////////////////////////////
    // PROGRAM DECLARATION
    //////////////////////////////////////////////////////////////////////////////////
    private Struct abstractMethodReturnType;
    private Struct currentMethodReturnType;

    //////////////////////////////////////////////////////////////////////////////////
    // DATA TYPES
    //////////////////////////////////////////////////////////////////////////////////
    private boolean mainMethodFound = false;
    private Obj currentMethod;
    private HashMap<String, List<Obj>> implementedAbstractMethods = new LinkedHashMap<>();
    private Stack<Obj> currentFunctionCall = new Stack<>();

    //////////////////////////////////////////////////////////////////////////////////
    // CONSTANT DECLARATION
    //////////////////////////////////////////////////////////////////////////////////
    private Stack<Integer> numberOfParametersWithValue = new Stack<>();

    //////////////////////////////////////////////////////////////////////////////////
    // VARIABLE DECLARATION
    //////////////////////////////////////////////////////////////////////////////////
    private Stack<Integer> currentParameterCheckIndex = new Stack<>();

    //////////////////////////////////////////////////////////////////////////////////
    // DESIGNATOR - FIELD MEMBER AND ARRAY ELEMENT ACCESS
    //////////////////////////////////////////////////////////////////////////////////

    public SemanticAnalyzer(SharedData sharedData) {
        SymbolTable.InitializeSymbolTable();

        this.sharedData = sharedData;
    }

    private void throwError(Integer line, String message) {
        errorDetected = true;

        if (line == null)
            log.error("Error: " + message);
        else
            log.error("Error at line " + line + ": " + message);
    }

    public boolean isErrorDetected() {
        return errorDetected;
    }

    public int getNumberOfGlobalVariables() {
        return numberOfGlobalVariables;
    }

    // PROGRAM ProgramName ProgramElementsDeclList LEFT_CURLY_BRACKET ProgramMethodsDeclList RIGHT_CURLY_BRACKET
    @Override
    public void visit(Program Program) {
        SymbolTable.chainLocalSymbols(Program.getProgramName().obj);

        if (!mainMethodFound)
            throwError(Program.getLine(), "Method 'void main()' is not defined in current program.");

        // checking constrains on global variables
        numberOfGlobalVariables = SymbolTable.currentScope.getnVars();
        if (numberOfGlobalVariables > GLOBAL_VARIABLES_MAX)
            throwError(Program.getLine(), "Function is not allowed to have more than " + GLOBAL_VARIABLES_MAX + " global variables.");

        // closing scope here must not be done because code generator could not search for symbols then
        sharedData.programName = Program.getProgramName().getProgramName();
        sharedData.numberOfMethodGlobalParameters = numberOfGlobalVariables;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // EXPRESSION
    //////////////////////////////////////////////////////////////////////////////////

    // IDENT:programName
    @Override
    public void visit(ProgramName ProgramName) {
        ProgramName.obj = SymbolTable.insert(Obj.Prog, ProgramName.getProgramName(), SymbolTable.noType);
        SymbolTable.openScope(SymbolTable.ScopeType.OUTSIDE_CLASS);
    }

    /**
     * Sets currentDataType to declared type
     */
    @Override
    public void visit(DataType DataType) {
        Obj object = SymbolTable.find(DataType.getTypeIdent());

        if (object != SymbolTable.noObj && object != null) {
            if (object.getKind() == Obj.Type) {
                DataType.struct = object.getType();
                currentDataType = DataType;
            }
            else {
                throwError(DataType.getLine(), "Name '" + DataType.getTypeIdent() + "' does not represent a type.");
                currentDataType = null;
            }
        }
        else {
            throwError(DataType.getLine(), "Data type '" + DataType.getTypeIdent() + "' has not been defined in the symbol table.");
            currentDataType = null;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // TERM
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(BooleanConst BooleanConst) {
        BooleanConst.struct = SymbolTable.BooleanStruct;
    }

    @Override
    public void visit(CharacterConst CharacterConst) {
        CharacterConst.struct = SymbolTable.charType;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // TERM
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(NumericalConst NumericalConst) {
        NumericalConst.struct = SymbolTable.intType;
    }

    /**
     * Constant declaration
     */
    @Override
    public void visit(SingleConst SingleConst) {
        if (currentDataType == null) {
            throwError(SingleConst.getLine(), "Data type not specified for constant declaration directive.");
            return;
        }

        String constName = SingleConst.getConstName();
        ConstValue constValue = SingleConst.getConstValue();

        // variable data type matching
        if (constValue.struct.assignableTo(currentDataType.struct)) {
            Obj obj = SymbolTable.currentScope.findSymbol(constName);

            if (obj == SymbolTable.noObj || obj == null) {
                Obj newObj = SymbolTable.insert(Obj.Con, constName, constValue.struct);

                // setting value of const into a symbol table
                if (constValue instanceof BooleanConst)
                    newObj.setAdr(((BooleanConst) constValue).getConstBoolValue() ? 1 : 0);
                else if (constValue instanceof CharacterConst)
                    newObj.setAdr(((CharacterConst) constValue).getConstCharValue());
                else if (constValue instanceof NumericalConst)
                    newObj.setAdr(((NumericalConst) constValue).getConstNumValue());
                else
                    throw new RuntimeException("Not supported data type for constant declaration.");

                classInstances.put(constName, currentDataType.getTypeIdent());
            }
            else
                throwError(SingleConst.getLine(), "Symbolic constant with name '" + constName + "' redefinition error.");
        }
        else
            throwError(SingleConst.getLine(), "Constants cannot be of other type other than integer, character or boolean.");
    }

    /**
     * Variable declaration
     */
    @Override
    public void visit(SingleVariableDeclaration SingleVariableDeclaration) {
        String variableName = SingleVariableDeclaration.getVariableName();

        if (currentDataType == null) {
            throwError(SingleVariableDeclaration.getLine(), "Cannot create variable '" + variableName + "' from non-existing data type.");
            return;
        }

        Obj obj = SymbolTable.currentScope.findSymbol(variableName);

        if (obj == SymbolTable.noObj || obj == null) {
            if (SingleVariableDeclaration.getSingleVarArray() instanceof SingleVarNoArray)                // non-array declaration
                SymbolTable.insert(SymbolTable.getCurrentScopeKind(), SingleVariableDeclaration.getVariableName(), currentDataType.struct);
            else                                                                                        // array declaration
                SymbolTable.insert(SymbolTable.getCurrentScopeKind(), SingleVariableDeclaration.getVariableName(), SymbolTable.getArrayStruct(currentDataType.struct));

            if (currentDataType.struct.getKind() == Struct.Class)
                classInstances.put(variableName, currentDataType.getTypeIdent());
        }
        else
            throwError(SingleVariableDeclaration.getLine(), "Variable with name '" + variableName + "' redefinition error.");
    }

    /**
     * If this is a global variable access tree will end here and no additional check is needed
     */
    @Override
    public void visit(DesignatorRoot DesignatorRoot) {
        Obj object = SymbolTable.find(DesignatorRoot.getDesignatorNameSingle());

        // uncomment for DEBUG
        // Object s = SymbolTable.currentScope;

        if (object != SymbolTable.noObj && object != null) {
            DesignatorRoot.obj = object;
            designatorAccessObject = object;
        }
        else
            throwError(DesignatorRoot.getLine(), "Variable '" + DesignatorRoot.getDesignatorNameSingle() + "' is not declared in the current context.");
    }

    /**
     * Class field access, so checks have to be done in order that class contains field with proper name
     * and of proper type
     */
    @Override
    public void visit(DesignatorNonArrayAccess DesignatorNonArrayAccess) {
        Designator parentDesignator = DesignatorNonArrayAccess.getDesignator();

        String parentName;
        if (parentDesignator instanceof DesignatorRoot)
            parentName = ((DesignatorRoot) parentDesignator).getDesignatorNameSingle();
        else if (parentDesignator instanceof DesignatorNonArrayAccess)
            parentName = ((DesignatorNonArrayAccess) parentDesignator).getDesignatorName();
        else
            parentName = "";

        if (parentDesignator.obj.getKind() == Obj.Var ||    // global variable
                parentDesignator.obj.getKind() == Obj.Fld ||    // class field
                parentDesignator.obj.getKind() == Obj.Elem ||   // array element
                parentDesignator.obj.getKind() == Obj.Type)     // class
        {
            if (parentDesignator.obj.getType().getKind() == Struct.Class ||
                    parentDesignator.obj.getType().getKind() == Struct.Interface) {
                Iterator<Obj> iterator = parentDesignator.obj.getType().getMembers().iterator();

                boolean found = false;
                while (iterator.hasNext()) {
                    Obj currentObject = iterator.next();
                    if (currentObject.getName().equals(DesignatorNonArrayAccess.getDesignatorName())) {
                        if (currentObject.getKind() == Obj.Meth ||
                                currentObject.getKind() == SymbolTable.AbstractMethodObject ||
                                currentObject.getKind() == Obj.Fld) {
                            designatorAccessObject = DesignatorNonArrayAccess.obj = currentObject;
                            found = true;
                            break;
                        }
                        else {
                            throwError(DesignatorNonArrayAccess.getLine(), "Object '" + DesignatorNonArrayAccess.getDesignatorName() + "' is not method nor field in '" + parentName + "'.");
                        }
                    }
                }

                if (!found) {
                    throwError(DesignatorNonArrayAccess.getLine(), "Object '" + DesignatorNonArrayAccess.getDesignatorName() + "' not found in class '" + parentName + "'.");
                }
            }
            else
                throwError(DesignatorNonArrayAccess.getLine(), "Object '" + parentName + "' is not a class to be accessed.");
        }
        else
            throwError(DesignatorNonArrayAccess.getLine(), "Object '" + parentName + "' has to be variable, class field, element of array or class in order to be able to access it.");
    }

    @Override
    public void visit(DesignatorArrayAccess DesignatorArrayAccess) {
        String parentName;
        if (DesignatorArrayAccess.getDesignator() instanceof DesignatorRoot)
            parentName = ((DesignatorRoot) DesignatorArrayAccess.getDesignator()).getDesignatorNameSingle();
        else
            parentName = ((DesignatorNonArrayAccess) DesignatorArrayAccess.getDesignator()).getDesignatorName();

        if (DesignatorArrayAccess.getDesignator().obj.getType().getKind() != Struct.Array)
            throwError(DesignatorArrayAccess.getLine(), "Identifier '" + parentName + "' is not declared as array, therefore it cannot be accessed.");
        else if (DesignatorArrayAccess.getExpr().struct != SymbolTable.intType)
            throwError(DesignatorArrayAccess.getLine(), "Indexer of '" + parentName + "' has to be of type integer.");
        else
            DesignatorArrayAccess.obj = new Obj(Obj.Elem, "ArrayAccess", DesignatorArrayAccess.getDesignator().obj.getType().getElemType());
    }

    //////////////////////////////////////////////////////////////////////////////////
    // CLASS
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(BinaryExpression BinaryExpression) {
        // combined arithmetic operators not supported by grammar currently

        if (BinaryExpression.getExpr().struct == BinaryExpression.getTerm().struct &&
                BinaryExpression.getExpr().struct == SymbolTable.intType)
            BinaryExpression.struct = SymbolTable.intType;
        else
            throwError(BinaryExpression.getLine(), "Addition and subtraction of expressions have to be done on top of integer arguments.");
    }

    @Override
    public void visit(UnaryExpression UnaryExpression) {
        if (UnaryExpression.getExprNegative() instanceof ExpressionNegative && UnaryExpression.getTerm().struct != SymbolTable.intType)
            throwError(UnaryExpression.getLine(), "Negative sign can be applied only to an integer.");
        else
            UnaryExpression.struct = UnaryExpression.getTerm().struct;
    }

    @Override
    public void visit(TermMultiple TermMultiple) {
        // combined arithmetic operators not supported by grammar currently

        if (TermMultiple.getFactor().struct == TermMultiple.getTerm().struct &&
                TermMultiple.getFactor().struct == SymbolTable.intType)
            TermMultiple.struct = SymbolTable.intType;
        else
            throwError(TermMultiple.getLine(), "Multiplication and division of expressions have to be done on top of integer arguments.");
    }

    @Override
    public void visit(TermSingle TermSingle) {
        TermSingle.struct = TermSingle.getFactor().struct;
    }

    @Override
    public void visit(FactorFunctionCall FactorFunctionCall) {
        Designator designator = FactorFunctionCall.getDesignator();

        // symbol not found, so return the method
        if (errorDetected && designator.obj == null)
            return;

        String functionName = "";
        if (designator instanceof DesignatorRoot)
            functionName = ((DesignatorRoot) designator).getDesignatorNameSingle();
        else if (designator instanceof DesignatorNonArrayAccess)
            functionName = ((DesignatorNonArrayAccess) designator).getDesignatorName();

        if ((designator.obj.getKind() == Obj.Meth || designator.obj.getKind() == SymbolTable.AbstractMethodObject) && designator.obj.getType() != SymbolTable.noType)                // method and has return value
            FactorFunctionCall.struct = designator.obj.getType();
        else if (FactorFunctionCall.getFactorFunctionCallParameters() instanceof NoFactorFunctionCallParameter)
            FactorFunctionCall.struct = designator.obj.getType();
        else if ((designator.obj.getKind() == Obj.Meth || designator.obj.getKind() == SymbolTable.AbstractMethodObject) && designator.obj.getType() == SymbolTable.noType)        // method and no return value
            throwError(FactorFunctionCall.getLine(), "'" + functionName + "' doesn't return value (i.e. is void).");
        else if (!(FactorFunctionCall.getFactorFunctionCallParameters() instanceof NoFactorFunctionCallParameter))                                                                                                    // not a method and not a 'for' loop condition
            throwError(FactorFunctionCall.getLine(), "'" + functionName + "' is not a function and cannot be invoked.");

        if (designator.obj.getKind() != Obj.Meth)
            return;

        if (currentFunctionCall.size() != 0) {
            if (currentFunctionCall.peek().getLevel() != numberOfParametersWithValue.peek())
                throwError(FactorFunctionCall.getLine(), "Number of invocation parameters of function call doesn't match the function formal definition.");

            currentParameterCheckIndex.pop();
            numberOfParametersWithValue.pop();
            currentFunctionCall.pop();
        }
    }

    @Override
    public void visit(FactorNumericalConst FactorNumericalConst) {
        FactorNumericalConst.struct = SymbolTable.intType;
    }

    @Override
    public void visit(FactorCharConst FactorCharConst) {
        FactorCharConst.struct = SymbolTable.charType;
    }

    @Override
    public void visit(FactorBoolConst FactorBoolConst) {
        FactorBoolConst.struct = SymbolTable.BooleanStruct;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // ABSTRACT CLASS
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(FactorArrayDeclaration FactorArrayDeclaration) {
        if (FactorArrayDeclaration.getFactorArrayDecl() instanceof ArrayDeclaration) {
            if (((ArrayDeclaration) FactorArrayDeclaration.getFactorArrayDecl()).getExpr().struct != SymbolTable.intType)
                throwError(FactorArrayDeclaration.getLine(), "Operator 'new' used for array creation expects integer as number of array elements.");
            else
                FactorArrayDeclaration.struct = SymbolTable.getArrayStruct(FactorArrayDeclaration.getType().struct);
        }
        else {
            if (FactorArrayDeclaration.getType().struct.getKind() == Struct.Class)
                FactorArrayDeclaration.struct = FactorArrayDeclaration.getType().struct;
            else if (FactorArrayDeclaration.getType().struct.getKind() == Struct.Interface)
                throwError(FactorArrayDeclaration.getLine(), "Operator 'new' cannot be used with abstract classes.");
            else
                throwError(FactorArrayDeclaration.getLine(), "Operator 'new' can only be used with classes.");
        }
    }

    @Override
    public void visit(FactorExpressionInBrackets FactorExpressionInBrackets) {
        FactorExpressionInBrackets.struct = FactorExpressionInBrackets.getExpr().struct;
    }

    @Override
    public void visit(ClassDeclaration ClassDeclaration) {
        resolveClassDeclaration(ClassDeclaration);
    }

    @Override
    public void visit(ClassDeclarationErrorInExtends ClassDeclarationErrorInExtends) {
        resolveClassDeclaration(ClassDeclarationErrorInExtends);
    }

    public void resolveClassDeclaration(ClassDecl ClassDecl) {
        SymbolTable.chainLocalSymbols(currentClass.getType());

        if (ClassDecl instanceof ClassDeclaration)
            ClassDecl.obj = ((ClassDeclaration) ClassDecl).getClassName().obj;
        else if (ClassDecl instanceof ClassDeclarationErrorInExtends)
            ClassDecl.obj = ((ClassDeclarationErrorInExtends) ClassDecl).getClassName().obj;
        else
            throw new RuntimeException("Invalid class declaration.");

        int numberOfClassFields = SymbolTable.currentScope().getnVars();
        if (numberOfClassFields > CLASS_FIELDS_MAX)
            throwError(ClassDecl.getLine(), "Class is not allowed to have more than " + CLASS_FIELDS_MAX + " class fields variables.");

        // if derived class check that all abstract methods are implemented
        if (currentClassExtends != null) {
            Iterator<Obj> classMembersIterator = currentClass.getType().getMembers().iterator();
            while (classMembersIterator.hasNext()) {
                Obj current = classMembersIterator.next();

                // check only inherited abstract methods
                if (current.getKind() == SymbolTable.AbstractMethodObject)
                    throwError(ClassDecl.getLine(), "The class has not implemented method '" + current.getName() + "' from the abstract base class.");
                else if (current.getKind() == Obj.Meth) {
                    List<Obj> methodList = implementedAbstractMethods.get(currentClassName);

                    for (Obj o : methodList) {
                        // check return type (TYPE or VOID)
                        if (o.getName().equals(current.getName())) {
                            if (SymbolTable.assignmentPossible(o.getType(), current.getType())) {
                                // check for the same number of parameters
                                if (o.getLevel() == current.getLevel()) {
                                    // no need to check the first formal ('this') parameter
                                    for (int i = 1; i < o.getLevel(); i++) {
                                        if (SymbolTable.getFormalParameter(o, i).getType() != SymbolTable.getFormalParameter(current, i).getType())
                                            throwError(ClassDecl.getLine(), "The class has implemented method '" + current.getName() + "', but formal parameter type differs from the one specified in abstract method definition.");
                                    }
                                }
                                else
                                    throwError(ClassDecl.getLine(), "The class has implemented method '" + current.getName() + "', but number of parameters differs from the one specified in abstract method definition.");
                            }
                            else
                                throwError(ClassDecl.getLine(), "The class has implemented method '" + current.getName() + "', but its return type differs from the one specified in abstract method definition.");
                        }
                    }
                }
            }
        }

        // ensure that all inherited abstract methods are implemented
        SymbolTable.chainLocalSymbols(currentClass.getType());

        SymbolTable.closeScope();
        currentClass = null;
        currentClassName = "";
        currentClassExtends = null;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // ABSTRACT METHOD
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ClassName ClassName) {
        Obj object = SymbolTable.find(ClassName.getClassName());

        if (object == SymbolTable.noObj || object == null) {
            currentClass = ClassName.obj = SymbolTable.insert(Obj.Type, ClassName.getClassName(), SymbolTable.getClassStruct(ClassName.getClassName()));
            currentClassName = ClassName.getClassName();

            prepareClassSymbolTable();
        }
        else
            throwError(ClassName.getLine(), "Class with name '" + ClassName.getClassName() + "' already exists.");
    }

    @Override
    public void visit(ClassDeclExtendsModifier ClassDeclExtendsModifier) {
        Struct baseClassType = ClassDeclExtendsModifier.getType().struct;

        Obj dataType = SymbolTable.find(((DataType) ClassDeclExtendsModifier.getType()).getTypeIdent());

        if (dataType == SymbolTable.noObj || dataType == null)
            throwError(ClassDeclExtendsModifier.getLine(), "Base class not found in symbol table.");
        else if (currentClass.getType() == baseClassType)
            throwError(ClassDeclExtendsModifier.getLine(), "Recursive class extension not allowed.");
        else if (baseClassType.getKind() != Struct.Class && baseClassType.getKind() != Struct.Interface)
            throwError(ClassDeclExtendsModifier.getLine(), "Class can only extend class.");
        else {
            currentClass.getType().setElementType(baseClassType);
            inheritBaseClassMembers(baseClassType);
            currentClassExtends = dataType;
        }
    }

    // final non-terminal for abstract class declaration
    @Override
    public void visit(AbstractClassDecl AbstractClassDecl) {
        if (currentClass != null)
            SymbolTable.chainLocalSymbols(currentClass.getType());

        AbstractClassDecl.obj = AbstractClassDecl.getAbstractClassName().obj;

        int numberOfClassFields = SymbolTable.currentScope().getnVars();
        if (numberOfClassFields > CLASS_FIELDS_MAX)
            throwError(AbstractClassDecl.getLine(), "Class is not allowed to have more than " + CLASS_FIELDS_MAX + " class fields variables.");

        // ensure that all inherited abstract methods are implemented
        SymbolTable.chainLocalSymbols(currentClass.getType());

        SymbolTable.closeScope();
        currentClass = null;
    }

    private void prepareClassSymbolTable() {
        SymbolTable.openScope(SymbolTable.ScopeType.IN_CLASS);

        if (implementedAbstractMethods.get(currentClassName) == null)
            implementedAbstractMethods.put(currentClassName, new LinkedList<Obj>());

        // needed to add, otherwise fields cannot be accesed properly during
        // code generation as first field would point to VTP, etc.
        SymbolTable.insert(Obj.Fld, "_vtp", Tab.noType);
    }

    @Override
    public void visit(AbstractClassName AbstractClassName) {
        Obj object = SymbolTable.find(AbstractClassName.getAbstractClassIdent());

        if (object == SymbolTable.noObj || object == null) {
            currentClass = AbstractClassName.obj = SymbolTable.insert(Obj.Type, AbstractClassName.getAbstractClassIdent(), new Struct(Struct.Interface));
            currentClassName = AbstractClassName.getAbstractClassIdent();

            prepareClassSymbolTable();
        }
        else
            throwError(AbstractClassName.getLine(), "Abstract Class with name '" + AbstractClassName.getAbstractClassIdent() + "' already exists.");
    }

    //////////////////////////////////////////////////////////////////////////////////
    // STATEMENT
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(AbstractClassExtends AbstractClassExtends) {
        Struct baseClassType = AbstractClassExtends.getType().struct;

        Obj dataType = SymbolTable.find(((DataType) AbstractClassExtends.getType()).getTypeIdent());

        if (dataType == SymbolTable.noObj || dataType == null)
            throwError(AbstractClassExtends.getLine(), "Base class not found in symbol table.");
        else if (currentClass.getType() == baseClassType)
            throwError(AbstractClassExtends.getLine(), "Recursive class extension not allowed.");
        else if (baseClassType.getKind() != Struct.Class && baseClassType.getKind() != Struct.Interface)
            throwError(AbstractClassExtends.getLine(), "Class can only extend class and abstract class.");
        else
            inheritBaseClassMembers(baseClassType);
    }

    private void inheritBaseClassMembers(Struct baseClassType) {
        SymbolTable.insert(Obj.Type, "extends", baseClassType);
        SymbolTable.chainLocalSymbols(currentClass.getType());

        // inherit fields and methods of base class
        Iterator<Obj> classIterator = baseClassType.getMembers().iterator();
        while (classIterator.hasNext()) {
            Obj current = classIterator.next();

            if (current.getKind() == Obj.Fld) {
                SymbolTable.insert(current.getKind(), current.getName(), current.getType());
            }
            else if (current.getKind() == Obj.Meth || current.getKind() == SymbolTable.AbstractMethodObject) {
                Iterator t = current.getLocalSymbols().iterator();

                // bugfix for not inheriting the address which is generated during code generation phase
                SymbolTable.currentScope.addToLocals(current);

				/*HashTableDataStructure hashTableDataStructure = new HashTableDataStructure();
				while (t.hasNext())
					hashTableDataStructure.insertKey((Obj)t.next());

				Obj root = SymbolTable.insert(current.getKind(), current.getName(), current.getType());
				root.setLevel(current.getLevel());
				root.setFpPos(current.getFpPos());
				root.setLocals(hashTableDataStructure);
				root.setAdr(current.getAdr());*/
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    // DESIGNATOR OPERATIONS
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(AbstractMethodWithoutReturn AbstractMethodWithoutReturn) {
        abstractMethodReturnType = SymbolTable.noType;
    }

    @Override
    public void visit(AbstractMethodWithReturn AbstractMethodWithReturn) {
        abstractMethodReturnType = AbstractMethodWithReturn.getType().struct;
    }

    @Override
    public void visit(AbstractMethodName AbstractMethodName) {
        String methodName = AbstractMethodName.getI1();
        currentMethodName = methodName;

        Obj object = SymbolTable.currentScope.findSymbol(methodName);
        if (object == SymbolTable.noObj || object == null) {
            Obj method = AbstractMethodName.obj = currentMethod = SymbolTable.insert(SymbolTable.AbstractMethodObject, methodName, abstractMethodReturnType);
            method.setLevel(0);

            SymbolTable.openScope(SymbolTable.ScopeType.OUTSIDE_CLASS);

            // adding formal parameter 'this'
            SymbolTable.insert(Obj.Var, "this", currentClass.getType());
            currentMethod.setLevel(1);
            /////////////////////////////////
        }
        else
            throwError(AbstractMethodName.getLine(), "Abstract method with name '" + methodName + "' has already been declared in current scope.");
    }

    @Override
    public void visit(AbstractMethodDeclaration AbstractMethodDeclaration) {
        AbstractMethodDeclaration.obj = AbstractMethodDeclaration.getAbstractMethodName().obj;
        AbstractMethodDeclaration.obj.setLocals(SymbolTable.currentScope.getLocals());

        abstractMethodReturnType = null;
        currentMethodName = "";
        currentMethod = null;

        SymbolTable.closeScope();
    }

    //////////////////////////////////////////////////////////////////////////////////
    // CONDITION FACT
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ReadStatement ReadStatement) {
        Designator designator = ReadStatement.getDesignator();

        if (designator.obj.getKind() != Obj.Var &&
                designator.obj.getKind() != Obj.Elem &&
                designator.obj.getKind() != Obj.Fld)
            throwError(ReadStatement.getLine(), "Read statement has to be used with variable, array element or class field.");
        else if (designator.obj.getType() != SymbolTable.intType &&
                designator.obj.getType() != SymbolTable.charType &&
                designator.obj.getType() != SymbolTable.BooleanStruct)
            throwError(ReadStatement.getLine(), "Read statement operand has to be of integer, character or boolean data type.");
    }

    @Override
    public void visit(PrintStatement PrintStatement) {
        if (PrintStatement.getExpr().struct != SymbolTable.intType &&
                PrintStatement.getExpr().struct != SymbolTable.charType &&
                PrintStatement.getExpr().struct != SymbolTable.BooleanStruct)
            throwError(PrintStatement.getLine(), "Print statement has to be used with integer, character or boolean data type.");
    }

    //////////////////////////////////////////////////////////////////////////////////
    // CONDITION FACT
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(DesignatorAssign DesignatorAssign) {
        if (errorDetected)
            return;

        Designator designator = DesignatorAssign.getDesignator();
        Expr expression = DesignatorAssign.getExpr();

        if (designator.obj.getKind() != Obj.Var &&
                designator.obj.getKind() != Obj.Elem &&
                designator.obj.getKind() != Obj.Fld)
            throwError(DesignatorAssign.getLine(), "Left side of assigment statement has to be variable, array element or class field.");
        else if (!SymbolTable.assignmentPossible(designator.obj.getType(), expression.struct))
            throwError(DesignatorAssign.getLine(), "Data types of left and right side of assigment statement don't match.");
    }

    private void designatorIncrementDecrement(Designator designator, int line) {
        if (errorDetected)
            return;

        if (designator.obj.getKind() != Obj.Var &&
                designator.obj.getKind() != Obj.Elem &&
                designator.obj.getKind() != Obj.Fld)
            throwError(line, "Increment/Decrement statement has to be done on top of variable, array element or class field.");
        else if (designator.obj.getType() != SymbolTable.intType)
            throwError(line, "Increment/Decrement statement has to be done on top of integer data type.");
    }

    @Override
    public void visit(DesignatorIncrement DesignatorIncrement) {
        designatorIncrementDecrement(DesignatorIncrement.getDesignator(), DesignatorIncrement.getLine());
    }

    @Override
    public void visit(DesignatorDecrement DesignatorDecrement) {
        designatorIncrementDecrement(DesignatorDecrement.getDesignator(), DesignatorDecrement.getLine());
    }

    @Override
    public void visit(CondFactBinary CondFactBinary) {
        Struct left = CondFactBinary.getExpr().struct;
        Relop op = CondFactBinary.getRelop();
        Struct right = CondFactBinary.getExpr1().struct;

        if (left == null || right == null)
            return;

        if (!left.compatibleWith(right))
            throwError(CondFactBinary.getLine(), "Types are not compatible for comparison with relation operators.");
        else if ((left.getKind() == Struct.Array || left.getKind() == Struct.Class || right.getKind() == Struct.Array || right.getKind() == Struct.Class) &&
                !(op instanceof OperatorEqual || op instanceof OperatorNotEqual))
            throwError(CondFactBinary.getLine(), "Conditionals with reference types can only be used with equals and not equals operators.");
        else
            CondFactBinary.struct = SymbolTable.BooleanStruct;
    }

    @Override
    public void visit(CondFactUnary CondFactUnary) {
        if (CondFactUnary.getExpr().struct != SymbolTable.BooleanStruct)
            throwError(CondFactUnary.getLine(), "Condition cannot be any other type than boolean.");
        else
            CondFactUnary.struct = SymbolTable.BooleanStruct;
    }

    @Override
    public void visit(MethodWithoutReturn MethodWithoutReturn) {
        currentMethodReturnType = MethodWithoutReturn.struct = SymbolTable.noType;
    }

    @Override
    public void visit(MethodWithReturn MethodWithReturn) {
        currentMethodReturnType = MethodWithReturn.struct = MethodWithReturn.getType().struct;
    }

    @Override
    public void visit(MethodName MethodName) {
        // NOTE: ord, chr, len shall not be added to symbol table as they already exist on its creation

        Struct returnType = currentMethodReturnType;
        String methodName = MethodName.getMethodName();

        // insert method name into symbol table and removing inherited abstract method
        Obj object = SymbolTable.currentScope.findSymbol(methodName);
        if (object == SymbolTable.noObj ||
                object == null ||
                object.getKind() == SymbolTable.AbstractMethodObject ||
                object.getKind() == Obj.Meth) {
            Obj newSymbol;
            if (currentClass != null && object != null &&
                    (object.getKind() == SymbolTable.AbstractMethodObject || object.getKind() == Obj.Meth)) {
                List<Obj> list;

                if (implementedAbstractMethods.get(currentClassName) == null)
                    implementedAbstractMethods.put(currentClassName, new LinkedList<Obj>());
                list = implementedAbstractMethods.get(currentClassName);

                list.add(object);
                SymbolTable.currentScope.getLocals().deleteKey(object.getName());
                newSymbol = SymbolTable.insert(Obj.Meth, methodName, returnType);
            }
            else
                newSymbol = SymbolTable.insert(Obj.Meth, methodName, returnType);

            MethodName.obj = newSymbol;
        }
        else
            throwError(MethodName.getLine(), "Method with name '" + methodName + "' has already been defined.");

        if (currentClass != null)
            SymbolTable.chainLocalSymbols(currentClass.getType());

        // open method scope <--> this will translate into Obj.Var so that new declarations shall be added as variables, not as field
        SymbolTable.openScope(SymbolTable.ScopeType.OUTSIDE_CLASS);
        // save reference to current method
        currentMethod = MethodName.obj;
        // set number of formal parameters to 0 (zero)
        currentMethod.setLevel(0);

        currentMethodName += methodName;

        if (currentClass == null && methodName.equals("main")) {
            // looking for main method declaration outside of any class
            if (returnType == SymbolTable.noType)
                mainMethodFound = true;
            else
                throwError(MethodName.getLine(), "Method 'main()' should not return any value.");
        }
        else if (currentClass != null) {
            //SymbolTable.chainLocalSymbols(currentClass.getType());
            // only functions inside class have implicit parameter 'this'
            SymbolTable.insert(Obj.Var, "this", currentClass.getType());
            currentMethod.setLevel(1);

            currentMethodName = currentClassName + "." + currentMethodName;
        }

        ConditionalStatementActions.MethodBegin();
    }

    @Override
    public void visit(MethodSignature MethodSignature) {
        MethodSignature.obj = MethodSignature.getMethodName().obj;
    }

    //////////////////////////////////////////////////////////////////////////////////
    // FOR LOOP
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(MethodDecl MethodDecl) {
        ConditionalStatementActions.MethodEnd();

        if (currentMethod.getType() != SymbolTable.noType && !ConditionalStatementActions.MethodReturnsOnAllPaths())
            throwError(MethodDecl.getLine(), "Function doesn't return value on all code paths.");

        int numberOfLocalVariables = SymbolTable.currentScope().getnVars();
        if (numberOfLocalVariables > LOCAL_VARIABLES_MAX)
            throwError(MethodDecl.getLine(), "Function is not allowed to have more than " + LOCAL_VARIABLES_MAX + " local variables.");

        MethodDecl.obj = MethodDecl.getMethodSignature().obj;
        MethodDecl.obj.setLocals(SymbolTable.currentScope.getLocals());

        SymbolTable.closeScope();
        currentMethod = null;
        currentMethodName = "";
    }

    @Override
    public void visit(ReturnStatementt ReturnStatementt) {
        ReturnStatement ret = ReturnStatementt.getReturnStatement();

        // method is VOID and doesn't return type
        if (currentMethod.getType() == SymbolTable.noType)
            throwError(ReturnStatementt.getLine(), "Method doesn't return any value, therefore 'return' statement cannot be used.");
            // incompatible declared return type and type of return statement
        else if (ret instanceof ExprReturnStatement && (((ExprReturnStatement) ret).getExpr().struct == null || !((ExprReturnStatement) ret).getExpr().struct.assignableTo(currentMethod.getType())))
            throwError(ReturnStatementt.getLine(), "Expression in return statement cannot be casted to declared return type of function.");
            // invalid placement of 'return' statement
        else if (currentMethod == null)
            throwError(ReturnStatementt.getLine(), "Statement 'return' cannot be used outside of methods.");
        else
            ConditionalStatementActions.ResolveReturnStatement();
    }

    @Override
    public void visit(SingleForVarDecl SingleForVarDecl) {
        ForStatementActions.ForBegin();
    }

    @Override
    public void visit(NoForVarDecl NoForVarDecl) {
        ForStatementActions.ForBegin();
    }

    @Override
    public void visit(ForStatement ForStatement) {
        ForStatementActions.ForEnd();
    }

    //////////////////////////////////////////////////////////////////////////////////
    // IF CONDITIONAL
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(BreakStatement BreakStatement) {
        if (!ForStatementActions.InForLoop())
            throwError(null, "Statement 'break' can only be used inside 'for' loop.");
    }

    @Override
    public void visit(ContinueStatement ContinueStatement) {
        if (!ForStatementActions.InForLoop())
            throwError(null, "Statement 'continue' can only be used inside 'for' loop.");
    }

    @Override
    public void visit(IfCondition IfCondition) {
        ConditionalStatementActions.IfBegin();
    }

    //////////////////////////////////////////////////////////////////////////////////
    // FUNCTION DECLARATION PARAMETER
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(ElseStatementKeyword ElseStatementKeyword) {
        ConditionalStatementActions.ElseBegin();
    }

    //////////////////////////////////////////////////////////////////////////////////
    // FUNCTION INVOCATION PARAMETERS
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void visit(IfStatement IfStatement) {
        ConditionalStatementActions.IfEnd();
    }

    @Override
    public void visit(SingleFormParameter SingleFormParameter) {
        Type parameterType = SingleFormParameter.getType();
        String parameterName = SingleFormParameter.getFormParamName();
        FormParamArray parameterArray = SingleFormParameter.getFormParamArray();

        Obj object = SymbolTable.currentScope.findSymbol(parameterName);

        if (object == SymbolTable.noObj || object == null) {
            if (currentClass == null && parameterName.equals("main")) {
                throwError(SingleFormParameter.getLine(), "Method 'main()' is not allowed to have any parameters.");
                return;
            }

            if (parameterArray instanceof NoFormParamArray)
                SingleFormParameter.obj = SymbolTable.insert(Obj.Var, parameterName, parameterType.struct);
            else
                SingleFormParameter.obj = SymbolTable.insert(Obj.Var, parameterName, SymbolTable.getArrayStruct(parameterType.struct));

            //////////////////////////////////////////////////////////////////////////////////
            // set position in formal parameter list for current method
            SingleFormParameter.obj.setFpPos(currentMethod.getLevel());
            // update number of parameters in current method
            currentMethod.setLevel(currentMethod.getLevel() + 1);
        }
        else
            throwError(SingleFormParameter.getLine(), "Variable '" + parameterName + "' has already been declared in the surrounding scope and cannot be named the same way in formal parameter specification of a function unless the name is changed.");
    }

    @Override
    public void visit(DesignatorInvoke DesignatorInvoke) {
        Obj methodObject = DesignatorInvoke.getDesignator().obj;
        if (methodObject == SymbolTable.noObj || methodObject == null)
            throwError(DesignatorInvoke.getLine(), "Method with name '" + DesignatorInvoke.getDesignator().obj.getName() + "' not found.");
        else if (methodObject.getLevel() != numberOfParametersWithValue.peek())
            throwError(DesignatorInvoke.getLine(), "Number of invocation parameters of function call doesn't match the function formal definition.");

        // reset current method invocation
        currentMethodName = "";
        currentParameterCheckIndex.pop();
        numberOfParametersWithValue.pop();
        currentFunctionCall.pop();
    }

    @Override
    public void visit(DesignatorMethodCallParameters DesignatorMethodCallParameters) {
        super.visit(DesignatorMethodCallParameters);
    }

    private void noParameters() {
        numberOfParametersWithValue.push(0);
        currentParameterCheckIndex.push(0);
        currentFunctionCall.push(designatorAccessObject);

        if (currentFunctionCall.peek() != null) {
            Obj obj = currentFunctionCall.peek();
            for (Obj localSymbol : obj.getLocalSymbols()) {
                if (localSymbol.getName().equals("this")) {
                    incrementAtTopOfStack(numberOfParametersWithValue);
                    break;
                }
            }
        }
    }

    @Override
    public void visit(NoFactorFunctionCallParameterSingle NoFactorFunctionCallParameterSingle) {
        noParameters();
    }

    @Override
    public void visit(NoDesignatorMethodCallParameters NoDesignatorMethodCallParameters) {
        noParameters();
    }

    private void incrementAtTopOfStack(Stack<Integer> stack) {
        Integer t = stack.pop();
        t++;
        stack.push(t);
    }

    private void decrementAtTopOfStack(Stack<Integer> stack) {
        Integer t = stack.pop();
        t--;
        stack.push(t);
    }

    private boolean checkParameter(Struct parameterDataType) {
        Obj methodObject = currentFunctionCall.peek();
        boolean result = false;

        Iterator<Obj> iterator = methodObject.getLocalSymbols().iterator();
        boolean exitWhile = false;

        while (iterator.hasNext()) {
            Obj current = iterator.next();

            if (currentParameterCheckIndex.peek() < 0)
                break;

            if (currentParameterCheckIndex.peek() == 0 &&
                    current.getName().equals("this")) {
                decrementAtTopOfStack(currentParameterCheckIndex);
                incrementAtTopOfStack(numberOfParametersWithValue);
            }

            if (exitWhile)    // needed because first parameter may not be 'this' and because search would then continue
                break;

            if (current.getFpPos() == currentParameterCheckIndex.peek()) {
                // frame position is ok
                decrementAtTopOfStack(currentParameterCheckIndex);

                // typechecking
                if (parameterDataType.assignableTo(current.getType())) {
                    result = true;

                    if (currentParameterCheckIndex.peek() == 0) {
                        iterator = methodObject.getLocalSymbols().iterator();
                        exitWhile = true;
                    }
                    else
                        break;
                }
            }
        }

        return result;
    }

    @Override
    public void visit(ActParsSingle ActParsSingle) {
        currentParameterCheckIndex.push(currentFunctionCall.peek().getLevel() - 1);
        numberOfParametersWithValue.push(1);

        if (!checkParameter(ActParsSingle.getExpr().struct))
            throwError(null, "Method invocation data type doesn't match the formal parameter data type.");
    }

    @Override
    public void visit(ActParsMultiple ActParsMultiple) {
        incrementAtTopOfStack(numberOfParametersWithValue);

        if (!checkParameter(ActParsMultiple.getExpr().struct))
            throwError(null, "Method invocation data type doesn't match the formal parameter data type.");
    }

    @Override
    public void visit(DesignatorInvokeMethodName DesignatorInvokeMethodName) {
        currentFunctionCall.push(designatorAccessObject);
    }

    @Override
    public void visit(DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd) {
        //currentFunctionCall.pop();
    }

    public static class SharedData {
        public String programName;
        public int numberOfMethodGlobalParameters;
    }
}