package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.registration.token.ConfirmationToken;
import com.endava.tmd.bookclubproject.security.UserRoles;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "user_sequence"
    )
    @Schema(description = "Unique id, auto-generated.", example = "5")
    @Id
    private Long id;
    @Column(nullable = false)
    @Schema(description = "First name of the User", example = "Ionci")
    private String firstName;
    @Column(nullable = false)
    @Schema(description = "Last name of the User", example = "Georgian")
    private String lastName;
    @Column(nullable = false, unique = true)
    @Schema(description = "Username of the User", example = "ionci123")
    private String username;
    @Column(nullable = false)
    @Schema(description = "Password of the User", example = "asd")
    private String password;
    @Column(nullable = false, unique = true)
    @Schema(description = "Email of the User", example = "ionci@endava.com")
    private String email;

    @Enumerated(EnumType.STRING)
    private UserRoles role;

    private boolean enabled = false;


    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<BookOwner> booksOwned = new ArrayList<>();

    @JsonIgnore
    @OneToMany(
            mappedBy = "borrower",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private List<BookBorrower> booksBorrowed = new ArrayList<>();

    public User(final String firstName,final String lastName,final String username,final String password,final String email,UserRoles role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getGrantedAuthorities();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
