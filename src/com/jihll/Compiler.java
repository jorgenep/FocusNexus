package com.jihll;

import java.util.List;

class Compiler {
    private final Chunk chunk;

    Compiler(Chunk chunk) {
        this.chunk = chunk;
    }

    void compile(List<Stmt> statements) {
        for (Stmt statement : statements) {
            compile(statement);
        }
        chunk.write(Op.RETURN); 
    }

    private void compile(Stmt stmt) {
        if (stmt instanceof Stmt.Print) {
            compile(((Stmt.Print) stmt).expression);
            chunk.write(Op.PRINT);
        } else if (stmt instanceof Stmt.Var) {
            Stmt.Var var = (Stmt.Var) stmt;
            if (var.initializer != null) {
                compile(var.initializer);
            } else {
                int idx = chunk.addConstant(0); // Default int 0
                chunk.write(Op.CONSTANT); 
                chunk.write(idx);
            }
            int nameIdx = chunk.addConstant(var.name.lexeme);
            chunk.write(Op.DEFINE_GLOBAL);
            chunk.write(nameIdx);
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
        } else if (stmt instanceof Stmt.Block) {
             for (Stmt s : ((Stmt.Block) stmt).statements) compile(s);
        } else if (stmt instanceof Stmt.Expression) {
            compile(((Stmt.Expression) stmt).expression);
            chunk.write(Op.POP);
        }
    }

    private void compile(Expr expr) {
        if (expr instanceof Expr.Literal) {
            int index = chunk.addConstant(((Expr.Literal) expr).value);
            chunk.write(Op.CONSTANT);
            chunk.write(index);
        } else if (expr instanceof Expr.Variable) {
            int nameIdx = chunk.addConstant(((Expr.Variable) expr).name.lexeme);
            chunk.write(Op.GET_GLOBAL);
            chunk.write(nameIdx);
        } else if (expr instanceof Expr.Call) {
            Expr.Call call = (Expr.Call) expr;
            compile(call.callee);
            for (Expr arg : call.arguments) compile(arg);
            chunk.write(Op.CALL);
            chunk.write(call.arguments.size());
        } else if (expr instanceof Expr.Array) {
            // Compile Array: Push elements, then OpCode with count
            Expr.Array array = (Expr.Array) expr;
            for (Expr element : array.elements) {
                compile(element);
            }
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
            }
        }
    }
}