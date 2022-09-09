package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.book.Book;
import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.endava.tmd.bookclubproject.exception.ApiForbiddenException;
import com.endava.tmd.bookclubproject.exception.ApiNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.endava.tmd.bookclubproject.security.UserRoles.ADMIN;
import static com.endava.tmd.bookclubproject.security.UserRoles.USER;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "User")
@RestController
@RequestMapping(path = "users")
public class UserController {

    @Autowired
    private UserService userService;


    @Operation(
            summary = "Get all users.",
            description = "Getting all users registered within application.",
            responses = {
                    @ApiResponse(
                            description = "See all the users with success.",
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
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> usersList = userService.getAllUsers();
        if (usersList.isEmpty()) {
            return noContent().build();
        }
        return ok(usersList);
    }


    @Operation(
            summary = "Get user.",
            description = "Getting user by id.",
            responses = {
                    @ApiResponse(
                            description = "See user with success.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))
                    ),
                    @ApiResponse(
                            description = "User not found.",
                            responseCode = "404",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Normal user cannot see other users.",
                            responseCode = "403",
                            content = @Content
                    )

            }
    )
    @PreAuthorize("hasAuthority('user:read')")
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    public User getUserById(@PathVariable("id") final Long userId, @AuthenticationPrincipal User authenticatedUser) {

        User userFound = userService.getUserById(userId)
                .orElseThrow(() -> new ApiNotFoundException(String.format("There is no user with id %d.", userId)));

        if (authenticatedUser.getRole().equals(USER) && !authenticatedUser.getId().equals(userId)) {
            throw new ApiForbiddenException("Normal user cannot see other users!");
        }
        return userFound;
    }


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
                    ),
                    @ApiResponse(
                            description = "Normal user cannot see other user's books.",
                            responseCode = "403",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "No user found with given id.",
                            responseCode = "404",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasAuthority('book:read')")
    @RequestMapping(method = RequestMethod.GET, path = "/BooksOwned/{id}")
    public ResponseEntity<List<Book>> getBooksOfUser(@PathVariable("id") final Long userId,
                                                     @AuthenticationPrincipal User authenticatedUser) {

        Optional<User> userOptional = userService.getUserById(userId);
        if (userOptional.isEmpty()) {
            throw new ApiNotFoundException(String.format("There is no user with id %d.", userId));
        }

        if (authenticatedUser.getRole().equals(USER) && !authenticatedUser.getId().equals(userId)) {
            throw new ApiForbiddenException("Normal user cannot see other user's books!");
        }

        List<Book> booksOwned = userService.getBooksOfUser(userId);
        if (booksOwned.isEmpty()) {
            return noContent().build();
        }
        return ok(booksOwned);
    }

//    @Operation(
//            summary = "Register new user.",
//            description = "Enter data for a new account and create it, if username and email not already used.",
//            responses = {
//                    @ApiResponse(
//                            description = "User account created with success.",
//                            responseCode = "200",
//                            content = @Content
//                    ),
//                    @ApiResponse(
//                            description = "Username or email already used.",
//                            responseCode = "400",
//                            content = @Content
//                    )
//            }
//    )
//    @PreAuthorize("hasAnyRole('user:write')")
//    @RequestMapping(method = RequestMethod.POST)
//    public ResponseEntity<String> registerUser(@RequestBody final User user) {
//
//        userService.registerUser(Optional.ofNullable(user));
//        return ok("User account created with success!");
//    }

    @Operation(
            summary = "Delete User.",
            description = "Delete the user with given id and all of his traces, including borrows he made, books he owns etc.",
            responses = {
                    @ApiResponse(
                            description = "User account and every action he made deleted with success.",
                            responseCode = "200",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "User with given id does not exist.",
                            responseCode = "404",
                            content = @Content
                    )
            }
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(method = RequestMethod.DELETE, path = "/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") final Long userId) {
        String deleteMessage = userService.deleteUser(userId);
        return ok(deleteMessage);
    }
}
