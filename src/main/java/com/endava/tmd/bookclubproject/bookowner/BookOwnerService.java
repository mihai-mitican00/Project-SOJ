package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookOwnerService {

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public List<BookOwner> getBooksAndOwners() {
        return bookOwnerRepository.findAll();
    }

    public ResponseEntity<String> addBookByUserId(final Long userId, final Optional<Book> bookOptional) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return HttpResponseUtilities.badRequest("User with given id does not exist.");
        }

        if (bookOptional.isEmpty() || hasIncompleteData(bookOptional)) {
            return HttpResponseUtilities.badRequest("Book has incomplete data, enter something on all fields!");
        }

        User user = userOptional.get();
        Book book = bookOptional.get();
        BookOwner bookOwner = new BookOwner(book, user);

        Optional<Book> bookAlreadyPresent = bookRepository
                .findByTitleAndAuthorAndEdition(
                        book.getTitle(),
                        book.getAuthor(),
                        book.getEdition()
                );

        //Check if book already exists in the virtual shelter
        if (bookAlreadyPresent.isPresent()) {
            //Verify that given user did not already added same book in the virtual shelter
            List<Book> booksOwnedByUser = bookOwnerRepository.findBooksOfUser(userId);
            if (booksOwnedByUser.contains(bookAlreadyPresent.get())) {
                return HttpResponseUtilities.badRequest("User with id " + user.getId() + " already added this book!");
            } else {
                bookOwner.setBook(bookAlreadyPresent.get());
                bookOwnerRepository.saveAndFlush(bookOwner);
            }
        } else {
            bookRepository.saveAndFlush(book);
            bookOwnerRepository.saveAndFlush(bookOwner);
        }

        return HttpResponseUtilities.insertSuccess("Book added with success!");
    }

    public ResponseEntity<String> deleteBookFromUser(final Long bookId, final Long ownerId) {

        Optional<Book> bookOptional = bookRepository.findById(bookId);
        Optional<User> ownerOptional = userRepository.findById(ownerId);

        if (bookOptional.isEmpty() || ownerOptional.isEmpty()) {
            return HttpResponseUtilities.badRequest("There is no book or owner with given id's.");
        }
        Book book = bookOptional.get();
        User owner = ownerOptional.get();

        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findByBookIdAndUserId(bookId, ownerId);
        if (bookOwnerOptional.isEmpty()) {
            return HttpResponseUtilities.badRequest("Given book does not belong to given owner.");
        }

        bookBorrowerRepository.deleteByBookIdAndOwnerId(bookId, ownerId);
        BookOwner bookOwner = bookOwnerOptional.get();
        bookOwnerRepository.delete(bookOwner);

        List<BookOwner> leftEntries = bookOwnerRepository.findAllByBookId(bookId);
        if (leftEntries.isEmpty()) {
            bookRepository.delete(book);
        }

        return HttpResponseUtilities.operationSuccess("Owner with user id "
                + owner.getUsername() + " deleted book " + book.getTitle() + " with success!");
    }

    private boolean hasIncompleteData(Optional<Book> bookOptional) {
        Book book = bookOptional.orElse(new Book());
        String[] bookData = {book.getTitle(), book.getAuthor(), book.getEdition()};
        return BooleanUtilities.anyNullParameters(bookData) || BooleanUtilities.anyEmptyString(bookData);
    }

}
