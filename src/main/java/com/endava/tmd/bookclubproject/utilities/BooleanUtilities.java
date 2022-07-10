package com.endava.tmd.bookclubproject.utilities;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface BooleanUtilities {

    @SafeVarargs
    static <T> boolean anyEmptyParameters(final Optional<T>... args){
        return Arrays.stream(args).anyMatch(Optional::isEmpty);
    }
    @SafeVarargs
    static<T> boolean anyNullParameters(final T... args){
        return Arrays.stream(args).anyMatch(Objects::isNull);
    }

    static boolean anyEmptyString(final String... args){
        return Arrays.stream(args).anyMatch(String::isEmpty);
    }

    static <T> boolean emptyList(final List<T> list){
        return list.isEmpty();
    }

}
