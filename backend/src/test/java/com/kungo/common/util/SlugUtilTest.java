package com.kungo.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SlugUtilTest {

    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource({
            "Musique & Live, musique-live",
            "Décoration et Traiteur, decoration-et-traiteur",
            "  Espaces   multiples  , espaces-multiples",
            "Événementiel Pro, evenementiel-pro"
    })
    void shouldGenerateUrlFriendlySlug(String input, String expected) {
        assertThat(SlugUtil.toSlug(input)).isEqualTo(expected);
    }

    @Test
    void shouldLowercaseResult() {
        assertThat(SlugUtil.toSlug("DJ ANIMATION")).isEqualTo("dj-animation");
    }
}
