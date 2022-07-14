package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Tag(name = "Renting")
@RestController
@RequestMapping("book_borrowers")
public class BookBorrowerController {

    @Autowired
    private BookBorrowerService bookBorrowerService;

    @RequestMapping(method = RequestMethod.GET)
    @Operation(
            summary = "Get all renting entries.",
            description = "Get all books borrowed and their borrowers, as well as owner id, borrow date and return date.",
            responses = {
                    @ApiResponse(
                            description = "See all the borrows that are currently made.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookBorrower.class))
                    ),
                    @ApiResponse(
                            description = "There are no borrows made yet.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<List<BookBorrower>> getAllBookBorrowers() {
        List<BookBorrower> listOfEntries = bookBorrowerService.getAllBookBorrowers();
        if (BooleanUtilities.emptyList(listOfEntries)) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(listOfEntries);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/BooksUserGave")
    @Operation(
            summary = "Get all books that user gave.",
            description = "See all the books that give user gave to borrowers, and the date when he will get them back.",
            responses = {
                    @ApiResponse(
                            description = "See all the books the user gave.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "This user gave no books yet.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> getBooksThatUserGave(@RequestParam("ownerId") final Long ownerId) {

        List<BookBorrower> borrowerList = bookBorrowerService.getBooksThatUserGave(ownerId);

        if (borrowerList.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }

        StringBuilder message = new StringBuilder();
        borrowerList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringBorrowerFocused())
                        .append("\n------------------------------\n"));

        return HttpResponseUtilities.operationSuccess(message.toString());
    }

    @RequestMapping(method = RequestMethod.GET, value = "/BooksUserRented")
    @Operation(
            summary = "Get all books that user rented.",
            description = "See all the books that give user rented from owners, and the date when he will return them back.",
            responses = {
                    @ApiResponse(
                            description = "See all the books the user rented.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "This user rented no books yet.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> getBooksThatUserRented(@RequestParam("borrowerId") final Long borrowerId) {
        List<BookBorrower> borrowerList = bookBorrowerService.getBooksThatUserRented(borrowerId);
        if (BooleanUtilities.emptyList(borrowerList)) {
            return HttpResponseUtilities.noContentFound();
        }

        StringBuilder message = new StringBuilder();
        borrowerList.forEach(bookBorrower ->
                message.append(bookBorrower.toStringOwnerFocused())
                        .append("\n------------------------------\n"));

        return HttpResponseUtilities.operationSuccess(message.toString());
    }


    @RequestMapping(method = RequestMethod.PUT)
    @Operation(
            summary = "Extend a rent.",
            description = "Extend the return date of a rent, with one week, until the renting period reaches a maximum of 5 weeks.",
            responses = {
                    @ApiResponse(
                            description = "Book return date extended successfully.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Return date extend failed.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> extendRentingPeriod(@RequestParam("bookId") final Long bookId,
                                                      @RequestParam("borrowerId") final Long borrowerId) {
        return bookBorrowerService.extendRentingPeriod(bookId, borrowerId);
    }

    @RequestMapping(method = RequestMethod.POST)
    @Operation(
            summary = "Rent a book.",
            description = "Rent a book from owner, for a certain period 1-4 weeks.",
            responses = {
                    @ApiResponse(
                            description = "Book rented successfully.",
                            responseCode = "201",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Borrow cannot be done from various reasons.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> borrowBookFromOwner(@RequestParam("bookId") final Long bookId,
                                                      @RequestParam("borrowerId") final Long borrowerId,
                                                      @RequestParam("ownerId") final Long ownerId,
                                                      @RequestParam("weeks") final Long weeksToRent
    ) {
            return bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent);
    }

}
