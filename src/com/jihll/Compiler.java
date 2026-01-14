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
                int idx = chunk.addConstant(0.0);
                chunk.write(Op.CONSTANT); 
                chunk.write(idx);
            }
            int nameIdx = chunk.addConstant(var.name.lexeme);
            chunk.write(Op.DEFINE_GLOBAL);
            chunk.write(nameIdx);
        } else if (stmt instanceof Stmt.If) {
            Stmt.If ifStmt = (Stmt.If) stmt;
            compile(ifStmt.condition);

            // Write JUMP_IF_FALSE with a placeholder for the offset
            chunk.write(Op.JUMP_IF_FALSE);
            int elseOffsetIndex = chunk.code.size();
            chunk.write(0); // placeholder

            // then-branch
            compile(ifStmt.thenBranch);

            // Write unconditional jump to skip else-branch
            chunk.write(Op.JUMP);
            int endOffsetIndex = chunk.code.size();
            chunk.write(0); // placeholder

            // Patch the JUMP_IF_FALSE placeholder to point to the start of else-branch
            int elseTarget = chunk.code.size();
            // runtime: after reading the placeholder, ip will be placeholderIndex+1,
            // so stored offset should be target - (placeholderIndex + 1)
            chunk.code.set(elseOffsetIndex, elseTarget - (elseOffsetIndex + 1));

            // else-branch
            if (ifStmt.elseBranch != null) compile(ifStmt.elseBranch);

            // Patch the JUMP placeholder to point to the end (after else-branch)
            int endTarget = chunk.code.size();
            chunk.code.set(endOffsetIndex, endTarget - (endOffsetIndex + 1));
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