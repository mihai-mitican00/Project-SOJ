package com.endava.tmd.bookclubproject.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository <Book, Long> {

    @Query("SELECT b FROM Book b WHERE b.title = ?1 AND b.author = ?2 AND b.edition = ?3")
    Optional<Book> findBooksByAllFields(final String title , final String author, final String edition);

}
