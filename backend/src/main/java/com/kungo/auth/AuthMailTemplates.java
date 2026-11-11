package com.kungo.auth;

/**
 * Templates HTML minimalistes pour les emails transactionnels d'authentification.
 * Volontairement inline (pas de moteur de template) : peu de variantes, contenu court.
 */
public final class AuthMailTemplates {

    private AuthMailTemplates() {
    }

    public static String activation(String firstName, String activationLink) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto">
                  <h2 style="color:#4f46e5">Bienvenue sur KUNGO, %s !</h2>
                  <p>Merci de vous être inscrit. Activez votre compte pour commencer :</p>
                  <p><a href="%s" style="background:#4f46e5;color:#fff;padding:12px 24px;
                     border-radius:8px;text-decoration:none;display:inline-block">Activer mon compte</a></p>
                  <p style="color:#6b7280;font-size:13px">Ce lien expire dans 24 heures.
                     Si vous n'êtes pas à l'origine de cette inscription, ignorez cet email.</p>
                </div>
                """.formatted(firstName, activationLink);
    }

    public static String passwordReset(String firstName, String resetLink) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto">
                  <h2 style="color:#4f46e5">Réinitialisation de mot de passe</h2>
                  <p>Bonjour %s, vous avez demandé la réinitialisation de votre mot de passe.</p>
                  <p><a href="%s" style="background:#4f46e5;color:#fff;padding:12px 24px;
                     border-radius:8px;text-decoration:none;display:inline-block">Réinitialiser</a></p>
                  <p style="color:#6b7280;font-size:13px">Ce lien expire dans 1 heure.
                     Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>
                </div>
                """.formatted(firstName, resetLink);
    }
}
