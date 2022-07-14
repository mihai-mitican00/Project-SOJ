package com.endava.tmd.bookclubproject.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Long> {
    Optional<User> findUserByUsernameOrEmail(final Optional<String> username, final Optional<String> email);
}
