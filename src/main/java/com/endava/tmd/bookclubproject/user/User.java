package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

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
    @Schema(description = "Password of the User", example = "ionci321")
    private String password;
    @Column(nullable = false, unique = true)
    @Schema(description = "Email of the User", example = "ionci@gmail.com")
    private String email;

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

    public User(final String firstName,final String lastName,final String username,final String password,final String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
    }

}
