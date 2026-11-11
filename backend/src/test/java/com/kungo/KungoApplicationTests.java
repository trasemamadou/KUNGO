package com.kungo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Test de fumée : vérifie que l'intégralité du contexte Spring (tous les modules,
 * ~40 beans de service/repository/mapper/sécurité) démarre sans erreur de câblage.
 * Utilise le profil {@code test} (H2 en mode compatibilité PostgreSQL, cache simple).
 */
@SpringBootTest
@ActiveProfiles("test")
class KungoApplicationTests {

    @Test
    void contextLoads() {
        // Le simple démarrage du contexte constitue l'assertion.
    }
}
