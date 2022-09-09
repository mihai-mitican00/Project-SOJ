package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.security.UserRoles;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.endava.tmd.bookclubproject.security.UserRoles.ADMIN;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Owners")
@RestController
@RequestMapping("book_owners")
public class BookOwnerController {

    @Autowired
    private BookOwnerService bookOwnerService;


    @Operation(
            summary = "Get all Book Owner entries.",
            description = "Get all books and their owners, a book with more copies has more owners.",
            responses = {
                    @ApiResponse(
                            description = "See all the books that were added and their owners.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookOwner.class))
                    ),
                    @ApiResponse(
                            description = "There are no books added yet in the virtual shelter by anyone.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BookOwner>> getAllBookOwners() {
        List<BookOwner> bookOwners = bookOwnerService.getAllBookOwners();
        if (bookOwners.isEmpty()) {
            return noContent().build();
        }
        return ok(bookOwners);
    }


    @Operation(
            summary = "Add book as user.",
            description = "Add a book owner entry as a user, the book will appear in Book API too if another user did not already added it.",
            responses = {
                    @ApiResponse(
                            description = "Add a book with success.",
                            responseCode = "201",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Book cannot be added in the virtual shelter due to various problems.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('book:write')")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<String> addBookAsUser(@AuthenticationPrincipal final User authenticatedUser,
                                                @RequestBody(required = false) final Book book) {

        bookOwnerService.addBookAsUser(authenticatedUser, book);
        String message =
                String.format("Thank you %s for adding \"%s\" by %s",
                        authenticatedUser.getUsername(),
                        book.getTitle(),
                        book.getAuthor());
        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Remove book from owner.",
            description = "Owner removes a book, and all traces of that book " +
                    "including waiting list entries, borrows, " +
                    "even the book itself if it is the only copy.",
            responses = {
                    @ApiResponse(
                            description = "Owner removed book successfully.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Book could not be removed from various reasons.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('book:delete')")
    @RequestMapping(method = RequestMethod.DELETE)
    public ResponseEntity<String> removeBookAsUser(@AuthenticationPrincipal final User authenticatedUser,
                                                   @RequestParam("bookId") final Long bookId) {
        Long userId = authenticatedUser.getId();
        bookOwnerService.deleteBookAsUser(bookId, userId);

        String message = String.format("You deleted book with id %d", bookId);
        return ok(message);
    }

}