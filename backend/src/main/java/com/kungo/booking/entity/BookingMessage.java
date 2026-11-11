package com.kungo.booking.entity;

import com.kungo.common.audit.BaseEntity;
import com.kungo.user.domain.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Message échangé entre le client et le professionnel dans le fil de discussion
 * d'une réservation. {@code read} indique si le destinataire (l'autre partie) l'a
 * consulté ; un message peut porter un texte, une pièce jointe, ou les deux.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"booking", "sender"})
@Entity
@Table(name = "booking_messages", indexes = {
        @Index(name = "idx_booking_messages_booking", columnList = "booking_id")
})
public class BookingMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "content", length = 2000)
    private String content;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "attachment_name")
    private String attachmentName;

    @Column(name = "read", nullable = false)
    @Builder.Default
    private boolean read = false;

    public void markRead() {
        this.read = true;
    }
}
