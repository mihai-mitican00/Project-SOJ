package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(hidden = true)
    private Long id;
    @Column(nullable = false)
    @Schema(description = "Title of the book", example = "The Lord of the Rings")
    private String title;
    @Column(nullable = false)
    @Schema(description = "Author of the book", example = "J.R.R. Tolkien")
    private String author;
    @Column(nullable = false)
    @Schema(description = "Edition of the book", example = "I")
    private String edition;

    @JsonIgnore
    @OneToMany(
            mappedBy = "book",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}
    )
    private Set<BookOwner> bookOwners = new HashSet<>();

    @JsonIgnore
    @OneToMany(
            mappedBy = "book",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}
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
