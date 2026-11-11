package com.kungo.common.mail;

public interface EmailService {

    /**
     * Envoie un email HTML de façon asynchrone (ne bloque jamais le thread appelant).
     */
    void sendHtml(String to, String subject, String htmlBody);
}
