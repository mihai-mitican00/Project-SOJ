package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor
@AllArgsConstructor

@Entity
@Table(name = "book_owners")
public class BookOwner {

    @Schema(description = "Book id and owner id as unique composed key")
    @EmbeddedId
    private BookOwnerId id;

    @ManyToOne
    @MapsId("bookId")
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    public BookOwner(final Book book, final User user) {
        this.book = book;
        this.user = user;
        this.id = new BookOwnerId(book.getId(), user.getId());
    }

}
