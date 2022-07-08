package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerService;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import com.endava.tmd.bookclubproject.user.UserService;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookOwnerService {

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookBorrowerService bookBorrowerService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    public List<BookOwner> getBooksAndOwners() {
        return bookOwnerRepository.findAll();
    }

    public List<Book> getBooksOwnedByUser(final Long userId){
        List<BookOwner> bookOwnersEntries = bookOwnerRepository.getEntriesByUserId(userId);
        return bookOwnersEntries.stream()
                .filter(bookOwner -> bookOwner.getUser().getId().equals(userId))
                .map(BookOwner::getBook)
                .toList();

    }

    public ResponseEntity<String> addBookByUserId(final Long userId, final Optional<Book> bookOptional) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty() || bookOptional.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        User user = userOptional.get();
        Book book = bookOptional.get();
        BookOwner bookOwner = new BookOwner(book, user);

        Optional<Book> bookAlreadyPresent = bookRepository.findBooksByAllFields(
                Optional.of(book.getTitle()),
                Optional.of(book.getAuthor()),
                Optional.of(book.getEdition())
        );

        //Check if book already exists in the virtual shelter
        if (bookAlreadyPresent.isPresent()) {
            //Verify that given user did not already added same book in the virtual shelter
            if (getBooksOwnedByUser(userId).contains(bookAlreadyPresent.get())) {
                return HttpResponseUtilities.dataConflict("You already added this book!");
            } else {
                bookOwner.setBook(bookAlreadyPresent.get());
                bookOwnerRepository.saveAndFlush(bookOwner);
            }
        } else {
            bookRepository.saveAndFlush(book);
            bookOwnerRepository.saveAndFlush(bookOwner);
        }

        return HttpResponseUtilities.insertDone("Book added with success!");
    }

    public void deleteAllBooksOwnedByAnUser(final Long userId) {
        List<BookOwner> bookOwners = bookOwnerRepository.findAll();

        List<Book> booksInOwnerTable = bookOwners.stream().map(BookOwner::getBook).collect(Collectors.toList());
        List<Book> booksInBooksTable = bookRepository.findAll();

        for(BookOwner entry : bookOwners){
            Book book = entry.getBook();
            if(entry.getUser().getId().equals(userId)){
                bookOwnerRepository.delete(entry);
                booksInOwnerTable.remove(book);
                if(booksInBooksTable.contains(book) && !booksInOwnerTable.contains(book)){
                    bookRepository.delete(book);
                    booksInBooksTable.remove(book);
                }
            }
        }
    }

    public ResponseEntity<String> deleteBookFromUser(final Optional<Long> bookId, final Optional<Long> userId){
        if(BooleanUtilities.anyEmptyParameters(bookId, userId)){
            return HttpResponseUtilities.wrongParameters();
        }

        Optional<Book> bookOptional = bookRepository.findById(bookId.orElse(0L));
        Optional<User> userOptional = userRepository.findById(userId.orElse(0L));
        if(bookOptional.isEmpty() || userOptional.isEmpty()){
            return HttpResponseUtilities.noContentFound();
        }

        Book book = bookOptional.get();
        User user = userOptional.get();
        Optional<BookOwner> bookOwnerOptional = bookOwnerRepository.findById(new BookOwnerKey(book.getId(), user.getId()));

        if(bookOwnerOptional.isEmpty()){
            return HttpResponseUtilities.noContentFound();
        }

        bookBorrowerService.deleteAllBorrowsOfAnBook(book.getId());

        BookOwner bookOwner = bookOwnerOptional.get();
        bookOwnerRepository.delete(bookOwner);
        List<BookOwner> leftEntries = bookOwnerRepository.getEntriesByBookId(book.getId());

        if(leftEntries.isEmpty()){
            bookRepository.delete(book);
        }

        return HttpResponseUtilities.operationWasDone("User with user id "
                + user.getUsername() + " deleted book " + book.getTitle() + " with success!");

    }
}
