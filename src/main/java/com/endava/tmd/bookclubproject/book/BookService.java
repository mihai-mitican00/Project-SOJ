package com.endava.tmd.bookclubproject.book;

import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public List<Book> getBooks() {
        return bookRepository.findAll();
    }

    public ResponseEntity<String> allBooksByTitleOrAuthor(final Optional<String> title, final Optional<String> author) {

        List<Book> booksByTitleOrAuthor = bookOwnerRepository.findBooksByTitleOrAuthor(title, author);
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

    public List<Book> getAllAvailableBooks() {
        List<Book> ownedBooks = bookOwnerRepository.findAllOwnedBooks();
        List<Book> borrowedBooks = bookBorrowerRepository.findAllBorrowedBooks();
        List<Book> availableBooks = new ArrayList<>();

        for (Book book : ownedBooks) {
            long ownedBookCount = ownedBooks.stream().filter(b -> b.equals(book)).count();
            long borrowedBookCount = borrowedBooks.stream().filter(b -> b.equals(book)).count();

            if (ownedBookCount > borrowedBookCount && !availableBooks.contains(book)) {
                availableBooks.add(book);
            }
        }

        return availableBooks;
    }

    public List<User> getBookOwnersOfBook(final Long bookId) {
        return bookOwnerRepository.findOwnersOfBook(bookId);
    }
}
