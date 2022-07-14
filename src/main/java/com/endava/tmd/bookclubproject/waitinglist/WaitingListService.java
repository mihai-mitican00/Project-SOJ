package com.endava.tmd.bookclubproject.waitinglist;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.bookowner.BookOwnerRepository;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Optional;

@Service
public class WaitingListService {

    @Autowired
    private WaitingListRepository waitingListRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookOwnerRepository bookOwnerRepository;

    @Autowired
    private BookBorrowerRepository bookBorrowerRepository;

    public List<WaitingList> getAllOnWaitingList() {
        return waitingListRepository.findAll();
    }

    public ResponseEntity<String> addUserOnList(final Long bookId, final Long userId) {
        if(objectsAreInvalid(bookId, userId)){
            return HttpResponseUtilities.noContentFound();
        }

        if(entryAlreadyPresent(bookId, userId)){
            return HttpResponseUtilities.dataConflict("User with id " + userId +
                    " already added himself on waiting list for book with id " + bookId);
        }

        if(userOwnsTheBook(bookId, userId)){
            return HttpResponseUtilities.dataConflict(
                    "User cannot be added on waiting list for his own book.");
        }

        if(bookAlreadyBorrowedByThisUser(bookId, userId)){
            return HttpResponseUtilities.dataConflict(
                    "User having id " + userId +
                            " is already renting book with id " + bookId);
        }

        if(!bookAlreadyBorrowedByThisUser(bookId, userId)){
            return HttpResponseUtilities.notAcceptable("You cannot add yourself on the waiting list for a book that is not already rented!");
        }


        WaitingList entry = new WaitingList(bookId, userId);
        waitingListRepository.save(entry);
        return HttpResponseUtilities.insertSuccess("User with id " + entry.getUserId()
                + " has added himself on waiting list for book with id " + entry.getBookId());
    }



    private boolean objectsAreInvalid(final Long bookId, final Long userId){
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        Optional<User> userOptional = userRepository.findById(userId);

        return bookOptional.isEmpty() || userOptional.isEmpty();
    }

    private boolean entryAlreadyPresent(final Long bookId, final Long userId){
        Optional<WaitingList> optionalWaitingList = waitingListRepository.findByBookIdAndUserId(bookId, userId);
        return optionalWaitingList.isPresent();
    }

    private boolean bookAlreadyBorrowedByThisUser(final Long bookId, final Long userId) {
        Optional<BookBorrower> borrowDoneByUser = bookBorrowerRepository.findByBookIdAndBorrowerId(bookId, userId);
        return borrowDoneByUser.isPresent();
    }

    private boolean userOwnsTheBook(final Long bookId, final Long userId){
        Optional<BookOwner> bookOwner = bookOwnerRepository.findByBookIdAndUserId(bookId, userId);
        return bookOwner.isPresent();
    }
}
