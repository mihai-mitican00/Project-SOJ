package com.endava.tmd.bookclubproject.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserPermissions {
    USER_READ("user:read"),
    USER_WRITE("user:write"),
    USER_DELETE("user:delete"),
    BOOK_READ("book:read"),
    BOOK_WRITE("book:write"),
    BOOK_RENT("book:rent"),
    BOOK_DELETE("book:delete");

    private final String permission;
}
