package cvetkovic.lexer;

import java_cup.runtime.Symbol;
import cvetkovic.parser.sym;

%%

%cup
%line
%column
%public

%{

	private Symbol createSymbol(int type) {
		return new Symbol(type, yyline + 1, yycolumn + 1);
	}

	private Symbol createSymbol(int type, Object value) {
		return new Symbol(type, yyline + 1, yycolumn + 1, value);
	}

%}

%xstate COMMENT

%eofval{
	return createSymbol(sym.EOF);
%eofval}

LineTerminator = "\r" | "\n" | "\r\n"

%%

// special codes

<YYINITIAL> "//"             { yybegin(COMMENT); }
<COMMENT> 	{LineTerminator} { yybegin(YYINITIAL); }
<COMMENT>   .                { yybegin(COMMENT); }

" "		{ }
"\b"	{ }
"\t"	{ }
"\r\n"	{ }
"\n"	{ }
"\r"	{ }
"\f"	{ }

// keywords
"program"	{ return createSymbol(sym.PROGRAM, yytext()); }
"break"		{ return createSymbol(sym.BREAK, yytext()); }
"class"		{ return createSymbol(sym.CLASS, yytext()); }
"abstract"	{ return createSymbol(sym.ABSTRACT, yytext()); }
"else"		{ return createSymbol(sym.ELSE, yytext()); }
"const"		{ return createSymbol(sym.CONST, yytext()); }
"if"		{ return createSymbol(sym.IF, yytext()); }
"new"		{ return createSymbol(sym.NEW, yytext()); }
"print"		{ return createSymbol(sym.PRINT, yytext()); }
"read"		{ return createSymbol(sym.READ, yytext()); }
"return"	{ return createSymbol(sym.RETURN, yytext()); }
"void"		{ return createSymbol(sym.VOID, yytext()); }
"for"		{ return createSymbol(sym.FOR, yytext()); }
"extends"	{ return createSymbol(sym.EXTENDS, yytext()); }
"continue"	{ return createSymbol(sym.CONTINUE, yytext()); }

// identifiers
"true"|"false"			{ return createSymbol(sym.BOOLEAN, new Boolean(yytext())); }
[a-zA-Z][a-zA-Z0-9_]*	{ return createSymbol(sym.IDENT, yytext()); }
[0-9]+					{ return createSymbol(sym.NUM_CONST, new Integer(yytext())); }
"'"[\040-\176]"'"		{ return createSymbol(sym.CHAR, new Character(yytext().charAt(1))); }

// operators
"+"		{ return createSymbol(sym.ADDITION, yytext()); }
"-"		{ return createSymbol(sym.SUBTRACTION, yytext()); }
"*"		{ return createSymbol(sym.MULTIPLICATION, yytext()); }
"/"		{ return createSymbol(sym.DIVISION, yytext()); }
"%"		{ return createSymbol(sym.MODULO, yytext()); }
"=="	{ return createSymbol(sym.EQUAL, yytext()); }
"!="	{ return createSymbol(sym.NOT_EQUAL, yytext()); }
">"		{ return createSymbol(sym.GREATER, yytext()); }
">="	{ return createSymbol(sym.GREATER_OR_EQUAL, yytext()); }
"<"		{ return createSymbol(sym.LESS, yytext()); }
"<="	{ return createSymbol(sym.LESS_OR_EQUAL, yytext()); }
"&&"	{ return createSymbol(sym.LOGICAL_AND, yytext()); }
"||"	{ return createSymbol(sym.LOGICAL_OR, yytext()); }
"="		{ return createSymbol(sym.ASSINGMENT, yytext()); }
"++"	{ return createSymbol(sym.INCREMENT, yytext()); }
"--"	{ return createSymbol(sym.DECREMENT, yytext()); }
";"		{ return createSymbol(sym.DELIMITER, yytext()); }
","		{ return createSymbol(sym.COMMA, yytext()); }
"."		{ return createSymbol(sym.DOT, yytext()); }
"("		{ return createSymbol(sym.LEFT_ROUND_BRACKET, yytext()); }
")"		{ return createSymbol(sym.RIGHT_ROUND_BRACKET, yytext()); }
"["		{ return createSymbol(sym.LEFT_SQUARE_BRACKET, yytext()); }
"]"		{ return createSymbol(sym.RIGHT_SQUARE_BRACKET, yytext()); }
"{"		{ return createSymbol(sym.LEFT_CURLY_BRACKET, yytext()); }
"}"		{ return createSymbol(sym.RIGHT_CURLY_BRACKET, yytext()); }

. { System.err.println("Invalid sequence '" + yytext() + "' at line " + (yyline + 1) + " at position " + (yycolumn + 1) + "."); }