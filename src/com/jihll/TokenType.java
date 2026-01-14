package com.jihll;

enum TokenType {
    // Keywords
    VAR, PRINT, IF, ELSE, WHILE, FUN, RETURN, CLASS, EXTENDS, THIS,
    TRUE, FALSE,
    
    // Literals
    IDENTIFIER, STRING, NUMBER,
    
    // Operators
    PLUS, MINUS, STAR, SLASH, EQUAL, EQUAL_EQUAL, 
    BANG, BANG_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    
    // Punctuation
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, 
    COMMA, DOT, SEMICOLON,
    
    EOF
}