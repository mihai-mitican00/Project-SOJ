package com.endava.tmd.bookclubproject.waitinglist;

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
    @Id
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public WaitingList(final Long bookId, final Long userId){
        this.bookId = bookId;
        this.userId = userId;
    }
}
