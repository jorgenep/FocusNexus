package com.jihll;

import java.util.ArrayList;
import java.util.List;

class Chunk {
    final List<Integer> code = new ArrayList<>();
    final List<Object> constants = new ArrayList<>();

    void write(int byteCode) {
        code.add(byteCode);
    }

    int addConstant(Object value) {
        constants.add(value);
        return constants.size() - 1;
    }
}