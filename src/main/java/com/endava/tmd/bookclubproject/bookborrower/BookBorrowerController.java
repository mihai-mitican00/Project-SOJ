package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor

@RestController
@RequestMapping("book_borrowers")
public class BookBorrowerController {

    @Autowired
    private BookBorrowerService bookBorrowerService;

    @RequestMapping(method = RequestMethod.GET)
    public Object getAllBookBorrowers() {
        List<BookBorrower> listOfEntries = bookBorrowerService.getAllBookBorrowers();
        if (listOfEntries.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return listOfEntries;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/BooksUserGave")
    public ResponseEntity<String> getBooksThatUserGave(@RequestParam("userId") final Optional<Long> userId) {
        if (userId.isEmpty()) {
            return HttpResponseUtilities.wrongParameters();
        }
        List<BookBorrower> borrowerList = bookBorrowerService.getBooksThatUserGave(userId.get());

        if (borrowerList.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        StringBuilder message = new StringBuilder();
        borrowerList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringBorrowerFocused())
                        .append("\n------------------------------\n"));

        return HttpResponseUtilities.operationWasDone(message.toString());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/BooksUserRented")
    public ResponseEntity<String> getBooksThatUserRented(@RequestParam("borrowerId") final Optional<Long> borrowerId) {

        if (borrowerId.isEmpty()) {
            return HttpResponseUtilities.wrongParameters();
        }
        List<BookBorrower> borrowerList = bookBorrowerService.getBooksThatUserRented(borrowerId.get());
        if (BooleanUtilities.emptyList(borrowerList)) {
            return HttpResponseUtilities.noContentFound();
        }

        StringBuilder message = new StringBuilder();
        borrowerList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringOwnerFocused())
                        .append("\n------------------------------\n"));

        return HttpResponseUtilities.operationWasDone(message.toString());
    }


    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<String> extendRentingPeriod(@RequestParam("bookId") final Optional<Long> bookId,
                                                      @RequestParam("borrowerId") final Optional<Long> borrowerId) {
        if (BooleanUtilities.anyEmptyParameters(bookId, borrowerId)) {
            return HttpResponseUtilities.wrongParameters();
        }

        return bookBorrowerService.extendRentingPeriod(bookId.orElse(0L), borrowerId.orElse(0L));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> borrowBookFromOwner(@RequestParam("bookId") final Optional<Long> bookId,
                                                      @RequestParam("borrowerId") final Optional<Long> borrowerId,
                                                      @RequestParam("ownerId") final Optional<Long> ownerId,
                                                      @RequestParam("weeks") final Optional<Long> weeksToRent
    ) {
        if (BooleanUtilities.anyEmptyParameters(bookId, borrowerId, ownerId, weeksToRent)
                || (weeksToRent.orElse(0L) < 1 || weeksToRent.orElse(5L) > 4)) {
            return HttpResponseUtilities.wrongParameters();
        }

        return bookBorrowerService
                .borrowBookFromOwner(
                        bookId.orElse(0L),
                        borrowerId.orElse(0L),
                        ownerId.orElse(0L),
                        weeksToRent.get()
                );
    }

}
