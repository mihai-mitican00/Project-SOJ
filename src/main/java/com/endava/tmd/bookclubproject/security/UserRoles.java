package com.endava.tmd.bookclubproject.security;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static com.endava.tmd.bookclubproject.security.UserPermissions.*;

@AllArgsConstructor
@Getter
public enum UserRoles {
    ADMIN(Sets.newHashSet(USER_READ, USER_WRITE, USER_DELETE, BOOK_READ, BOOK_WRITE, BOOK_DELETE)),
    USER(Sets.newHashSet(USER_READ, USER_WRITE, BOOK_WRITE, BOOK_READ, BOOK_RENT, BOOK_DELETE));

    private final Set<UserPermissions> userPermissions;

    public Set<SimpleGrantedAuthority> getGrantedAuthorities() {
        Set<SimpleGrantedAuthority> grantedAuthorities
                = userPermissions.stream()
                .map(userPermission -> new SimpleGrantedAuthority(userPermission.getPermission()))
                .collect(Collectors.toSet());
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return grantedAuthorities;
    }
}
