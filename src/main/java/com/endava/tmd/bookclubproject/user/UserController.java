package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.utilities.BooleanUtilities;
import com.endava.tmd.bookclubproject.utilities.HttpResponseUtilities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "User")
@RequestMapping(path = "users")
public class UserController {

    private static final User body = new User("adi","adi", "adi", "adi", "adi");

    @Autowired
    private UserService userService;


    @RequestMapping(method = RequestMethod.GET)
    @Operation(
            summary = "Get all users.",
            description = "Getting all users registered within application.",
            responses = {
                    @ApiResponse(
                            description = "Get users with success.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(
                            description = "No users are registered.",
                            responseCode = "204",
                            content = @Content
                    )

            }
    )
    public ResponseEntity<List<User>> getUsers() {
        List<User> usersList = userService.getUsers();
        if (BooleanUtilities.emptyList(usersList)) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(usersList);
    }

    @RequestMapping(method = RequestMethod.GET, value = "BooksOwned")
    @Operation(
            summary = "Get books owned.",
            description = "Get all books owned by a certain user.",
            responses = {
                    @ApiResponse(
                            description = "Get books owned by user.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Book.class))
                    ),
                    @ApiResponse(
                            description = "User owns no book.",
                            responseCode = "204",
                            content = @Content
                    )

            }
    )
    public ResponseEntity<List<Book>> getBooksOwned(@RequestParam("userId") Long userId) {
        List<Book> booksOwned = userService.getBooksOwned(userId);
        if (booksOwned.isEmpty()) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(booksOwned);
    }

    @RequestMapping(method = RequestMethod.POST)
    @Operation(
            summary = "Register new user.",
            description = "Enter data for a new account and create it, if username and email not already used.",
            responses = {
                    @ApiResponse(
                            description = "User account created with success.",
                            responseCode = "201",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Details for account creation are not complete.",
                            responseCode = "406",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Username or email already used.",
                            responseCode = "400",
                            content = @Content
                    )

            }
    )
    public ResponseEntity<String> registerUser(@RequestBody final Optional<User> userOptional) {
        return userService.registerUser(userOptional);
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @Operation(
            summary = "Delete User.",
            description = "Delete the user with given id and all of his traces, including borrows he made, books he owns etc.",
            responses = {
                    @ApiResponse(
                            description = "User account created with success.",
                            responseCode = "201",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Details for account creation are not complete.",
                            responseCode = "406",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Username or email already used.",
                            responseCode = "400",
                            content = @Content
                    )

            }
    )
    public ResponseEntity<String> deleteUser(@RequestParam("userId") final Optional<Long> userId) {
        return userService.deleteUser(userId);
    }

}
