package com.jihll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class VM {
    // UPDATED: Stack is now Object[] to hold Ints, Strings, Lists, etc.
    private final Object[] stack = new Object[256]; 
    private int sp = 0; 
    
    private final Map<String, Object> globals = new HashMap<>();
    private Chunk chunk;
    private int ip = 0;

    void defineNative(String name, NativeMethod method) {
        globals.put(name, method);
    }

    void interpret(Chunk chunk) {
        this.chunk = chunk;
        this.ip = 0;
        run();
    }

    private void run() {
        while (ip < chunk.code.size()) {
            int instruction = readByte();
            switch (instruction) {
                case Op.RETURN: return;
                
                case Op.CONSTANT:
                    int constantIndex = readByte();
                    Object val = chunk.constants.get(constantIndex);
                    push(val);
                    break;
                    
                case Op.PRINT: System.out.println(pop()); break;
                case Op.POP: pop(); break;
                
                case Op.DEFINE_GLOBAL:
                    String defName = (String) chunk.constants.get(readByte());
                    globals.put(defName, pop());
                    break;
                    
                case Op.GET_GLOBAL:
                    String getName = (String) chunk.constants.get(readByte());
                    if (!globals.containsKey(getName)) throw new RuntimeException("Undefined var " + getName);
                    push(globals.get(getName));
                    break;
                
                // Polymorphic Math Operations
                case Op.ADD: {
                    Object b = pop();
                    Object a = pop();
                    if (a instanceof String || b instanceof String) {
                        push(String.valueOf(a) + String.valueOf(b));
                    } else if (a instanceof Double || b instanceof Double) {
                        push(toDouble(a) + toDouble(b));
                    } else if (a instanceof Integer && b instanceof Integer) {
                        push((Integer)a + (Integer)b);
                    } else {
                        throw new RuntimeException("Invalid operand types for +");
                    }
                    break;
                }
                case Op.SUBTRACT: {
                    Object b = pop();
                    Object a = pop();
                    if (a instanceof Double || b instanceof Double) push(toDouble(a) - toDouble(b));
                    else push((Integer)a - (Integer)b);
                    break;
                }
                case Op.MULTIPLY: {
                    Object b = pop();
                    Object a = pop();
                    if (a instanceof Double || b instanceof Double) push(toDouble(a) * toDouble(b));
                    else push((Integer)a * (Integer)b);
                    break;
                }
                case Op.DIVIDE: {
                    Object b = pop();
                    Object a = pop();
                    push(toDouble(a) / toDouble(b)); // Always double division
                    break;
                }
                
                case Op.LESS: {
                    Object b = pop();
                    Object a = pop();
                    push(toDouble(a) < toDouble(b)); // Boolean result
                    break;
                }
                case Op.GREATER: {
                    Object b = pop();
                    Object a = pop();
                    push(toDouble(a) > toDouble(b));
                    break;
                }
                
                case Op.JUMP_IF_FALSE: {
                    int offset = readByte();
                    if (isFalsey(pop())) ip += offset;
                    break;
                }
                
                case Op.JUMP: {
                    int offset = readByte();
                    ip += offset;
                    break;
                }
                
                case Op.BUILD_LIST: {
                    int count = readByte();
                    List<Object> list = new ArrayList<>();
                    // Pop in reverse order to maintain array order
                    for (int i = 0; i < count; i++) {
                        list.add(null); 
                    }
                    for (int i = count - 1; i >= 0; i--) {
                        list.set(i, pop());
                    }
                    push(list);
                    break;
                }
                
                case Op.CALL:
                    int argCount = readByte();
                    Object callee = stack[sp - 1 - argCount];
                    if (callee instanceof NativeMethod) {
                        NativeMethod nativeFunc = (NativeMethod) callee;
                        Object[] args = new Object[argCount];
                        for (int i = argCount - 1; i >= 0; i--) args[i] = pop();
                        pop(); // Pop function
                        push(nativeFunc.invoke(args));
                    }
                    break;
            }
        }
    }

    private int readByte() { return chunk.code.get(ip++); }
    private void push(Object value) { stack[sp++] = value; }
    private Object pop() { return stack[--sp]; }
    
    private double toDouble(Object o) {
        if (o instanceof Double) return (Double) o;
        if (o instanceof Integer) return ((Integer) o).doubleValue();
        throw new RuntimeException("Expected number, got " + o.getClass().getSimpleName());
    }
    
    private boolean isFalsey(Object o) {
        if (o == null) return true;
        if (o instanceof Boolean) return !(Boolean)o;
        if (o instanceof Integer) return (Integer)o == 0;
        if (o instanceof Double) return (Double)o == 0.0;
        return false;
    }
}