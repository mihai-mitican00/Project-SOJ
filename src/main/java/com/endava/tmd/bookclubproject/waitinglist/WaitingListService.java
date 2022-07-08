package com.endava.tmd.bookclubproject.waitinglist;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.book.BookRepository;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrower;
import com.endava.tmd.bookclubproject.bookborrower.BookBorrowerRepository;
import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.user.User;
import com.endava.tmd.bookclubproject.user.UserRepository;
import static com.endava.tmd.bookclubproject.utilities.BooleanUtilities.*;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
    private BookBorrowerRepository bookBorrowerRepository;

    public List<WaitingList> getAllOnWaitingList() {
        return waitingListRepository.findAll();
    }



    public ResponseEntity<String> addUserOnList(final Long bookId, final Long userId) {
        Optional<Book> bookOptional = bookRepository.findById(bookId);
        Optional<User> userOptional = userRepository.findById(userId);

        if(bookOptional.isEmpty() || userOptional.isEmpty()){
            return HttpResponseUtilities.noContentFound();
        }


        Optional<WaitingList> entryAlreadyPresent = waitingListRepository.getEntryByBookIdAndUserId(bookId, userId);
        if(entryAlreadyPresent.isPresent()){
            return HttpResponseUtilities.dataConflict(
                    "User already with id " + userId +
                    " added himself on waiting list for book with id " + bookId);
        }

        Optional<BookBorrower> borrowDoneByUser = bookBorrowerRepository.findEntryByBookAndBorrower(bookId, userId);
        if(borrowDoneByUser.isPresent()){
            return HttpResponseUtilities.dataConflict(
                    "User already with id " + userId +
                            " already has book with id " + bookId);
        }

        Optional<BookBorrower> bookOwnerOptional = bookBorrowerRepository.findEntryByBookAndOwner(bookId, userId);
        if(bookOwnerOptional.isPresent()){
            return HttpResponseUtilities.dataConflict(
                    "User cannot be added on waiting list for his own book ");
        }

        WaitingList entry = new WaitingList(bookId, userId);
        waitingListRepository.save(entry);
        return HttpResponseUtilities.insertDone("Insert done with success!");
    }

    public void deleteAllEntriesOfAnUser(final Long userId){
        List<WaitingList> entries = waitingListRepository.findAll();
        for(WaitingList entry : entries){
            if(entry.getUserId().equals(userId)){
                waitingListRepository.delete(entry);
            }
        }
    }
}
