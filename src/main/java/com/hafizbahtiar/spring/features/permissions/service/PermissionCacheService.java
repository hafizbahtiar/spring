package com.hafizbahtiar.spring.features.permissions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hafizbahtiar.spring.features.permissions.dto.UserPermissionsResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for caching user permissions in Redis.
 * Provides methods to cache, retrieve, and invalidate permission data.
 */
@Service
@Slf4j
public class PermissionCacheService {

    private static final String CACHE_KEY_PREFIX = "permissions:user:";
    private static final String CACHE_KEY_USER_GROUPS = "permissions:user:groups:";
    private static final String CACHE_KEY_GROUP_PERMISSIONS = "permissions:group:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public PermissionCacheService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Value("${permissions.cache.ttl:3600}") // Default: 1 hour
    private long cacheTtlSeconds;

    @Value("${permissions.cache.enabled:true}")
    private boolean cacheEnabled;

    /**
     * Cache user permissions.
     *
     * @param userId      User ID
     * @param permissions User permissions response
     */
    public void cacheUserPermissions(Long userId, UserPermissionsResponse permissions) {
        if (!cacheEnabled) {
            return;
        }

        try {
            String key = CACHE_KEY_PREFIX + userId;
            String json = objectMapper.writeValueAsString(permissions);
            redisTemplate.opsForValue().set(key, json, cacheTtlSeconds, TimeUnit.SECONDS);
            log.debug("Cached permissions for user ID: {}", userId);
        } catch (JsonProcessingException e) {
            log.warn("Failed to cache permissions for user ID: {}", userId, e);
        }
    }

    /**
     * Get cached user permissions.
     *
     * @param userId User ID
     * @return Cached permissions or null if not found
     */
    public UserPermissionsResponse getCachedUserPermissions(Long userId) {
        if (!cacheEnabled) {
            return null;
        }

        try {
            String key = CACHE_KEY_PREFIX + userId;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                String json = value.toString();
                UserPermissionsResponse permissions = objectMapper.readValue(json, UserPermissionsResponse.class);
                log.debug("Retrieved cached permissions for user ID: {}", userId);
                return permissions;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve cached permissions for user ID: {}", userId, e);
        }
        return null;
    }

    /**
     * Invalidate user permissions cache.
     * Called when user is added/removed from group or group permissions change.
     *
     * @param userId User ID
     */
    public void invalidateUserPermissions(Long userId) {
        if (!cacheEnabled) {
            return;
        }

        try {
            String key = CACHE_KEY_PREFIX + userId;
            redisTemplate.delete(key);
            log.debug("Invalidated permissions cache for user ID: {}", userId);
        } catch (Exception e) {
            log.warn("Failed to invalidate permissions cache for user ID: {}", userId, e);
        }
    }

    /**
     * Invalidate permissions cache for all users in a group.
     * Called when group permissions are modified or group is deleted.
     *
     * @param groupId Group ID
     */
    public void invalidateGroupPermissions(Long groupId) {
        if (!cacheEnabled) {
            return;
        }

        try {
            String key = CACHE_KEY_GROUP_PERMISSIONS + groupId;
            redisTemplate.delete(key);
            log.debug("Invalidated permissions cache for group ID: {}", groupId);
            // Note: We invalidate all user caches since we don't track which users are in
            // which groups in cache
            // This is acceptable as permission checks are relatively infrequent compared to
            // other operations
        } catch (Exception e) {
            log.warn("Failed to invalidate permissions cache for group ID: {}", groupId, e);
        }
    }

    /**
     * Invalidate all permission caches.
     * Use sparingly - only when major permission system changes occur.
     */
    public void invalidateAllPermissions() {
        if (!cacheEnabled) {
            return;
        }

        try {
            // Delete all keys matching the pattern
            redisTemplate.delete(redisTemplate.keys(CACHE_KEY_PREFIX + "*"));
            redisTemplate.delete(redisTemplate.keys(CACHE_KEY_USER_GROUPS + "*"));
            redisTemplate.delete(redisTemplate.keys(CACHE_KEY_GROUP_PERMISSIONS + "*"));
            log.info("Invalidated all permission caches");
        } catch (Exception e) {
            log.warn("Failed to invalidate all permission caches", e);
        }
    }

    /**
     * Check if caching is enabled.
     *
     * @return true if caching is enabled
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }
}
