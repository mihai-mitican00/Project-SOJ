package com.endava.tmd.bookclubproject.waitinglist;

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
@Tag(name = "Waiting List")
@RestController
@RequestMapping("waiting_list")
public class WaitingListController {

    @Autowired
    private WaitingListService waitingListService;

    @RequestMapping(method = RequestMethod.GET)
    @Operation(
            summary = "Get all on waiting list.",
            description = "Get all entries on the waiting list.",
            responses = {
                    @ApiResponse(
                            description = "See all entries on waiting list.",
                            responseCode = "200",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = WaitingList.class))
                    ),
                    @ApiResponse(
                            description = "There are no entries on waiting list yet.",
                            responseCode = "204",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<List<WaitingList>> getAllOnWaitingList() {
        List<WaitingList> listOfEntries = waitingListService.getAllOnWaitingList();
        if (BooleanUtilities.emptyList(listOfEntries)) {
            return HttpResponseUtilities.noContentFound();
        }
        return HttpResponseUtilities.operationSuccess(listOfEntries);
    }

    @RequestMapping(method = RequestMethod.POST)
    @Operation(
            summary = "Post user on waiting list.",
            description = "Post user with given id on waiting list for book with given id.",
            responses = {
                    @ApiResponse(
                            description = "Entry created with success",
                            responseCode = "201",
                            content = @Content
                    ),
                    @ApiResponse(
                            description = "Entry for the given userId and bookId is not possible.",
                            responseCode = "400",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<String> addUserOnList(@RequestParam("bookId") final Long bookId,
                                                @RequestParam("userId") final Long userId) {

        return waitingListService.addUserOnList(bookId, userId);
    }


}
