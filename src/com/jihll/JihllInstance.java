package com.jihll;
import java.util.HashMap;
import java.util.Map;

class JihllInstance {
    final JihllClass klass;
    final Map<String, Object> fields = new HashMap<>();

    JihllInstance(JihllClass klass) { this.klass = klass; }

    @Override public String toString() { return klass.name + " instance"; }
}