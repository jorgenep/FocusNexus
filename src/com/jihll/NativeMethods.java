package com.jihll;

@FunctionalInterface
interface NativeMethod {
    Object invoke(Object[] args);
}