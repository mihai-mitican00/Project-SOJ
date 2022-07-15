package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface BookOwnerRepository extends JpaRepository <BookOwner, BookOwnerId> {
    List<BookOwner> findAllByBookId(final Long bookId);

    @Query(value = "SELECT bo.user FROM  BookOwner bo WHERE bo.book.id = ?1")
    List<User> findOwnersOfBook(final Long bookId);
    @Query(value = "SELECT bo.book FROM  BookOwner bo WHERE bo.user.id = ?1")
    List<Book> findBooksOfUser(final Long userId);

    @Query(value = "SELECT bo.book FROM BookOwner bo")
    List<Book> findAllOwnedBooks();

    Optional<BookOwner> findByBookIdAndUserId(final Long bookId, final Long userId);
}
