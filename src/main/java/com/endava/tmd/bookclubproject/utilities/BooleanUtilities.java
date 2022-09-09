package com.endava.tmd.bookclubproject.utilities;

import java.lang.reflect.Array;
import java.util.*;

public interface BooleanUtilities {

    static <T> boolean anyNullElements(T... args){
        return Arrays.stream(args).anyMatch(Objects::isNull);
    }

    static boolean anyEmptyStringElements(String... args){
        return Arrays.stream(args).anyMatch(s -> (s.isEmpty() || s.isBlank()));
    }
}
