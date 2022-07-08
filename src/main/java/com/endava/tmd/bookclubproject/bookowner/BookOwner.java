package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Fetch;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "book_owners")
public class BookOwner {

    @EmbeddedId
    private BookOwnerKey id;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;


    public BookOwner(Book book, User user){
        this.book = book;
        this.user = user;
        this.id = new BookOwnerKey(book.getId(), user.getId());
    }

}
