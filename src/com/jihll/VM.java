package com.jihll;

import java.util.HashMap;
import java.util.Map;

class VM {
    private final double[] stack = new double[256]; 
    private final Object[] objectStack = new Object[256]; 
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
            //System.out.println("TRACE ip=" + (ip-1) + " instr=" + instruction + " sp=" + sp);
            switch (instruction) {
                case Op.RETURN: return;
                case Op.CONSTANT:
                    int constantIndex = readByte();
                    Object val = chunk.constants.get(constantIndex);
                    push(val);
                    break;
                case Op.PRINT: 
                    System.out.println(pop()); 
                    break;
                case Op.POP: 
                    pop(); 
                    break;
                case Op.DEFINE_GLOBAL:
                    String defName = (String) chunk.constants.get(readByte());
                    globals.put(defName, pop());
                    break;
                case Op.GET_GLOBAL:
                    String getName = (String) chunk.constants.get(readByte());
                    if (!globals.containsKey(getName)) throw new RuntimeException("Undefined var " + getName);
                    push(globals.get(getName));
                    break;
                case Op.ADD: 
                    binaryOp((a, b) -> a + b); 
                    break;
                case Op.SUBTRACT: 
                    binaryOp((a, b) -> a - b); 
                    break;
                case Op.MULTIPLY: 
                    binaryOp((a, b) -> a * b); 
                    break;
                case Op.DIVIDE: 
                    binaryOp((a, b) -> a / b); 
                    break;
                case Op.LESS: 
                    binaryOp((a, b) -> (a < b) ? 1.0 : 0.0); 
                    break;
                case Op.GREATER: 
                    binaryOp((a, b) -> (a > b) ? 1.0 : 0.0); 
                    break;
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
                case Op.CALL:
                    int argCount = readByte();
                    Object callee = objectStack[sp - 1 - argCount];
                    if (callee instanceof NativeMethod) {
                        NativeMethod nativeFunc = (NativeMethod) callee;
                        Object[] args = new Object[argCount];
                        for (int i = argCount - 1; i >= 0; i--) args[i] = pop();
                        pop();
                        push(nativeFunc.invoke(args));
                    }
                    break;
            }
        }
    }

    private int readByte() { return chunk.code.get(ip++); }
    private void push(Object value) { objectStack[sp++] = value; }
    private Object pop() {
        if (sp == 0) {
            throw new RuntimeException("Stack underflow at ip=" + ip + " sp=" + sp + " code=" + chunk.code);
        }
        return objectStack[--sp];
    }
    
    private void binaryOp(java.util.function.DoubleBinaryOperator op) {
        double b = (Double) pop();
        double a = (Double) pop();
        push(op.applyAsDouble(a, b));
    }
    
    private boolean isFalsey(Object o) {
        if (o == null) return true;
        if (o instanceof Double) return (Double)o == 0.0;
        return false;
    }
}