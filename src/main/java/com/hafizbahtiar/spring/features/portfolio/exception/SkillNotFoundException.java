package com.hafizbahtiar.spring.features.portfolio.exception;

/**
 * Exception thrown when a skill is not found.
 */
public class SkillNotFoundException extends RuntimeException {

    public SkillNotFoundException(String message) {
        super(message);
    }

    public static SkillNotFoundException byId(Long skillId) {
        return new SkillNotFoundException("Skill not found with ID: " + skillId);
    }

    public static SkillNotFoundException byIdAndUser(Long skillId, Long userId) {
        return new SkillNotFoundException("Skill not found with ID: " + skillId + " for user ID: " + userId);
    }
}
