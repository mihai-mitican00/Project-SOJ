package com.endava.tmd.bookclubproject.waitinglist;

import com.endava.tmd.bookclubproject.exception.ApiBadRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestMethodOrder(MethodOrderer.DisplayName.class)
@WebMvcTest(WaitingListController.class)
@WithMockUser
class WaitingListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WaitingListService waitingListService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final WaitingList ENTRY_1 = new WaitingList(1L, 2L, 3L);
    private static final WaitingList ENTRY_2 = new WaitingList(3L, 1L, 2L);


    @Test
    @DisplayName("getAllOnWaitingList() returns no content when no entry found")
    void getAllShouldReturnNoContent() throws Exception {
        when(waitingListService.getAllOnWaitingList()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/waiting_list"))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(waitingListService).getAllOnWaitingList();
    }

    @Test
    @DisplayName("getAllOnWaitingList() returns entries when entry found")
    void getAllShouldReturnOk() throws Exception {
        List<WaitingList> entries = List.of(ENTRY_1, ENTRY_2);
        when(waitingListService.getAllOnWaitingList()).thenReturn(entries);

        String expectedEntries = objectMapper.writeValueAsString(entries);
        mockMvc.perform(get("/waiting_list"))
                .andExpectAll(
                        status().isOk(),
                        content().json(expectedEntries, true)
                )
                .andDo(print());

        verify(waitingListService).getAllOnWaitingList();
    }

    @Test
    @DisplayName("addUserOnList(..) throws bad request when add is not possible")
    void addUserOnListShouldThrowBadRequest() throws Exception {
        Long bookId = 1L, ownerId = 1L, userId = 1L;
        doThrow(ApiBadRequestException.class)
                .when(waitingListService).
                addUserOnList(bookId, ownerId, userId);

        mockMvc.perform(post("/waiting_list")
                        .param("bookId", String.valueOf(bookId))
                        .param("ownerId", String.valueOf(ownerId))
                        .param("userId", String.valueOf(userId))
                        .with(csrf())
                )
                .andExpect(status().isBadRequest())
                .andDo(print());

        verify(waitingListService).addUserOnList(bookId, ownerId, userId);
    }

    @Test
    @DisplayName("addUserOnList(..) creates entry on waiting list when add is possible")
    void addUserOnListShouldReturnCreated() throws Exception {
        Long bookId = ENTRY_1.getBookId();
        Long ownerId = ENTRY_1.getOwnerId();
        Long userId = ENTRY_1.getUserId();

        String message =
                String.format(
                        "User with id %d has added himself on waiting list for book with id %d owned by user with id %d",
                        userId,
                        bookId,
                        ownerId);

        mockMvc.perform(post("/waiting_list")
                        .param("bookId", String.valueOf(bookId))
                        .param("ownerId", String.valueOf(ownerId))
                        .param("userId", String.valueOf(userId))
                        .with(csrf())
                )
                .andExpectAll(
                        status().isCreated(),
                        content().string(message)
                )
                .andDo(print());
    }
}