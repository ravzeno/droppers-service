package com.ravzeno.droppers_service.commons.utils;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class NullSafeUtils {

    private NullSafeUtils() {}

    public static <T, R> R get(T obj, Function<T, R> getter) {
        return obj != null ? getter.apply(obj) : null;
    }

    public static <T, R> R getOrDefault(T obj, Function<T, R> getter, R defaultValue) {
        return obj != null ? getter.apply(obj) : defaultValue;
    }

    @SafeVarargs
    public static <T> String joinNonBlank(String delimiter, T obj, Function<T, String>... getters) {
        return Arrays.stream(getters)
                .map(g -> g.apply(obj))
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(delimiter));
    }
}
