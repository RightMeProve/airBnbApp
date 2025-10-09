package com.rightmeprove.airbnb.airBnbApp.repository;

import com.rightmeprove.airbnb.airBnbApp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 * Provides CRUD operations via JpaRepository and custom query to fetch user by email.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email.
     * - Returns Optional<User> to handle cases when no user is found.
     *
     * @param email the email of the user
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);
}
