package com.endava.tmd.bookclubproject.user;

import com.endava.tmd.bookclubproject.bookowner.BookOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findUserByEmail(final String email);


}
