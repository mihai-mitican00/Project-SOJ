package com.endava.tmd.bookclubproject.book;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository <Book, Long> {
        Optional<Book> findByTitleAndAuthorAndEdition(final String title , final String author, final String edition);
}
