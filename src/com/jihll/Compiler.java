package com.jihll;

import java.util.List;

class Compiler {
    private final Chunk chunk;

    Compiler(Chunk chunk) { this.chunk = chunk; }

    void compile(List<Stmt> statements) {
        for (Stmt statement : statements) compile(statement);
        chunk.write(Op.RETURN); 
    }

    private void compile(Stmt stmt) {
        if (stmt instanceof Stmt.Print) {
            compile(((Stmt.Print) stmt).expression);
            chunk.write(Op.PRINT);
        } else if (stmt instanceof Stmt.Expression) {
            compile(((Stmt.Expression) stmt).expression);
            chunk.write(Op.POP); 
        } else if (stmt instanceof Stmt.Function) {
            // COMPILING A FUNCTION
            Stmt.Function func = (Stmt.Function) stmt;
            
            // 1. Emit Jump to skip over function body during normal flow
            chunk.write(Op.JUMP);
            chunk.write(0xff); // Placeholder
            int jumpIdx = chunk.code.size() - 1;

            // 2. Record function start address
            int startAddress = chunk.code.size();
            
            // 3. Compile body
            for (Token param : func.params) {
                // In a real VM, we'd handle locals here. 
                // For this simple stack VM, we assume args are just on stack.
            }
            for (Stmt s : func.body) compile(s);
            
            // Ensure implicit return at end of function
            chunk.write(Op.RETURN);

            // 4. Patch the jump
            int endAddress = chunk.code.size();
            chunk.code.set(jumpIdx, endAddress - jumpIdx - 1);

            // 5. Create JihllFunction object and store in a Global Variable
            JihllFunction fnObj = new JihllFunction(func.name.lexeme, func.params.size(), startAddress);
            int constIdx = chunk.addConstant(fnObj);
            chunk.write(Op.CONSTANT);
            chunk.write(constIdx);
            
            int nameIdx = chunk.addConstant(func.name.lexeme);
            chunk.write(Op.SET_GLOBAL); // Define function name globally
            chunk.write(nameIdx);
            chunk.write(Op.POP); // SET_GLOBAL keeps value on stack, we pop it off

        } else if (stmt instanceof Stmt.If) {
            Stmt.If ifStmt = (Stmt.If) stmt;
            compile(ifStmt.condition);
            chunk.write(Op.JUMP_IF_FALSE);
            chunk.write(0xff); 
            int elseJump = chunk.code.size() - 1;
            compile(ifStmt.thenBranch);
            chunk.write(Op.JUMP);
            chunk.write(0xff);
            int endJump = chunk.code.size() - 1;
            chunk.code.set(elseJump, chunk.code.size() - 1 - elseJump);
            if (ifStmt.elseBranch != null) compile(ifStmt.elseBranch);
            chunk.code.set(endJump, chunk.code.size() - 1 - endJump);
        } else if (stmt instanceof Stmt.While) {
            Stmt.While whileStmt = (Stmt.While) stmt;
            int loopStart = chunk.code.size();
            compile(whileStmt.condition);
            chunk.write(Op.JUMP_IF_FALSE);
            chunk.write(0xff);
            int exitJump = chunk.code.size() - 1;
            compile(whileStmt.body);
            chunk.write(Op.JUMP);
            chunk.write(loopStart - (chunk.code.size() + 1));
            chunk.code.set(exitJump, chunk.code.size() - 1 - exitJump);
        } else if (stmt instanceof Stmt.Return) {
            Stmt.Return ret = (Stmt.Return) stmt;
            if (ret.value != null) compile(ret.value);
            else {
                int nullIdx = chunk.addConstant(null);
                chunk.write(Op.CONSTANT);
                chunk.write(nullIdx);
            }
            chunk.write(Op.RETURN);
        } else if (stmt instanceof Stmt.Block) {
             for (Stmt s : ((Stmt.Block) stmt).statements) compile(s);
        }
    }

    private void compile(Expr expr) {
        if (expr instanceof Expr.Assign) {
            Expr.Assign assign = (Expr.Assign) expr;
            compile(assign.value);
            int nameIdx = chunk.addConstant(assign.name.lexeme);
            chunk.write(Op.SET_GLOBAL);
            chunk.write(nameIdx);
        } else if (expr instanceof Expr.Call) {
            Expr.Call call = (Expr.Call) expr;
            
            // Check if this is a SPAWN command
            if (call.callee instanceof Expr.Variable && 
                ((Expr.Variable)call.callee).name.type == TokenType.SPAWN) {
                 // It's a spawn! "spawn func()" -> func is first arg
                 // The parser treats "spawn" as the function name in a call structure if used like spawn(x)
                 // But our parser likely parsed "spawn" as a Keyword if we added it to Lexer properly.
                 // Actually, "spawn fn()" parsing might need tweaking if spawn is a keyword.
                 // Assuming parser handles "spawn fn(args)" similar to "call fn(args)"
            }

            compile(call.callee);
            for (Expr arg : call.arguments) compile(arg);
            
            // If the callee was actually the "spawn" keyword, emit SPAWN
            // Since SPAWN is a keyword, the Parser might have handled it.
            // Simplified: We assume standard CALL opcode unless we detect specific syntax.
            // For now, let's assume the user types: spawn myFunc(arg)
            // And we handle "spawn" specially here? 
            // Better: Add a SPAWN statement in Parser?
            // For THIS implementation: use Op.CALL unless specifically handled.
            
            chunk.write(Op.CALL);
            chunk.write(call.arguments.size());
        } 
        // ... (Include other expressions: Literal, Variable, Binary, Array from previous steps)
        else if (expr instanceof Expr.Literal) {
            int index = chunk.addConstant(((Expr.Literal) expr).value);
            chunk.write(Op.CONSTANT);
            chunk.write(index);
        } else if (expr instanceof Expr.Variable) {
            int nameIdx = chunk.addConstant(((Expr.Variable) expr).name.lexeme);
            chunk.write(Op.GET_GLOBAL);
            chunk.write(nameIdx);
        } else if (expr instanceof Expr.Array) {
            Expr.Array array = (Expr.Array) expr;
            for (Expr element : array.elements) compile(element);
            chunk.write(Op.BUILD_LIST);
            chunk.write(array.elements.size());
        } else if (expr instanceof Expr.Binary) {
            Expr.Binary binary = (Expr.Binary) expr;
            compile(binary.left);
            compile(binary.right);
            switch (binary.operator.type) {
                case PLUS:  chunk.write(Op.ADD); break;
                case MINUS: chunk.write(Op.SUBTRACT); break;
                case STAR:  chunk.write(Op.MULTIPLY); break;
                case SLASH: chunk.write(Op.DIVIDE); break;
                case LESS:  chunk.write(Op.LESS); break;
                case GREATER: chunk.write(Op.GREATER); break;
                case EQUAL_EQUAL: chunk.write(Op.EQUAL); break;
            }
        }
    }
}