package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.book.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface BookBorrowerRepository extends JpaRepository<BookBorrower, BookBorrowerId> {
    Optional<BookBorrower> findByBookIdAndBorrowerId(final Long bookId, final Long borrowerId);
    Optional<BookBorrower> findByBookIdAndOwnerId(final Long bookId, final Long ownerId);
    List<BookBorrower> findAllByOwnerId(final Long ownerId);
    List<BookBorrower> findAllByBorrowerId(final Long borrowerId);

    @Query(value = "SELECT bookBorrower.book FROM BookBorrower bookBorrower")
    List<Book> findAllBorrowedBooks();

    @Query(value = "SELECT bookBorrower.returnDate FROM BookBorrower bookBorrower")
    List<LocalDate> findAllReturnDates();


    @Transactional
    void deleteByBookIdAndOwnerId(final Long bookId, final Long ownerId);
    @Transactional
    void deleteAllByBorrowerId(final Long borrowerId);
    @Transactional
    void deleteAllByOwnerId(final Long ownerId);
}
