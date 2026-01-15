package com.jihll;

class JihllFunction {
    final String name;
    final int arity;
    final int address;

    JihllFunction(String name, int arity, int address) {
        this.name = name;
        this.arity = arity;
        this.address = address;
    }

    @Override
    public String toString() { return "<fn " + name + ">"; }
}