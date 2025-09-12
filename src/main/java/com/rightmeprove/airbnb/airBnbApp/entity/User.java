package com.rightmeprove.airbnb.airBnbApp.entity;

import com.rightmeprove.airbnb.airBnbApp.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "app_user")
// Renamed because "user" is a reserved keyword in Postgres
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Primary key, auto-incremented
    private Long id;

    @Column(unique = true, nullable = false)
    // Unique + NOT NULL → ensures no two users share same email
    // Postgres automatically creates an index for unique columns
    private String email;

    @Column(nullable = false)
    private String password; // Hashed password (never store plain text)

    private String name; // User's display name

    @ElementCollection(fetch = FetchType.EAGER)
    /*
     * - Stores a collection of simple types (like enums, Strings, numbers) in a separate table.
     * - Here, roles are stored in a join table automatically created by JPA.
     *   Table name → user_roles (by default)
     *   Columns → user_id (FK), roles (enum string)
     *
     * FetchType.EAGER → roles are always loaded with the User (since roles are small and essential).
     */
    @Enumerated(EnumType.STRING)
    /*
     * Enum stored as String (e.g., "ADMIN", "CUSTOMER") instead of ordinal numbers.
     * Using STRING is safer because ORDINAL would store 0, 1, 2...
     * → which breaks if enum order changes in code.
     */
    private Set<Role> roles;
}
