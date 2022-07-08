package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface BookOwnerRepository extends JpaRepository <BookOwner, BookOwnerKey> {

    @Query(value = "SELECT * FROM book_owners WHERE user_id = ?1", nativeQuery = true)
    List<BookOwner> getEntriesByUserId(final Long userId);

    @Query(value = "SELECT * FROM book_owners WHERE book_id = ?1", nativeQuery = true)
    List<BookOwner> getEntriesByBookId(final Long bookId);

}
