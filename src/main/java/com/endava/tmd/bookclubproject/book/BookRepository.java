package com.endava.tmd.bookclubproject.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByTitleAndAuthorAndEdition(final String title, final String author, final String edition);

    @Query("SELECT bo.book FROM BookOwner bo WHERE bo.book NOT IN " +
            "(SELECT bb.book FROM BookBorrower bb WHERE bb.ownerId = bo.user.id)")
    List<Book> findAvailableBooks();


    List<Book> findAllByTitleOrAuthor(final String title, final String author);

}
