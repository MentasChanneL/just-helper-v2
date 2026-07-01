package com.prikolz.justhelper.util;

public interface ContextResolver<T, A> {
    T resolve(A value);
}
