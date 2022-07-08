package com.endava.tmd.bookclubproject.utilities;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.user.User;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface BooleanUtilities {

    @SafeVarargs
    static <T> boolean anyEmptyParameters(final Optional<T>... args){
        return Arrays.stream(args).anyMatch(Optional::isEmpty);
    }

    @SafeVarargs
    static <T> boolean allEmptyParameters(final Optional<T>... args){
        return Arrays.stream(args).allMatch(Optional::isEmpty);
    }

    static <T> boolean emptyList(final List<T> list){
        return list.isEmpty();
    }

}
