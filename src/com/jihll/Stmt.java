package com.jihll;

import java.util.List;

abstract class Stmt {
    static class Expression extends Stmt {
        final Expr expression;
        Expression(Expr expression) { this.expression = expression; }
    }
    static class Print extends Stmt {
        final Expr expression;
        Print(Expr expression) { this.expression = expression; }
    }
    static class Var extends Stmt {
        final Token name;
        final Expr initializer;
        Var(Token name, Expr initializer) { this.name = name; this.initializer = initializer; }
    }
    static class Block extends Stmt {
        final List<Stmt> statements;
        Block(List<Stmt> statements) { this.statements = statements; }
    }
    static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;
        If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition; this.thenBranch = thenBranch; this.elseBranch = elseBranch;
        }
    }
}