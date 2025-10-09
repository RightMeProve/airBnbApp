package com.rightmeprove.airbnb.airBnbApp.service;

import com.rightmeprove.airbnb.airBnbApp.dto.ProfileUpdateRequestDto;
import com.rightmeprove.airbnb.airBnbApp.dto.UserDto;
import com.rightmeprove.airbnb.airBnbApp.entity.User;
import com.rightmeprove.airbnb.airBnbApp.exception.ResourceNotFoundException;
import com.rightmeprove.airbnb.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.rightmeprove.airbnb.airBnbApp.util.AppUtils.getCurrentUser;

/**
 * Service implementation for managing users.
 *
 * Responsibilities:
 * 1. Fetch user by ID.
 * 2. Update the current authenticated user's profile.
 * 3. Return the current user's profile as DTO.
 * 4. Integrate with Spring Security for authentication (UserDetailsService).
 */
@Service
@RequiredArgsConstructor // generates constructor for all final dependencies
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository; // repository to manage User entities
    private final ModelMapper modelMapper;       // ModelMapper for entity <-> DTO conversion

    /**
     * Fetch a user by their ID.
     * Throws ResourceNotFoundException if user does not exist.
     *
     * @param id user ID
     * @return User entity
     */
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with userID: " + id));
    }

    /**
     * Update the current authenticated user's profile.
     * Only non-null fields in the DTO are updated.
     *
     * @param profileUpdateRequestDto DTO containing profile updates
     */
    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user = getCurrentUser(); // fetch the currently authenticated user

        // Update fields only if provided in the request DTO
        if (profileUpdateRequestDto.getDateOfBirth() != null)
            user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
        if (profileUpdateRequestDto.getName() != null)
            user.setName(profileUpdateRequestDto.getName());
        if (profileUpdateRequestDto.getGender() != null)
            user.setGender(profileUpdateRequestDto.getGender());

        // Persist changes
        userRepository.save(user);
    }

    /**
     * Fetch the profile of the current authenticated user as a DTO.
     *
     * @return UserDto containing the user's profile
     */
    @Override
    public UserDto getMyProfile() {
        User user = getCurrentUser();
        log.info("Getting the profile for user with id: {}", user.getId());

        return modelMapper.map(user, UserDto.class); // map entity → DTO
    }

    /**
     * Spring Security integration: load user by username (email).
     * Returns UserDetails for authentication.
     *
     * @param username user email
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
    }
}
