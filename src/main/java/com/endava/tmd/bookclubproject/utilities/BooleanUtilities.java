package com.endava.tmd.bookclubproject.utilities;

import java.util.Arrays;
import java.util.Objects;

public interface BooleanUtilities {

    @SafeVarargs
    static <T> boolean anyNullElements(T... args){
        return Arrays.stream(args).anyMatch(Objects::isNull);
    }

    static boolean anyEmptyStringElements(String... args){
        return Arrays.stream(args).anyMatch(String::isEmpty);
    }
}
