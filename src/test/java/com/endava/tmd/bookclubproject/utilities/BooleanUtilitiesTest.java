package com.endava.tmd.bookclubproject.utilities;

import com.endava.tmd.bookclubproject.book.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


class BooleanUtilitiesTest {


    @Test
    @DisplayName("anyNullElements(..) should return true if any or all null")
    void anyNullElementsReturnTrueIfNullFound() {
        Book book = mock(Book.class);
        Book [] testData = {book, null};

        assertThat(BooleanUtilities.anyNullElements(testData)).isTrue();
    }

    @Test
    @DisplayName("anyNullElements(..) should return false if no nulls")
    void anyNullElementsReturnFalseIfNullNotFound() {
        Book book = mock(Book.class);
        Book [] testData = {book};

        assertThat(BooleanUtilities.anyNullElements(testData)).isFalse();
    }

    @Test
    @DisplayName("anyEmptyStringElements(..) should return true if empty or blank strings found")
    void anyEmptyStringsShouldReturnTrue(){
        String [] testData = {"test", "    "};

        assertThat(BooleanUtilities.anyEmptyStringElements(testData)).isTrue();
    }
    @Test
    @DisplayName("anyEmptyStringElements(..) should return false if no empty or blank strings found")
    void anyEmptyStringsShouldReturnFalse(){
        String [] testData = {"test"};

        assertThat(BooleanUtilities.anyEmptyStringElements(testData)).isFalse();
    }
}