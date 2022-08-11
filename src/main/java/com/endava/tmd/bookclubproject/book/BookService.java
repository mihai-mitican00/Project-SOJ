package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> allBooksByTitleOrAuthor(final Optional<String> title, final Optional<String> author) {
        return bookRepository.findBooksByTitleOrAuthor(title, author);
    }

    public List<Book> getAllAvailableBooks() {
        return bookRepository.findAvailableBooks();
    }

    public List<User> getBookOwnersOfBook(final Long bookId) {
        return bookOwnerRepository.findOwnersOfBook(bookId);
    }

    public String formatAvailableBooks(final List<Book> availableBooks) {
        StringBuilder formattedResult = new StringBuilder();
        for (Book book : availableBooks) {
            String formattedBook = formatBook(book);


            if (!formattedResult.toString().contains(formattedBook)) {
                formattedResult.append(formattedBook).append("\n----------------------------\n");

            }
        }
        return formattedResult.toString();
    }

    public String formatBooksByTitleOrAuthor(final List<Book> booksByTitleOrAuthor) {

        List<Book> borrowedBooks = bookBorrowerRepository.findAllBorrowedBooks();
        List<LocalDate> returnDates = bookBorrowerRepository.findAllReturnDates();

        StringBuilder formattedResult = new StringBuilder();
        int i = 0;
        for (Book book : booksByTitleOrAuthor) {
            String formattedBook = formatBook(book);
            //make sure information doesn't repeat in result
            if (!formattedResult.toString().contains(formattedBook)) {
                formattedResult.append(formattedBook);
                if (borrowedBooks.contains(book)) {
                    formattedResult.append("\nAvailable From: ").append(returnDates.get(i)).append("\n-----------------------------\n");
                    borrowedBooks.remove(book);
                } else {
                    formattedResult.append("\nAvailable").append("\n-----------------------------\n");
                }

                i++;
            }

        }
        return formattedResult.toString();
    }

    private String formatBook(final Book book) {
        List<Long> allBookOwnersIds = bookOwnerRepository.findOwnersIdsOfBook(book.getId());
        //owner ids of the available book that did not rent the book yet.
        List<Long> availableOwnerIds = allBookOwnersIds
                .stream()
                .filter(ownerId -> bookBorrowerRepository.findByBookIdAndOwnerId(book.getId(), ownerId).isEmpty())
                .toList();

        StringBuilder bookString = new StringBuilder();
        bookString
                .append(book)
                .append("\nAvailable from users with id: ").append(availableOwnerIds);

        return bookString.toString();
    }

}
