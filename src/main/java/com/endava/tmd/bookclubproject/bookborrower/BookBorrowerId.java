package com.endava.tmd.bookclubproject.bookborrower;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor
@AllArgsConstructor

@Embeddable
public class BookBorrowerId implements Serializable {

    @Schema(description = "Id of the book that was borrowed.", example = "5")
    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Schema(description = "Id of the user that borrowed the book.", example = "5")
    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;
}
