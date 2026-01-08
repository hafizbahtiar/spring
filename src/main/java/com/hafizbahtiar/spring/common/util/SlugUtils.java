package com.hafizbahtiar.spring.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs from strings.
 */
public class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-|-$)");

    private SlugUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Generate a URL-friendly slug from a string.
     * 
     * @param input The input string to convert to a slug
     * @return A URL-friendly slug
     */
    public static String generateSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        // Normalize to NFD (decomposed form) to separate base characters from
        // diacritics
        String normalized = Normalizer.normalize(input.trim(), Normalizer.Form.NFD);

        // Remove diacritics (accents)
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // Convert to lowercase
        String lowercased = withoutAccents.toLowerCase(Locale.ENGLISH);

        // Replace whitespace with dashes
        String withDashes = WHITESPACE.matcher(lowercased).replaceAll("-");

        // Remove non-alphanumeric characters (except dashes)
        String cleaned = NON_LATIN.matcher(withDashes).replaceAll("");

        // Remove multiple consecutive dashes
        cleaned = cleaned.replaceAll("-+", "-");

        // Remove dashes from edges
        cleaned = EDGES_DASHES.matcher(cleaned).replaceAll("");

        // Limit length to 200 characters (matching database column)
        if (cleaned.length() > 200) {
            cleaned = cleaned.substring(0, 200);
            // Remove trailing dash if exists
            if (cleaned.endsWith("-")) {
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
        }

        return cleaned.isEmpty() ? "post" : cleaned;
    }

    /**
     * Generate a unique slug by appending a number if the slug already exists.
     * 
     * @param baseSlug      The base slug
     * @param existsChecker Function to check if slug exists
     * @return A unique slug
     */
    public static String generateUniqueSlug(String baseSlug,
            java.util.function.Function<String, Boolean> existsChecker) {
        String slug = baseSlug;
        int counter = 1;

        while (existsChecker.apply(slug)) {
            String suffix = "-" + counter;
            int maxLength = 200 - suffix.length();
            if (maxLength <= 0) {
                slug = "post-" + counter;
            } else {
                String base = baseSlug.length() > maxLength
                        ? baseSlug.substring(0, maxLength)
                        : baseSlug;
                slug = base + suffix;
            }
            counter++;
        }

        return slug;
    }
}
