package cvetkovic;

import cvetkovic.lexer.Yylex;
import cvetkovic.parser.MJParser;
import cvetkovic.parser.ast.Program;
import cvetkovic.parser.ast.SyntaxNode;
import cvetkovic.semantics.SemanticAnalyzer;
import cvetkovic.util.SymbolTable;
import java_cup.runtime.Symbol;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Compiler {
    private static String inputFile = "", outputFile = "";
    private static boolean dumpAST = false, dumpSymbolTable = false, dumpDisassembly = false, run = false;
    private static boolean showHelp;

    public static void main(String[] args) throws Exception {
        boolean setInputFile = false, setOutputFile = false;

        for (String s : args) {
            if (s.toLowerCase().equals("-help")) {
                showHelp = true;
                break;
            }
            else if (s.toLowerCase().equals("-input")) {
                setInputFile = true;
                setOutputFile = false;
            }
            else if (s.toLowerCase().equals("-output")) {
                setOutputFile = true;
                setInputFile = false;
            }
            else if (s.toLowerCase().equals("-dump_ast"))
                dumpAST = true;
            else if (s.toLowerCase().equals("-dump_symbols"))
                dumpSymbolTable = true;
            else if (s.toLowerCase().equals("-dump_disassembly"))
                dumpDisassembly = true;
            else if (s.toLowerCase().equals("-run"))
                run = true;
            else {
                if (setInputFile)
                    inputFile = s;
                else if (setOutputFile)
                    outputFile = s;
                else
                    throw new RuntimeException("Error parsing command line arguments. Compilation aborted.");
            }
        }

        if (showHelp) {
            System.out.println("MikroJava Compiler v1.0\n" +
                    "--------------------------------------------------------\n" +
                    "Command line arguments:\n" +
                    "-help - show command line arguments help\n" +
                    "-input [PATH] - input MikroJava program to compile\n" +
                    "-output [PATH] - output MikroJava object file\n" +
                    "-dump_ast - dump abstract syntax tree\n" +
                    "-dump_symbols - dump symbol table\n" +
                    "-dump_disassembly - dump disassembled code\n" +
                    "-run - run generated object file upon compilation\n");

            return;
        }

        File inputFile = new File(Compiler.inputFile);
        if (!inputFile.exists()) {
            System.out.println("Specified input file doesn't exist.");
            return;
        }

        File outputFile = new File(Compiler.outputFile);

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            Yylex lexer = new Yylex(reader);
            MJParser parser = new MJParser(lexer);
            Symbol symbol = parser.parse();
            Program program = (Program) symbol.value;

            System.out.println("================ PARSER ================");
            if (dumpAST)
                System.out.println(program.toString(""));

            System.out.println("================ SEMANTIC ANALYZER ================");
            SyntaxNode syntaxNode = (SyntaxNode) (symbol.value);

            SemanticAnalyzer.SharedData sharedData = new SemanticAnalyzer.SharedData();
            SemanticAnalyzer semanticCheck = new SemanticAnalyzer(sharedData);
            syntaxNode.traverseBottomUp(semanticCheck);

            System.out.println("================ SYMBOL TABLE ================");
            if (dumpSymbolTable)
                SymbolTable.dump();

            System.out.println("================ INTERMEDIATE CODE GENERATOR ================");

            if (!parser.isErrorDetected() && !semanticCheck.isErrorDetected()) {

            }
            else if (parser.isErrorDetected())
                System.out.println("Error during syntax analysis. Semantic analyze and code generation aborted.");
            else if (semanticCheck.isErrorDetected())
                System.out.println("Error during semantic analysis. Code generation aborted.");
        }
    }
}