package com.jihll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class JIHLLLanguage {
    private static final VM vm = new VM();

    public static void main(String[] args) throws IOException {
        vm.defineNative("clock", (nArgs) -> (double) System.currentTimeMillis() / 1000.0);
        vm.defineNative("sqrt", (nArgs) -> Math.sqrt((Double) nArgs[0]));

        if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        String source = Files.readString(Paths.get(path));
        run(source);
    }

    private static void runPrompt() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("JIHLL Interactive Shell (Type 'exit' to quit)");
        System.out.println("------------------------------------------");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine();
            
            if (line.equals("exit")) break;
            if (line.trim().isEmpty()) continue;

            try {
                run(line);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void run(String source) {
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        Chunk chunk = new Chunk();
        Compiler compiler = new Compiler(chunk);
        compiler.compile(statements);

        vm.interpret(chunk);
    }
}