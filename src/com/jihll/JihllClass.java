package com.jihll;
import java.util.HashMap;
import java.util.Map;

class JihllClass {
    final String name;
    final Map<String, JihllFunction> methods = new HashMap<>();

    JihllClass(String name) { this.name = name; }
    
    JihllFunction findMethod(String name) {
        if (methods.containsKey(name)) return methods.get(name);
        return null;
    }
    @Override public String toString() { return name; }
}