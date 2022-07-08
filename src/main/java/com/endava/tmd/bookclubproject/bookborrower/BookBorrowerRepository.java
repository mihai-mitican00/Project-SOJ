package com.endava.tmd.bookclubproject.bookborrower;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("SqlDialectInspection")
@Repository
public interface BookBorrowerRepository extends JpaRepository<BookBorrower, BookBorrowerId> {

    @Query("SELECT bb FROM BookBorrower bb WHERE bb.bookBorrowerId.bookId = ?1 AND bb.ownerId = ?2")
    Optional<BookBorrower> findEntryByBookAndOwner(final Long bookId, final Long ownerId);

    @Query("SELECT bb FROM BookBorrower bb WHERE bb.bookBorrowerId.bookId = ?1 AND bb.bookBorrowerId.borrowerId = ?2")
    Optional<BookBorrower> findEntryByBookAndBorrower(final Long bookId, final Long borrowerId);

    @Query(value = "SELECT * FROM book_borrowers WHERE owner_id = ?1" , nativeQuery = true)
    List<BookBorrower> findBooksThatUserGave(final Long ownerId);

    @Query(value = "SELECT * FROM book_borrowers WHERE borrower_id = ?1" , nativeQuery = true)
    List<BookBorrower> findBooksThatUserRented(final Long borrowerId);

}
