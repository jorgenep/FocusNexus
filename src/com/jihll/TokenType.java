package com.jihll;

enum TokenType {
    // Keywords
    VAR, PRINT, IF, ELSE, WHILE, FUN, RETURN, CLASS, EXTENDS, THIS,
    TRUE, FALSE,
    
    // Primitive Types
    TYPE_INT, TYPE_DOUBLE, TYPE_BOOL, TYPE_STRING, TYPE_VOID,

    // Literals
    IDENTIFIER, STRING, NUMBER_INT, NUMBER_DOUBLE,
    
    // Operators
    PLUS, MINUS, STAR, SLASH, EQUAL, EQUAL_EQUAL, 
    BANG, BANG_EQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
    
    // Punctuation
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, 
    LEFT_BRACKET, RIGHT_BRACKET, // New: [ ] for Arrays
    COMMA, DOT, SEMICOLON, COLON,
    
    EOF
}