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

        if (title.isEmpty() || author.isEmpty()) {
            return HttpResponseUtilities.wrongParameters();
        }

        List<Book> booksByTitleOrAuthor = bookOwnerRepository
                .findAll()
                .stream()
                .map(BookOwner::getBook)
                .filter(book -> (book.getTitle().equals(title.get()) ||
                        book.getAuthor().equals(author.get())))
                .toList();


        if (BooleanUtilities.emptyList(booksByTitleOrAuthor)) {
            return HttpResponseUtilities.noContentFound();
        }

        List<BookBorrower> entriesList = bookBorrowerRepository.findAll();
        List<Book> borrowedBooks = entriesList.stream().map(BookBorrower::getBook).collect(Collectors.toList());
        List<LocalDate> returnDates = entriesList.stream().map(BookBorrower::getReturnDate).toList();

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

        return HttpResponseUtilities.operationWasDone(message.toString());
    }

    public List<Book> getAllAvailableBooks() {
        List<Book> ownedBooks =
                bookOwnerRepository
                        .findAll()
                        .stream()
                        .map(BookOwner::getBook)
                        .toList();


        List<Book> borrowedBooks =
                bookBorrowerRepository
                        .findAll()
                        .stream()
                        .map(BookBorrower::getBook)
                        .toList();

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

    public List<User> getBookOwners(Long bookId) {
        List<BookOwner> bookOwnerEntries = bookOwnerRepository.findAll();
        Book book = bookRepository.findById(bookId).orElse(null);

        return bookOwnerEntries.stream()
                .filter(bo -> bo.getBook().equals(book))
                .map(BookOwner::getUser)
                .toList();
    }
}
