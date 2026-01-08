package com.hafizbahtiar.spring.features.user.service;

import com.hafizbahtiar.spring.common.service.EmailService;
import com.hafizbahtiar.spring.features.user.exception.RoleException;
import com.hafizbahtiar.spring.features.user.exception.UserAlreadyExistsException;
import com.hafizbahtiar.spring.features.user.exception.UserNotFoundException;
import com.hafizbahtiar.spring.features.user.dto.UpdateProfileRequest;
import com.hafizbahtiar.spring.features.user.dto.UserProfileResponse;
import com.hafizbahtiar.spring.features.user.dto.UserRegistrationRequest;
import com.hafizbahtiar.spring.features.user.dto.UserResponse;
import com.hafizbahtiar.spring.features.user.dto.UserUpdateRequest;
import com.hafizbahtiar.spring.features.user.entity.CurrencyPreferences;
import com.hafizbahtiar.spring.features.user.entity.NotificationPreferences;
import com.hafizbahtiar.spring.features.user.entity.User;
import com.hafizbahtiar.spring.features.user.entity.UserPreferences;
import com.hafizbahtiar.spring.features.user.mapper.UserMapper;
import com.hafizbahtiar.spring.features.user.repository.CurrencyPreferencesRepository;
import com.hafizbahtiar.spring.features.user.repository.NotificationPreferencesRepository;
import com.hafizbahtiar.spring.features.user.repository.UserPreferencesRepository;
import com.hafizbahtiar.spring.features.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserActivityLoggingService userActivityLoggingService;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final UserPreferencesRepository userPreferencesRepository;
    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final CurrencyPreferencesRepository currencyPreferencesRepository;

    @Value("${app.file-storage.upload-dir:uploads/avatars}")
    private String uploadDir;

    @Value("${app.file-storage.base-url:http://localhost:8080/api/v1/files/avatars}")
    private String baseUrl;

    @Value("${app.file-storage.max-file-size:5242880}") // 5MB default
    private long maxFileSize;

    @Override
    public UserResponse register(UserRegistrationRequest request) {
        log.debug("Registering new user with email: {}", request.getEmail());

        try {
            // Check if email already exists
            if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
                throw UserAlreadyExistsException.email(request.getEmail());
            }

            // Check if username already exists
            if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
                throw UserAlreadyExistsException.username(request.getUsername());
            }

            // Map request to entity
            User user = userMapper.toEntity(request);

            // Hash password
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

            // Save user and flush to ensure it's persisted before email verification
            User savedUser = userRepository.saveAndFlush(user);
            log.info("User registered successfully with ID: {}", savedUser.getId());

            // Generate email verification token and send verification email (do this BEFORE
            // creating preferences)
            // This ensures that if preferences creation fails, it won't affect email
            // verification
            try {
                String verificationToken = emailVerificationService.generateVerificationToken(savedUser);
                log.info("Email verification token generated for user: {}", savedUser.getEmail());

                // Send verification email (asynchronous, non-blocking)
                try {
                    emailService.sendEmailVerificationEmail(
                            savedUser.getEmail(),
                            verificationToken,
                            savedUser.getFullName());
                    log.info("Verification email sent to: {}", savedUser.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send verification email to: {}", savedUser.getEmail(), e);
                    // Don't fail registration if email fails - token is already saved
                    // User can request a new verification email later via resend endpoint
                }
            } catch (Exception e) {
                log.error("Failed to generate email verification token for user: {}", savedUser.getEmail(), e);
                // Don't fail registration if token generation fails - user can request
                // verification later
            }

            // Auto-create default user preferences (after email verification to avoid flush
            // issues)
            try {
                UserPreferences defaultPreferences = new UserPreferences(savedUser);
                userPreferencesRepository.save(defaultPreferences);
                log.info("Default user preferences created for user ID: {}", savedUser.getId());
            } catch (Exception e) {
                log.error("Failed to create default user preferences for user ID: {}", savedUser.getId(), e);
                // Don't fail registration if preferences creation fails - preferences can be
                // created on first access
            }

            // Auto-create default notification preferences
            try {
                NotificationPreferences defaultNotificationPreferences = new NotificationPreferences(savedUser);
                notificationPreferencesRepository.save(defaultNotificationPreferences);
                log.info("Default notification preferences created for user ID: {}", savedUser.getId());
            } catch (Exception e) {
                log.error("Failed to create default notification preferences for user ID: {}", savedUser.getId(), e);
                // Don't fail registration if preferences creation fails - preferences can be
                // created on first access
            }

            // Auto-create default currency preferences
            try {
                CurrencyPreferences defaultCurrencyPreferences = new CurrencyPreferences(savedUser);
                currencyPreferencesRepository.save(defaultCurrencyPreferences);
                log.info("Default currency preferences created for user ID: {}", savedUser.getId());
            } catch (Exception e) {
                log.error("Failed to create default currency preferences for user ID: {}", savedUser.getId(), e);
                // Don't fail registration if preferences creation fails - preferences can be
                // created on first access
            }

            // Log user registration activity (non-blocking, don't fail if logging fails)
            try {
                HttpServletRequest httpRequest = getCurrentRequest();
                if (httpRequest != null) {
                    userActivityLoggingService.logRegistration(
                            savedUser.getId(),
                            savedUser.getEmail(),
                            httpRequest);
                }
            } catch (Exception e) {
                log.error("Failed to log user registration activity for user ID: {}", savedUser.getId(), e);
                // Don't fail registration if logging fails
            }

            // Map to response
            UserResponse response = userMapper.toResponse(savedUser);
            log.debug("User registration completed successfully for email: {}", request.getEmail());
            return response;
        } catch (UserAlreadyExistsException e) {
            // Re-throw user already exists exceptions
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during user registration for email: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        log.debug("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfileById(Long id) {
        log.debug("Fetching user profile with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));
        return userMapper.toProfileResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all active users");
        List<User> users = userRepository.findByActiveTrue();
        return userMapper.toResponseList(users);
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.debug("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));

        // Check if email is being changed and if it already exists
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw UserAlreadyExistsException.email(request.getEmail());
            }
        }

        // Check if username is being changed and if it already exists
        if (request.getUsername() != null && !request.getUsername().equalsIgnoreCase(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(request.getUsername(), id)) {
                throw UserAlreadyExistsException.username(request.getUsername());
            }
        }

        // Note: Role updates are not handled via UserUpdateRequest
        // Role changes should be done via a separate admin endpoint with proper
        // validation

        // Update user entity
        userMapper.updateEntityFromRequest(request, user);

        // Save updated user
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        // Log profile update activity
        userActivityLoggingService.logProfileUpdate(
                updatedUser.getId(),
                getCurrentRequest(),
                null // Response time can be tracked at controller level
        );

        return userMapper.toResponse(updatedUser);
    }

    /**
     * Update user role with OWNER uniqueness validation.
     * This method should be called by admin endpoints when updating roles.
     *
     * @param userId  User ID to update
     * @param newRole New role to assign
     * @return Updated UserResponse
     */
    @Override
    public UserResponse updateUserRole(Long userId, String newRole) {
        log.debug("Updating role for user ID: {} to role: {}", userId, newRole);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        String currentRole = user.getRole();
        String upperNewRole = newRole != null ? newRole.toUpperCase() : null;

        // Validate role
        if (upperNewRole == null
                || (!upperNewRole.equals("USER") && !upperNewRole.equals("OWNER") && !upperNewRole.equals("ADMIN"))) {
            throw RoleException.invalidRole(newRole);
        }

        // If assigning OWNER role, check if one already exists
        if ("OWNER".equals(upperNewRole) && !"OWNER".equalsIgnoreCase(currentRole)) {
            if (userRepository.existsOwner()) {
                throw RoleException.ownerAlreadyExists();
            }
        }

        // If removing OWNER role, ensure at least one owner remains
        if ("OWNER".equalsIgnoreCase(currentRole) && !"OWNER".equals(upperNewRole)) {
            // Check if this is the only owner
            Optional<User> existingOwner = userRepository.findOwner();
            if (existingOwner.isPresent() && existingOwner.get().getId().equals(userId)) {
                throw RoleException.cannotRemoveOwner();
            }
        }

        // Update role
        user.setRole(upperNewRole);
        User updatedUser = userRepository.save(user);
        log.info("User role updated successfully. User ID: {}, New role: {}", updatedUser.getId(), upperNewRole);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.debug("Deleting (deactivating) user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));

        user.deactivate();
        userRepository.save(user);
        log.info("User deactivated successfully with ID: {}", id);

        // Log user deactivation activity
        userActivityLoggingService.logDeactivation(
                id,
                getCurrentRequest());
    }

    @Override
    public UserResponse verifyEmail(Long id) {
        log.debug("Verifying email for user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> UserNotFoundException.byId(id));

        user.verifyEmail();
        User updatedUser = userRepository.save(user);
        log.info("Email verified successfully for user with ID: {}", id);

        // Log email verification activity
        userActivityLoggingService.logEmailVerification(
                id,
                getCurrentRequest());

        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        log.info("Profile update request received for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        // Update profile fields using mapper
        userMapper.updateProfileFromRequest(request, user);

        User updatedUser = userRepository.save(user);
        log.info("Profile updated successfully for user ID: {}", userId);

        userActivityLoggingService.logProfileUpdate(
                updatedUser.getId(),
                getCurrentRequest(),
                null);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public UserResponse uploadAvatar(Long userId, String avatarUrl, MultipartFile file) {
        log.info("Avatar upload request received for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.byId(userId));

        String finalAvatarUrl = null;

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            // Validate file
            validateAvatarFile(file);

            // Store file and get URL
            finalAvatarUrl = storeAvatarFile(userId, file);
            log.info("Avatar file stored successfully for user ID: {}, URL: {}", userId, finalAvatarUrl);
        } else if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            // Use provided URL
            finalAvatarUrl = avatarUrl.trim();
            log.info("Avatar URL provided for user ID: {}, URL: {}", userId, finalAvatarUrl);
        } else {
            throw new IllegalArgumentException("Either avatarUrl or file must be provided");
        }

        // Update user's avatar URL
        user.setAvatarUrl(finalAvatarUrl);
        User updatedUser = userRepository.save(user);
        log.info("Avatar updated successfully for user ID: {}", userId);

        userActivityLoggingService.logProfileUpdate(
                updatedUser.getId(),
                getCurrentRequest(),
                null);

        return userMapper.toResponse(updatedUser);
    }

    /**
     * Validate avatar file (type and size)
     */
    private void validateAvatarFile(MultipartFile file) {
        // Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(
                    String.format("File size exceeds maximum allowed size of %d bytes", maxFileSize));
        }

        // Check file type (images only)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Check specific image types
        String[] allowedTypes = { "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp" };
        boolean isAllowed = false;
        for (String allowedType : allowedTypes) {
            if (contentType.equalsIgnoreCase(allowedType)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException(
                    "File type not allowed. Allowed types: JPEG, PNG, GIF, WebP");
        }
    }

    /**
     * Store avatar file and return the URL
     */
    private String storeAvatarFile(Long userId, MultipartFile file) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = userId + "_" + UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL
            return baseUrl + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store avatar file for user ID: {}", userId, e);
            throw new RuntimeException("Failed to store avatar file", e);
        }
    }

    /**
     * Get current HTTP request from RequestContextHolder
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
