package com.endava.tmd.bookclubproject.waitinglist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@SuppressWarnings("JpaDataSourceORMInspection")
@Data
@NoArgsConstructor

@Entity
@Table(name = "waiting_list")
public class WaitingList {

    @SequenceGenerator(
            name = "waiting_list_sequence",
            sequenceName = "waiting_list_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "waiting_list_sequence"
    )
    @Schema(description = "Auto-generated id", example = "2")
    @Id
    private Long id;

    @Schema(description = "Book id", example = "2")
    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Schema(description = "User id", example = "3")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    public WaitingList(final Long bookId, final Long userId){
        this.bookId = bookId;
        this.userId = userId;
    }
}
