package com.jihll;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class JIHLLLanguage {
    private static final VM vm = new VM();

    public static void main(String[] args) throws IOException {        
        vm.defineNative("clock", (argsArr) -> (double) System.currentTimeMillis() / 1000.0);
        
        vm.defineNative("write", (argsArr) -> {
            System.out.print(argsArr[0]);
            return null;
        });

        vm.defineNative("input", (argsArr) -> {
            if (argsArr.length > 0) System.out.print(argsArr[0]);
            return new Scanner(System.in).nextLine();
        });

        vm.defineNative("exit", (argsArr) -> {
            int code = (argsArr.length > 0 && argsArr[0] instanceof Double) 
                    ? ((Double) argsArr[0]).intValue() : 0;
            System.exit(code);
            return null;
        });
        
        vm.defineNative("sleep", (argsArr) -> {
            try {
                long ms = ((Double) argsArr[0]).longValue();
                Thread.sleep(ms);
            } catch (InterruptedException e) { }
            return null;
        });

        vm.defineNative("abs", (argsArr) -> Math.abs((Double) argsArr[0]));
        
        vm.defineNative("ceil", (argsArr) -> Math.ceil((Double) argsArr[0]));
        
        vm.defineNative("floor", (argsArr) -> Math.floor((Double) argsArr[0]));
        
        vm.defineNative("max", (argsArr) -> Math.max((Double) argsArr[0], (Double) argsArr[1]));
        
        vm.defineNative("min", (argsArr) -> Math.min((Double) argsArr[0], (Double) argsArr[1]));
        
        vm.defineNative("pow", (argsArr) -> Math.pow((Double) argsArr[0], (Double) argsArr[1]));
        
        vm.defineNative("sqrt", (argsArr) -> Math.sqrt((Double) argsArr[0]));
        
        vm.defineNative("random", (argsArr) -> Math.random());

        vm.defineNative("str", (argsArr) -> String.valueOf(argsArr[0]));

        vm.defineNative("num", (argsArr) -> Double.parseDouble(argsArr[0].toString()));
        
        vm.defineNative("len", (argsArr) -> (double) argsArr[0].toString().length());

        vm.defineNative("typeof", (argsArr) -> {
            Object o = argsArr[0];
            if (o == null) return "nil";
            if (o instanceof Double) return "number";
            if (o instanceof Integer) return "number";
            if (o instanceof String) return "string";
            if (o instanceof Boolean) return "boolean";
            return "object";
        });

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