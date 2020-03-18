// generated with ast extension for cup
// version 0.8
// 18/2/2020 17:24:49


package cvetkovic.parser.ast;

public interface Visitor {

    void visit(MethodDeclReturnType MethodDeclReturnType);

    void visit(FactorArrayDecl FactorArrayDecl);

    void visit(FactorFunctionCallParameters FactorFunctionCallParameters);

    void visit(Factor Factor);

    void visit(SingleFormalParameter SingleFormalParameter);

    void visit(Statement Statement);

    void visit(AbstractMethodReturnType AbstractMethodReturnType);

    void visit(MethodStatements MethodStatements);

    void visit(ReturnStatement ReturnStatement);

    void visit(FactorFunctionCallParametersSingle FactorFunctionCallParametersSingle);

    void visit(AbstractMethodDecl AbstractMethodDecl);

    void visit(Relop Relop);

    void visit(ElseStatement ElseStatement);

    void visit(ClassVarList ClassVarList);

    void visit(MultipleFormalParameter MultipleFormalParameter);

    void visit(Expr Expr);

    void visit(CondTermRight CondTermRight);

    void visit(ProgramElementsDeclList ProgramElementsDeclList);

    void visit(VarDecl VarDecl);

    void visit(DesignatorParams DesignatorParams);

    void visit(ClassMethod ClassMethod);

    void visit(ForLoopCondition ForLoopCondition);

    void visit(SingleConstDeclaration SingleConstDeclaration);

    void visit(MultipleVarDeclaration MultipleVarDeclaration);

    void visit(MultipleStatement MultipleStatement);

    void visit(LogicalOr LogicalOr);

    void visit(ForVarDecl ForVarDecl);

    void visit(Mulop Mulop);

    void visit(DesignatorStatement DesignatorStatement);

    void visit(ProgramElementsDeclListElement ProgramElementsDeclListElement);

    void visit(AbstractClassMethodList AbstractClassMethodList);

    void visit(Addop Addop);

    void visit(ConditionRight ConditionRight);

    void visit(MethodParameters MethodParameters);

    void visit(ClassMethodList ClassMethodList);

    void visit(Type Type);

    void visit(AdditionalConstDeclaration AdditionalConstDeclaration);

    void visit(AbstractMethodParameters AbstractMethodParameters);

    void visit(MethodVarDeclList MethodVarDeclList);

    void visit(ProgramMethodsDeclList ProgramMethodsDeclList);

    void visit(ClassDecl ClassDecl);

    void visit(ForUpdateVarList ForUpdateVarList);

    void visit(AbstractClassMethodTypesAllowed AbstractClassMethodTypesAllowed);

    void visit(AbstractClassMultipleMethodTypes AbstractClassMultipleMethodTypes);

    void visit(ExprNegative ExprNegative);

    void visit(ConstValue ConstValue);

    void visit(AbstractClassVarList AbstractClassVarList);

    void visit(MultiplePrint MultiplePrint);

    void visit(ClassDeclExtends ClassDeclExtends);

    void visit(ActPars ActPars);

    void visit(AbstractExtends AbstractExtends);

    void visit(Designator Designator);

    void visit(CondFact CondFact);

    void visit(SingleVarDeclaration SingleVarDeclaration);

    void visit(Term Term);

    void visit(OperatorModulo OperatorModulo);

    void visit(OperatorDivision OperatorDivision);

    void visit(OperatorMultiplication OperatorMultiplication);

    void visit(OperatorSubtraction OperatorSubtraction);

    void visit(OperatorAddition OperatorAddition);

    void visit(OperatorLessOrEqual OperatorLessOrEqual);

    void visit(OperatorLess OperatorLess);

    void visit(OperatorGreaterOrEqual OperatorGreaterOrEqual);

    void visit(OperatorGreater OperatorGreater);

    void visit(OperatorNotEqual OperatorNotEqual);

    void visit(OperatorEqual OperatorEqual);

    void visit(Assignop Assignop);

    void visit(DesignatorNonArrayAccess DesignatorNonArrayAccess);

    void visit(DesignatorArrayAccess DesignatorArrayAccess);

    void visit(DesignatorRoot DesignatorRoot);

    void visit(NoArrayDeclaration NoArrayDeclaration);

    void visit(ArrayDeclaration ArrayDeclaration);

    void visit(NoFactorFunctionCallParameterSingle NoFactorFunctionCallParameterSingle);

    void visit(FactorFunctionCallParameterSingle FactorFunctionCallParameterSingle);

    void visit(NoFactorFunctionCallParameter NoFactorFunctionCallParameter);

    void visit(FactorFunctionCallParameter FactorFunctionCallParameter);

    void visit(FactorExpressionInBrackets FactorExpressionInBrackets);

    void visit(FactorArrayDeclaration FactorArrayDeclaration);

    void visit(FactorBoolConst FactorBoolConst);

    void visit(FactorCharConst FactorCharConst);

    void visit(FactorNumericalConst FactorNumericalConst);

    void visit(FactorFunctionCall FactorFunctionCall);

    void visit(TermMultiple TermMultiple);

    void visit(TermSingle TermSingle);

    void visit(ExpressionPositive ExpressionPositive);

    void visit(ExpressionNegative ExpressionNegative);

    void visit(BinaryExpression BinaryExpression);

    void visit(UnaryExpression UnaryExpression);

    void visit(CondFactBinary CondFactBinary);

    void visit(CondFactUnary CondFactUnary);

    void visit(NoCondTermRight NoCondTermRight);

    void visit(CondTermRightt CondTermRightt);

    void visit(CondTerm CondTerm);

    void visit(LogicalOrCondition LogicalOrCondition);

    void visit(NoConditionRight NoConditionRight);

    void visit(ConditionRightt ConditionRightt);

    void visit(Condition Condition);

    void visit(ActParsMultiple ActParsMultiple);

    void visit(ActParsSingle ActParsSingle);

    void visit(NoDesignatorMethodCallParameters NoDesignatorMethodCallParameters);

    void visit(DesignatorMethodCallParameters DesignatorMethodCallParameters);

    void visit(DesignatorInvokeMethodNameEnd DesignatorInvokeMethodNameEnd);

    void visit(DesignatorInvokeMethodName DesignatorInvokeMethodName);

    void visit(DesignatorAssignMakeLeaf DesignatorAssignMakeLeaf);

    void visit(DesignatorDecrement DesignatorDecrement);

    void visit(DesignatorIncrement DesignatorIncrement);

    void visit(DesignatorInvoke DesignatorInvoke);

    void visit(DesignatorAssign DesignatorAssign);

    void visit(NoMultipleStatement NoMultipleStatement);

    void visit(MultipleStatements MultipleStatements);

    void visit(NoMultiplePrint NoMultiplePrint);

    void visit(MultiplePrintSt MultiplePrintSt);

    void visit(BlankReturnStatement BlankReturnStatement);

    void visit(ExprReturnStatement ExprReturnStatement);

    void visit(EndOfForStatement EndOfForStatement);

    void visit(ForUpdateVarListError ForUpdateVarListError);

    void visit(NoForUpdateVarList NoForUpdateVarList);

    void visit(SingleForUpdateVarList SingleForUpdateVarList);

    void visit(ForLoopConditionEnd ForLoopConditionEnd);

    void visit(StartForCondition StartForCondition);

    void visit(ForLoopConditionError ForLoopConditionError);

    void visit(NoForLoopCondition NoForLoopCondition);

    void visit(SingleForLoopCondition SingleForLoopCondition);

    void visit(ForVarDeclEnd ForVarDeclEnd);

    void visit(ForVarDeclError ForVarDeclError);

    void visit(NoForVarDecl NoForVarDecl);

    void visit(SingleForVarDecl SingleForVarDecl);

    void visit(ForKeyword ForKeyword);

    void visit(ElseStatementKeyword ElseStatementKeyword);

    void visit(NoElseStatement NoElseStatement);

    void visit(ElseStatementt ElseStatementt);

    void visit(IfKeyword IfKeyword);

    void visit(IfCondition IfCondition);

    void visit(ErrorStatement ErrorStatement);

    void visit(MultipleStatementt MultipleStatementt);

    void visit(PrintStatement PrintStatement);

    void visit(ReadStatement ReadStatement);

    void visit(ReturnStatementt ReturnStatementt);

    void visit(ContinueStatement ContinueStatement);

    void visit(BreakStatement BreakStatement);

    void visit(ForStatement ForStatement);

    void visit(IfStatement IfStatement);

    void visit(StatementDesignator StatementDesignator);

    void visit(DataType DataType);

    void visit(NoFormParamArray NoFormParamArray);

    void visit(FormParamArray FormParamArray);

    void visit(NoMultipleFormalParameter NoMultipleFormalParameter);

    void visit(MultipleFormalParameterr MultipleFormalParameterr);

    void visit(SingleFormParameterError SingleFormParameterError);

    void visit(SingleFormParameter SingleFormParameter);

    void visit(FormPars FormPars);

    void visit(NoAbstractMethodParameters NoAbstractMethodParameters);

    void visit(AbstractMethodParameterss AbstractMethodParameterss);

    void visit(AbstractMethodWithoutReturn AbstractMethodWithoutReturn);

    void visit(AbstractMethodWithReturn AbstractMethodWithReturn);

    void visit(AbstractMethodName AbstractMethodName);

    void visit(AbstractMethodDeclarationError AbstractMethodDeclarationError);

    void visit(AbstractMethodDeclaration AbstractMethodDeclaration);

    void visit(NoMethodStatements NoMethodStatements);

    void visit(MethodStatementss MethodStatementss);

    void visit(NoMethodVarDeclList NoMethodVarDeclList);

    void visit(MethodVarDeclListt MethodVarDeclListt);

    void visit(NoMethodParameters NoMethodParameters);

    void visit(MethodParameterss MethodParameterss);

    void visit(MethodWithoutReturn MethodWithoutReturn);

    void visit(MethodWithReturn MethodWithReturn);

    void visit(MethodName MethodName);

    void visit(MethodSignature MethodSignature);

    void visit(MethodDecl MethodDecl);

    void visit(AbstractClassMethodTypesAbstractMethodType AbstractClassMethodTypesAbstractMethodType);

    void visit(AbstractClassMethodTypesMethodType AbstractClassMethodTypesMethodType);

    void visit(NoAbstractClassMultipleMethodTypes NoAbstractClassMultipleMethodTypes);

    void visit(AbstractClassMultipleMethodTypesClass AbstractClassMultipleMethodTypesClass);

    void visit(NoAbstractClassMethodList NoAbstractClassMethodList);

    void visit(AbstractClassMethodListing AbstractClassMethodListing);

    void visit(NoAbstractClassVarList NoAbstractClassVarList);

    void visit(AbstractClassVariableList AbstractClassVariableList);

    void visit(NoAbstractExtends NoAbstractExtends);

    void visit(AbstractClassExtends AbstractClassExtends);

    void visit(AbstractClassName AbstractClassName);

    void visit(AbstractClassDecl AbstractClassDecl);

    void visit(NoClassMethod NoClassMethod);

    void visit(ClassMethodd ClassMethodd);

    void visit(NoClassMethodList NoClassMethodList);

    void visit(ClassMethodListt ClassMethodListt);

    void visit(NoClassVarList NoClassVarList);

    void visit(ClassVarListt ClassVarListt);

    void visit(NoClassExtends NoClassExtends);

    void visit(ClassDeclExtendsModifier ClassDeclExtendsModifier);

    void visit(ClassName ClassName);

    void visit(ClassDeclarationErrorBeforeExtends ClassDeclarationErrorBeforeExtends);

    void visit(ClassDeclarationErrorInExtends ClassDeclarationErrorInExtends);

    void visit(ClassDeclaration ClassDeclaration);

    void visit(SingleVarArray SingleVarArray);

    void visit(SingleVarNoArray SingleVarNoArray);

    void visit(SingleVarError SingleVarError);

    void visit(SingleVariableDeclaration SingleVariableDeclaration);

    void visit(SingleVarDecl SingleVarDecl);

    void visit(MultipleVariablesDeclaration MultipleVariablesDeclaration);

    void visit(VarDeclError VarDeclError);

    void visit(VarDeclarationRoot VarDeclarationRoot);

    void visit(BooleanConst BooleanConst);

    void visit(CharacterConst CharacterConst);

    void visit(NumericalConst NumericalConst);

    void visit(SingleConst SingleConst);

    void visit(NoAditionalConstDeclaration NoAditionalConstDeclaration);

    void visit(AdditionalConstantDeclaration AdditionalConstantDeclaration);

    void visit(ConstDecl ConstDecl);

    void visit(NoProgramMethodList NoProgramMethodList);

    void visit(ProgramMethodDeclList ProgramMethodDeclList);

    void visit(SingleProgramClass SingleProgramClass);

    void visit(SingleProgramAbstractClass SingleProgramAbstractClass);

    void visit(SingleProgramVar SingleProgramVar);

    void visit(SingleProgramConst SingleProgramConst);

    void visit(NoProgramElements NoProgramElements);

    void visit(ProgramElementsDeclarationList ProgramElementsDeclarationList);

    void visit(ProgramName ProgramName);

    void visit(Program Program);

}
