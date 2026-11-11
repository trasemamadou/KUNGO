package com.kungo.common.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Génère des slugs URL-friendly (catégories, sous-catégories) à partir d'un nom,
 * en supprimant les accents et caractères spéciaux.
 */
public final class SlugUtil {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private SlugUtil() {
    }

    public static String toSlug(String input) {
        String noWhitespace = WHITESPACE.matcher(input.trim()).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = DIACRITICS.matcher(normalized).replaceAll("");
        slug = NON_LATIN.matcher(slug).replaceAll("");
        return slug.toLowerCase().replaceAll("-{2,}", "-");
    }
}
