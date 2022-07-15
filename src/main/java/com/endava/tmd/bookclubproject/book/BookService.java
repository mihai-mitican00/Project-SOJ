package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    public ResponseEntity<List<Book>>getAllBooks() {
        List<Book> books = bookRepository.findAll();
        if (BooleanUtilities.emptyList(books)) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(books);
    }

    public ResponseEntity<String> allBooksByTitleOrAuthor(final Optional<String> title, final Optional<String> author) {

        List<Book> booksByTitleOrAuthor = bookRepository.findBooksByTitleOrAuthor(title, author);
        if (BooleanUtilities.emptyList(booksByTitleOrAuthor)) {
            return HttpResponseUtilities.noContentFound();
        }

        List<Book> borrowedBooks = bookBorrowerRepository.findAllBorrowedBooks();
        List<LocalDate> returnDates = bookBorrowerRepository.findAllReturnDates();


        StringBuilder message = new StringBuilder();
        int i = 0;
        for (Book book : booksByTitleOrAuthor) {
            message.append(book.toString());
            if (borrowedBooks.contains(book)) {
                message.append("\nAvailable From: ").append(returnDates.get(i)).append("\n-----------------------\n");
                borrowedBooks.remove(book);
            } else {
                message.append("\nAvailable").append("\n-----------------------\n");
            }
            i++;
        }

        return HttpResponseUtilities.operationSuccess(message.toString());
    }

    public ResponseEntity<String> getAllAvailableBooks() {
        List<Book> availableBooks = bookRepository.findAvailableBooks();

        if (BooleanUtilities.emptyList(availableBooks)) {
            return HttpResponseUtilities.noContentFound();
        }
        StringBuilder message = new StringBuilder();
        availableBooks.forEach(
                book -> message
                        .append(book.toString())
                        .append("\n-----------------------------\n")
        );
        return HttpResponseUtilities.operationSuccess(message.toString());
    }

    public ResponseEntity<List<User>> getBookOwnersOfBook(final Long bookId) {
        List<User> bookOwners = bookOwnerRepository.findOwnersOfBook(bookId);
        if (BooleanUtilities.emptyList(bookOwners)) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(bookOwners);
    }
}
