package com.kungo.booking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.enums.BookingStatus;
import com.kungo.common.domain.Money;
import com.kungo.common.exception.InvalidStateTransitionException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Vérifie que {@link Booking} n'autorise que les transitions de statut légales,
 * conformément à la machine à états documentée dans docs/UML-CLASSES.md.
 */
@DisplayName("Booking - machine à états")
class BookingStateMachineTest {

    private Booking newPendingBooking() {
        return Booking.builder()
                .eventDate(LocalDateTime.now().plusDays(10))
                .totalPrice(Money.of(new BigDecimal("200000"), "XOF"))
                .build(); // status = PENDING par défaut
    }

    private Booking paidBooking() {
        Booking booking = newPendingBooking();
        booking.accept();
        booking.confirmPayment(new BigDecimal("12000"), new BigDecimal("188000"));
        return booking;
    }

    @Test
    @DisplayName("PENDING -> ACCEPTED est autorisé")
    void shouldAcceptFromPending() {
        Booking booking = newPendingBooking();

        booking.accept();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
    }

    @Test
    @DisplayName("ACCEPTED -> ACCEPTED (double acceptation) est refusé")
    void shouldRejectDoubleAcceptance() {
        Booking booking = newPendingBooking();
        booking.accept();

        assertThatThrownBy(booking::accept).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("PENDING -> NEGOTIATING -> ACCEPTED applique les termes de la proposition acceptée")
    void shouldAcceptNegotiatedProposal() {
        Booking booking = newPendingBooking();
        LocalDateTime newDate = LocalDateTime.now().plusDays(20);
        Money newPrice = Money.of(new BigDecimal("150000"), "XOF");

        booking.enterNegotiation();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.NEGOTIATING);

        booking.acceptProposal(newPrice, newDate, 180, "Matériel sonorisation fourni");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
        assertThat(booking.getEventDate()).isEqualTo(newDate);
        assertThat(booking.getTotalPrice().getAmount()).isEqualByComparingTo("150000");
        assertThat(booking.getEstimatedDurationMinutes()).isEqualTo(180);
    }

    @Test
    @DisplayName("enterNegotiation() est idempotent depuis NEGOTIATING")
    void shouldStayInNegotiationOnRepeatedProposals() {
        Booking booking = newPendingBooking();
        booking.enterNegotiation();
        booking.enterNegotiation();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.NEGOTIATING);
    }

    @Test
    @DisplayName("ACCEPTED -> PAID renseigne commission et montant professionnel")
    void shouldConfirmPayment() {
        Booking booking = newPendingBooking();
        booking.accept();

        booking.confirmPayment(new BigDecimal("12000"), new BigDecimal("88000"));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAID);
        assertThat(booking.getCommissionAmount()).isEqualByComparingTo("12000");
        assertThat(booking.getProfessionalAmount()).isEqualByComparingTo("88000");
    }

    @Test
    @DisplayName("Impossible de payer une réservation encore PENDING")
    void shouldRejectPaymentOnPendingBooking() {
        Booking booking = newPendingBooking();

        assertThatThrownBy(() -> booking.confirmPayment(BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("Le suivi granulaire PAID -> PREPARING -> EN_ROUTE -> ARRIVED -> IN_PROGRESS fonctionne")
    void shouldWalkThroughFulfillmentSteps() {
        Booking booking = paidBooking();

        booking.prepare();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PREPARING);

        booking.startTravel();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.EN_ROUTE);

        booking.markArrived();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ARRIVED);

        booking.start();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("start() peut être appelé directement depuis PAID (étapes intermédiaires facultatives)")
    void shouldAllowSkippingIntermediateFulfillmentSteps() {
        Booking booking = paidBooking();

        booking.start();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("COMPLETED n'est atteint qu'après confirmation des deux parties")
    void shouldRequireDualConfirmationToComplete() {
        Booking booking = paidBooking();
        booking.start();

        boolean afterClientOnly = booking.clientConfirmCompletion();
        assertThat(afterClientOnly).isFalse();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.IN_PROGRESS);
        assertThat(booking.getClientConfirmedAt()).isNotNull();

        boolean afterBoth = booking.professionalConfirmCompletion();
        assertThat(afterBoth).isTrue();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    @DisplayName("COMPLETED -> PAYOUT_SETTLED est autorisé une fois la double confirmation obtenue")
    void shouldSettlePayoutAfterCompletion() {
        Booking booking = paidBooking();
        booking.start();
        booking.clientConfirmCompletion();
        booking.professionalConfirmCompletion();

        booking.markPayoutSettled();

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PAYOUT_SETTLED);
    }

    @Test
    @DisplayName("Une réservation payée peut être annulée")
    void shouldCancelPaidBooking() {
        Booking booking = paidBooking();

        booking.cancel("Empêchement de dernière minute");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @DisplayName("Une réservation en cours de prestation ne peut plus être annulée")
    void shouldRejectCancelOnInProgressBooking() {
        Booking booking = paidBooking();
        booking.start();

        assertThatThrownBy(() -> booking.cancel("trop tard")).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    @DisplayName("isActive() reflète l'état terminal ou non de la réservation")
    void shouldExposeIsActive() {
        Booking booking = newPendingBooking();
        assertThat(booking.isActive()).isTrue();

        booking.reject("indisponible");
        assertThat(booking.isActive()).isFalse();
    }
}
