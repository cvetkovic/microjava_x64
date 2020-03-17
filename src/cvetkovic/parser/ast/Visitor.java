// generated with ast extension for cup
// version 0.8
// 17/2/2020 22:24:19


package cvetkovic.parser.ast;

public interface Visitor {

    public void visit(MethodDeclReturnType MethodDeclReturnType);

    public void visit(FactorArrayDecl FactorArrayDecl);

    public void visit(FactorFunctionCallParameters FactorFunctionCallParameters);

    public void visit(Factor Factor);

    public void visit(SingleFormalParameter SingleFormalParameter);

    public void visit(Statement Statement);

    public void visit(AbstractMethodReturnType AbstractMethodReturnType);

    public void visit(MethodStatements MethodStatements);

    public void visit(ReturnStatement ReturnStatement);

    public void visit(FactorFunctionCallParametersSingle FactorFunctionCallParametersSingle);

    public void visit(AbstractMethodDecl AbstractMethodDecl);

    public void visit(Relop Relop);

    public void visit(ElseStatement ElseStatement);

    public void visit(ClassVarList ClassVarList);

    public void visit(MultipleFormalParameter MultipleFormalParameter);

    public void visit(Expr Expr);

    public void visit(CondTermRight CondTermRight);

    public void visit(ProgramElementsDeclList ProgramElementsDeclList);

    public void visit(VarDecl VarDecl);

    public void visit(DesignatorParams DesignatorParams);

    public void visit(ClassMethod ClassMethod);

    public void visit(ForLoopCondition ForLoopCondition);

    public void visit(SingleConstDeclaration SingleConstDeclaration);

    public void visit(MultipleVarDeclaration MultipleVarDeclaration);

    public void visit(MultipleStatement MultipleStatement);

    public void visit(LogicalOr LogicalOr);

    public void visit(ForVarDecl ForVarDecl);

    public void visit(Mulop Mulop);

    public void visit(DesignatorStatement DesignatorStatement);

    public void visit(ProgramElementsDeclListElement ProgramElementsDeclListElement);

    public void visit(AbstractClassMethodList AbstractClassMethodList);

    public void visit(Addop Addop);

    public void visit(ConditionRight ConditionRight);

    public void visit(MethodParameters MethodParameters);

    public void visit(ClassMethodList ClassMethodList);

    public void visit(Type Type);

    public void visit(AdditionalConstDeclaration AdditionalConstDeclaration);

    public void visit(AbstractMethodParameters AbstractMethodParameters);

    public void visit(MethodVarDeclList MethodVarDeclList);

    public void visit(ProgramMethodsDeclList ProgramMethodsDeclList);

    public void visit(ClassDecl ClassDecl);

    public void visit(ForUpdateVarList ForUpdateVarList);

    public void visit(AbstractClassMethodTypesAllowed AbstractClassMethodTypesAllowed);

    public void visit(AbstractClassMultipleMethodTypes AbstractClassMultipleMethodTypes);

    public void visit(ExprNegative ExprNegative);

    public void visit(ConstValue ConstValue);

    public void visit(AbstractClassVarList AbstractClassVarList);

    public void visit(MultiplePrint MultiplePrint);

    public void visit(ClassDeclExtends ClassDeclExtends);

    public void visit(ActPars ActPars);

    public void visit(AbstractExtends AbstractExtends);

    public void visit(Designator Designator);

    public void visit(CondFact CondFact);

    public void visit(SingleVarDeclaration SingleVarDeclaration);

    public void visit(Term Term);

    public void visit(OperatorModulo OperatorModulo);

    public void visit(OperatorDivision OperatorDivision);

    public void visit(OperatorMultiplication OperatorMultiplication);

    public void visit(OperatorSubtraction OperatorSubtraction);

    public void visit(OperatorAddition OperatorAddition);

    public void visit(OperatorLessOrEqual OperatorLessOrEqual);

    public void visit(OperatorLess OperatorLess);

    public void visit(OperatorGreaterOrEqual OperatorGreaterOrEqual);

    public void visit(OperatorGreater OperatorGreater);

    public void visit(OperatorNotEqual OperatorNotEqual);

    public void visit(OperatorEqual OperatorEqual);

    public void visit(Assignop Assignop);

    public void visit(DesignatorNonArrayAccess DesignatorNonArrayAccess);

    public void visit(DesignatorArrayAccess DesignatorArrayAccess);

    public void visit(DesignatorRoot DesignatorRoot);

    public void visit(NoArrayDeclaration NoArrayDeclaration);

    public void visit(ArrayDeclaration ArrayDeclaration);

    public void visit(NoFactorFunctionCallParameterSingle NoFactorFunctionCallParameterSingle);

    public void visit(FactorFunctionCallParameterSingle FactorFunctionCallParameterSingle);

    public void visit(NoFactorFunctionCallParameter NoFactorFunctionCallParameter);

    public void visit(FactorFunctionCallParameter FactorFunctionCallParameter);

    public void visit(FactorExpressionInBrackets FactorExpressionInBrackets);

    public void visit(FactorArrayDeclaration FactorArrayDeclaration);

    public void visit(FactorBoolConst FactorBoolConst);

    public void visit(FactorCharConst FactorCharConst);

    public void visit(FactorNumericalConst FactorNumericalConst);

    public void visit(FactorFunctionCall FactorFunctionCall);

    public void visit(TermMultiple TermMultiple);

    public void visit(TermSingle TermSingle);

    public void visit(ExpressionPositive ExpressionPositive);

    public void visit(ExpressionNegative ExpressionNegative);

    public void visit(BinaryExpression BinaryExpression);

    public void visit(UnaryExpression UnaryExpression);

    public void visit(CondFactBinary CondFactBinary);

    public void visit(CondFactUnary CondFactUnary);

    public void visit(NoCondTermRight NoCondTermRight);

    public void visit(CondTermRightt CondTermRightt);

    public void visit(CondTerm CondTerm);

    public void visit(LogicalOrCondition LogicalOrCondition);

    public void visit(NoConditionRight NoConditionRight);

    public void visit(ConditionRightt ConditionRightt);

    public void visit(Condition Condition);

    public void visit(ActParsMultiple ActParsMultiple);

    public void visit(ActParsSingle ActParsSingle);

    public void visit(NoDesignatorMethodCallParameters NoDesignatorMethodCallParameters);

    public void visit(DesignatorMethodCallParameters DesignatorMethodCallParameters);

    public void visit(DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd);

    public void visit(DesignatorInvokeMethodName DesignatorInvokeMethodName);

    public void visit(DesignatorDecrement DesignatorDecrement);

    public void visit(DesignatorIncrement DesignatorIncrement);

    public void visit(DesignatorInvoke DesignatorInvoke);

    public void visit(DesignatorAssign DesignatorAssign);

    public void visit(NoMultipleStatement NoMultipleStatement);

    public void visit(MultipleStatements MultipleStatements);

    public void visit(NoMultiplePrint NoMultiplePrint);

    public void visit(MultiplePrintSt MultiplePrintSt);

    public void visit(BlankReturnStatement BlankReturnStatement);

    public void visit(ExprReturnStatement ExprReturnStatement);

    public void visit(EndOfForStatement EndOfForStatement);

    public void visit(ForUpdateVarListError ForUpdateVarListError);

    public void visit(NoForUpdateVarList NoForUpdateVarList);

    public void visit(SingleForUpdateVarList SingleForUpdateVarList);

    public void visit(ForLoopConditionEnd ForLoopConditionEnd);

    public void visit(StartForCondition StartForCondition);

    public void visit(ForLoopConditionError ForLoopConditionError);

    public void visit(NoForLoopCondition NoForLoopCondition);

    public void visit(SingleForLoopCondition SingleForLoopCondition);

    public void visit(ForVarDeclEnd ForVarDeclEnd);

    public void visit(ForVarDeclError ForVarDeclError);

    public void visit(NoForVarDecl NoForVarDecl);

    public void visit(SingleForVarDecl SingleForVarDecl);

    public void visit(ForKeyword ForKeyword);

    public void visit(ElseStatementKeyword ElseStatementKeyword);

    public void visit(NoElseStatement NoElseStatement);

    public void visit(ElseStatementt ElseStatementt);

    public void visit(IfKeyword IfKeyword);

    public void visit(IfCondition IfCondition);

    public void visit(ErrorStatement ErrorStatement);

    public void visit(MultipleStatementt MultipleStatementt);

    public void visit(PrintStatement PrintStatement);

    public void visit(ReadStatement ReadStatement);

    public void visit(ReturnStatementt ReturnStatementt);

    public void visit(ContinueStatement ContinueStatement);

    public void visit(BreakStatement BreakStatement);

    public void visit(ForStatement ForStatement);

    public void visit(IfStatement IfStatement);

    public void visit(StatementDesignator StatementDesignator);

    public void visit(DataType DataType);

    public void visit(NoFormParamArray NoFormParamArray);

    public void visit(FormParamArray FormParamArray);

    public void visit(NoMultipleFormalParameter NoMultipleFormalParameter);

    public void visit(MultipleFormalParameterr MultipleFormalParameterr);

    public void visit(SingleFormParameterError SingleFormParameterError);

    public void visit(SingleFormParameter SingleFormParameter);

    public void visit(FormPars FormPars);

    public void visit(NoAbstractMethodParameters NoAbstractMethodParameters);

    public void visit(AbstractMethodParameterss AbstractMethodParameterss);

    public void visit(AbstractMethodWithoutReturn AbstractMethodWithoutReturn);

    public void visit(AbstractMethodWithReturn AbstractMethodWithReturn);

    public void visit(AbstractMethodName AbstractMethodName);

    public void visit(AbstractMethodDeclarationError AbstractMethodDeclarationError);

    public void visit(AbstractMethodDeclaration AbstractMethodDeclaration);

    public void visit(NoMethodStatements NoMethodStatements);

    public void visit(MethodStatementss MethodStatementss);

    public void visit(NoMethodVarDeclList NoMethodVarDeclList);

    public void visit(MethodVarDeclListt MethodVarDeclListt);

    public void visit(NoMethodParameters NoMethodParameters);

    public void visit(MethodParameterss MethodParameterss);

    public void visit(MethodWithoutReturn MethodWithoutReturn);

    public void visit(MethodWithReturn MethodWithReturn);

    public void visit(MethodName MethodName);

    public void visit(MethodSignature MethodSignature);

    public void visit(MethodDecl MethodDecl);

    public void visit(AbstractClassMethodTypesAbstractMethodType AbstractClassMethodTypesAbstractMethodType);

    public void visit(AbstractClassMethodTypesMethodType AbstractClassMethodTypesMethodType);

    public void visit(NoAbstractClassMultipleMethodTypes NoAbstractClassMultipleMethodTypes);

    public void visit(AbstractClassMultipleMethodTypesClass AbstractClassMultipleMethodTypesClass);

    public void visit(NoAbstractClassMethodList NoAbstractClassMethodList);

    public void visit(AbstractClassMethodListing AbstractClassMethodListing);

    public void visit(NoAbstractClassVarList NoAbstractClassVarList);

    public void visit(AbstractClassVariableList AbstractClassVariableList);

    public void visit(NoAbstractExtends NoAbstractExtends);

    public void visit(AbstractClassExtends AbstractClassExtends);

    public void visit(AbstractClassName AbstractClassName);

    public void visit(AbstractClassDecl AbstractClassDecl);

    public void visit(NoClassMethod NoClassMethod);

    public void visit(ClassMethodd ClassMethodd);

    public void visit(NoClassMethodList NoClassMethodList);

    public void visit(ClassMethodListt ClassMethodListt);

    public void visit(NoClassVarList NoClassVarList);

    public void visit(ClassVarListt ClassVarListt);

    public void visit(NoClassExtends NoClassExtends);

    public void visit(ClassDeclExtendsModifier ClassDeclExtendsModifier);

    public void visit(ClassName ClassName);

    public void visit(ClassDeclarationErrorBeforeExtends ClassDeclarationErrorBeforeExtends);

    public void visit(ClassDeclarationErrorInExtends ClassDeclarationErrorInExtends);

    public void visit(ClassDeclaration ClassDeclaration);

    public void visit(SingleVarArray SingleVarArray);

    public void visit(SingleVarNoArray SingleVarNoArray);

    public void visit(SingleVarError SingleVarError);

    public void visit(SingleVariableDeclaration SingleVariableDeclaration);

    public void visit(SingleVarDecl SingleVarDecl);

    public void visit(MultipleVariablesDeclaration MultipleVariablesDeclaration);

    public void visit(VarDeclError VarDeclError);

    public void visit(VarDeclarationRoot VarDeclarationRoot);

    public void visit(BooleanConst BooleanConst);

    public void visit(CharacterConst CharacterConst);

    public void visit(NumericalConst NumericalConst);

    public void visit(SingleConst SingleConst);

    public void visit(NoAditionalConstDeclaration NoAditionalConstDeclaration);

    public void visit(AdditionalConstantDeclaration AdditionalConstantDeclaration);

    public void visit(ConstDecl ConstDecl);

    public void visit(NoProgramMethodList NoProgramMethodList);

    public void visit(ProgramMethodDeclList ProgramMethodDeclList);

    public void visit(SingleProgramClass SingleProgramClass);

    public void visit(SingleProgramAbstractClass SingleProgramAbstractClass);

    public void visit(SingleProgramVar SingleProgramVar);

    public void visit(SingleProgramConst SingleProgramConst);

    public void visit(NoProgramElements NoProgramElements);

    public void visit(ProgramElementsDeclarationList ProgramElementsDeclarationList);

    public void visit(ProgramName ProgramName);

    public void visit(Program Program);

}
