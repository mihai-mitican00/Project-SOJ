package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
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
    @Id
    private Long id;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}
    )
    public Set<BookOwner> booksOwned = new HashSet<>();

    @JsonIgnore
    @OneToMany(
            mappedBy = "borrower",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}
    )
    public Set<BookBorrower> booksBorrowed = new HashSet<>();

    public User(){}

    public User(String firstName, String lastName, String username, String password, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public void addBook(Book book){
        BookOwner bookOwner = new BookOwner(book, this);
        booksOwned.add(bookOwner);
        book.getBookOwners().add(bookOwner);
    }

}
