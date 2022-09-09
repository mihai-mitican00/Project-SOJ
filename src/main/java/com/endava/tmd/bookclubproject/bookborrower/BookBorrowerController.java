package com.endava.tmd.bookclubproject.bookborrower;

import com.endava.tmd.bookclubproject.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Renting")
@RestController
@RequestMapping("book_borrowers")
public class BookBorrowerController {

    @Autowired
    private BookBorrowerService bookBorrowerService;

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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BookBorrower>> getAllBookBorrowers() {
        List<BookBorrower> listOfEntries = bookBorrowerService.getAllBookBorrowers();
        if (listOfEntries.isEmpty()) {
            return noContent().build();
        }
        return ok(listOfEntries);
    }


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
    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/BooksUserGave")
    public ResponseEntity<String> getBooksThatUserGave(@RequestParam("ownerId") final Long ownerId) {
        List<BookBorrower> borrowsList = bookBorrowerService.getBorrowsWhereUserGave(ownerId);
        if (borrowsList.isEmpty()) {
            return noContent().build();
        }
        String message = bookBorrowerService.formatBooksThatUserGave(borrowsList);
        return ok(message);
    }


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
    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, value = "/BooksUserRented")
    public ResponseEntity<String> getBooksThatUserRented(@RequestParam("borrowerId") final Long borrowerId) {
        List<BookBorrower> borrowersList = bookBorrowerService.getBorrowsWhereUserReceived(borrowerId);
        if (borrowersList.isEmpty()) {
            return noContent().build();
        }
        String message = bookBorrowerService.formatBooksThatUserReceived(borrowersList);
        return ok(message);
    }

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
    @PreAuthorize("hasAuthority('book:rent')")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> borrowBookFromOwner(@AuthenticationPrincipal final User authenticatedUser,
                                                      @RequestParam("bookId") final Long bookId,
                                                      @RequestParam("ownerId") final Long ownerId,
                                                      @RequestParam("weeks") final Long weeksToRent
    ) {
        Long borrowerId = authenticatedUser.getId();
        String message = bookBorrowerService.borrowBookFromOwner(bookId, borrowerId, ownerId, weeksToRent);

        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }


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
    @PreAuthorize("hasAuthority('book:rent')")
    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<String> extendRentingPeriod(@AuthenticationPrincipal final User authenticatedUser,
                                                      @RequestParam("bookId") final Long bookId) {
        Long borrowerId = authenticatedUser.getId();
        String message = bookBorrowerService.extendRentingPeriod(bookId, borrowerId);
        return ok(message);
    }

}
