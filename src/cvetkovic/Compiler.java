package cvetkovic;

import cvetkovic.ir.IRCodeGenerator;
import cvetkovic.ir.optimizations.IROptimizer;
import cvetkovic.ir.quadruple.Quadruple;
import cvetkovic.lexer.Yylex;
import cvetkovic.parser.MJParser;
import cvetkovic.parser.ast.Program;
import cvetkovic.parser.ast.SyntaxNode;
import cvetkovic.semantics.SemanticAnalyzer;
import cvetkovic.structures.SymbolTable;
import cvetkovic.x64.AssemblyGenerator;
import java_cup.runtime.Symbol;
import rs.etf.pp1.symboltable.concepts.Obj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

public class Compiler {
    private static String inputFile = "", outputFile = "";
    private static boolean dumpAST = false, dumpSymbolTable = false, dumpIR = false, run = false, optimize_ir = false, dump_asm = false;
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
            else if (s.toLowerCase().equals("-dump_ir"))
                dumpIR = true;
            else if (s.toLowerCase().equals("-optimize_ir"))
                optimize_ir = true;
            else if (s.toLowerCase().equals("-dump_asm"))
                dump_asm = true;
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
                    "\n" +
                    "\tAuthor: Lazar M. Cvetković (l.cvetkovic.997@gmail.com)\n" +
                    "--------------------------------------------------------\n" +
                    "Command line arguments:\n" +
                    "-help - show command line arguments help\n" +
                    "-input [PATH] - input MikroJava program to compile\n" +
                    "-output [PATH] - output MikroJava object file\n" +
                    "-dump_ast - dump abstract syntax tree\n" +
                    "-dump_symbols - dump symbol table\n" +
                    "-dump_ir - dump intermediate code\n" +
                    //"-optimize_ir - do optimizations on intermediate code\n" +
                    "-dump_asm - dump generated x86-64 code\n");

            return;
        }

        File inputFile = new File(Compiler.inputFile);
        if (!inputFile.exists()) {
            System.out.println("Specified input file doesn't exist.");
            return;
        }

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

            if (!parser.isErrorDetected() && !semanticCheck.isErrorDetected()) {
                IRCodeGenerator irCodeGenerator = new IRCodeGenerator();
                syntaxNode.traverseBottomUp(irCodeGenerator);

                System.out.println("================ INTERMEDIATE CODE GENERATION ================");
                List<List<Quadruple>> irCode = irCodeGenerator.getIRCodeOutput();
                List<Obj> functions = irCodeGenerator.getFunctionsObj();
                IROptimizer irCodeOptimizer = new IROptimizer(irCode, functions);
                String IRCodeToPrintPre = null;
                String IRCodeToPrintPost = null;

                IRCodeToPrintPre = irCodeOptimizer.toString();

                if (optimize_ir) {
                    System.out.println("================ INTERMEDIATE CODE OPTIMIZATION ================");
                    //irCodeOptimizer.executeOptimizations();
                }

                IRCodeToPrintPost = irCodeOptimizer.toString();

                if (dumpIR) {
                    System.out.println("================ INTERMEDIATE CODE BEFORE OPTIMIZER ================");
                    System.out.print(IRCodeToPrintPre);
                    if (optimize_ir) {
                        System.out.println("================ INTERMEDIATE CODE AFTER OPTIMIZER ================");
                        System.out.print(IRCodeToPrintPost);
                    }
                }

                System.out.println("================ MACHINE CODE GENERATION ================");
                AssemblyGenerator assemblyGenerator = new AssemblyGenerator(Compiler.outputFile,
                        irCodeOptimizer.getOptimizationOutput(),
                        semanticCheck.getGlobalVariables(),
                        semanticCheck.getClassMetadata());

                assemblyGenerator.generateCode();
                if (dump_asm) {
                    readOutputFile(Compiler.outputFile);
                }

                System.out.println("================ COMPILATION DONE ================");
            }
            else if (parser.isErrorDetected())
                System.out.println("Error during syntax analysis. Semantic analyze and code generation aborted.");
            else if (semanticCheck.isErrorDetected())
                System.out.println("Error during semantic analysis. Code generation aborted.");
        }
    }

    private static void readOutputFile(String outputFile) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(outputFile))) {
            String line;

            while ((line = reader.readLine()) != null)
                System.out.println(line);
        }
    }
}