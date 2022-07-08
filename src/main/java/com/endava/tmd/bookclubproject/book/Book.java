package com.endava.tmd.bookclubproject.book;

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
@Table(name = "books")
public class Book {

    @SequenceGenerator(
            name = "book_sequence",
            sequenceName = "book_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "book_sequence"
    )
    @Id
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String author;
    @Column(nullable = false)
    private String edition;

    @JsonIgnore
    @OneToMany(
            mappedBy = "book",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private Set<BookOwner> bookOwners = new HashSet<>();


    @JsonIgnore
    @OneToMany(
            mappedBy = "book",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}
    )
    private Set<BookBorrower> bookBorrowers = new HashSet<>();

    public Book() {
    }

    public Book(final String title, final String author, final String edition) {
        this.title = title;
        this.author = author;
        this.edition = edition;
    }

    @Override
    public String toString(){
        return  "BookID:"+this.getId()
                +"\nTitle:"+this.getTitle()
                +"\nAuthor:" + this.getAuthor()
                + "\nEdition:"+ this.getEdition();
    }

}
