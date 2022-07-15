package com.endava.tmd.bookclubproject.bookowner;

import com.endava.tmd.bookclubproject.user.User;
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
public class BookOwnerId implements Serializable {

    @Schema(description = "Id of the book held by owner.", example = "5")
    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Schema(description = "Id of the user that owns the book.", example = "5")
    @Column(name = "user_id", nullable = false)
    private Long userId;

}
