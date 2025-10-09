package com.rightmeprove.airbnb.airBnbApp.util;

import com.rightmeprove.airbnb.airBnbApp.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * ⚡ AppUtils
 *
 * Utility class for application-wide helper methods.
 * Currently provides methods related to security and authentication.
 */
public class AppUtils {

    /**
     * Get the currently authenticated user from the Spring Security context.
     *
     * Usage:
     * - Instead of repeatedly fetching the user from SecurityContextHolder in services,
     *   call AppUtils.getCurrentUser() for cleaner code.
     *
     * @return currently authenticated User entity
     * @throws ClassCastException if the principal is not of type User
     */
    public static User getCurrentUser() {
        // SecurityContextHolder stores authentication info for the current thread
        // getPrincipal() returns the logged-in user details
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
