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
        if (userOptional.isEmpty() || bookOptional.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        if (hasIncompleteData(bookOptional)) {
            return HttpResponseUtilities.notAcceptable("Book has incomplete data, enter something on all fields!");
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
                return HttpResponseUtilities.dataConflict("You already added this book to user with id " + user.getId() + "!");
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

    public ResponseEntity<String> deleteBookFromUser(final Optional<Long> bookId, final Optional<Long> ownerId) {
        if (BooleanUtilities.anyEmptyParameters(bookId, ownerId)) {
            return HttpResponseUtilities.wrongParameters();
        }

        Optional<Book> bookOptional = bookRepository.findById(bookId.orElse(0L));
        Optional<User> ownerOptional = userRepository.findById(ownerId.orElse(0L));
        if (bookOptional.isEmpty() || ownerOptional.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }
        Book book = bookOptional.get();
        User owner = ownerOptional.get();

        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findById(new BookOwnerKey(book.getId(), owner.getId()));
        if (bookOwnerOptional.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        bookBorrowerRepository.deleteByBookIdAndOwnerId(book.getId(), owner.getId());

        BookOwner bookOwner = bookOwnerOptional.get();
        bookOwnerRepository.delete(bookOwner);

        List<BookOwner> leftEntries = bookOwnerRepository.findAllByBookId(book.getId());
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
