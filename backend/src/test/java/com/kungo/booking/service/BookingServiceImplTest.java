package com.kungo.booking.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.enums.BookingStatus;
import com.kungo.booking.dto.BookingCreateRequest;
import com.kungo.booking.mapper.BookingMapper;
import com.kungo.booking.repository.BookingAttachmentRepository;
import com.kungo.booking.repository.BookingProposalRepository;
import com.kungo.booking.repository.BookingRepository;
import com.kungo.booking.repository.BookingStatusHistoryRepository;
import com.kungo.category.Category;
import com.kungo.common.domain.Money;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ForbiddenException;
import com.kungo.common.security.UserPrincipal;
import com.kungo.common.util.FileStorageService;
import com.kungo.prestation.domain.entity.EventService;
import com.kungo.prestation.domain.enums.ServiceStatus;
import com.kungo.prestation.service.PrestationService;
import com.kungo.professional.domain.entity.ProfessionalProfile;
import com.kungo.user.domain.entity.User;
import com.kungo.user.service.UserService;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService")
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingStatusHistoryRepository historyRepository;
    @Mock private BookingProposalRepository proposalRepository;
    @Mock private BookingAttachmentRepository attachmentRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private PrestationService prestationService;
    @Mock private UserService userService;
    @Mock private FileStorageService fileStorageService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User client;
    private User professionalUser;
    private ProfessionalProfile professionalProfile;
    private EventService publishedService;

    @BeforeEach
    void setUp() {
        client = withId(User.builder().firstName("Awa").lastName("Diop").build(), UUID.randomUUID());
        professionalUser = withId(User.builder().firstName("DJ").lastName("Modou").build(), UUID.randomUUID());
        professionalProfile = withId(ProfessionalProfile.builder().user(professionalUser).build(), UUID.randomUUID());

        publishedService = withId(EventService.builder()
                .professional(professionalProfile)
                .category(withId(Category.builder().name("Musique").build(), UUID.randomUUID()))
                .title("Animation DJ mariage")
                .price(Money.of(java.math.BigDecimal.valueOf(200000), "XOF"))
                .status(ServiceStatus.PUBLISHED)
                .build(), UUID.randomUUID());
    }

    /** Renseigne l'id (hérité de {@code BaseEntity}, non exposé par le builder Lombok généré). */
    private static <T extends com.kungo.common.audit.BaseEntity> T withId(T entity, UUID id) {
        entity.setId(id);
        return entity;
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(User user) {
        UserPrincipal principal = UserPrincipal.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role("CLIENT")
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, java.util.List.of()));
    }

    @Test
    @DisplayName("create() refuse une prestation non publiée")
    void shouldRejectBookingOnUnpublishedService() {
        publishedService.setStatus(ServiceStatus.DRAFT);
        when(prestationService.getEntityById(publishedService.getId())).thenReturn(publishedService);
        authenticateAs(client);

        BookingCreateRequest request = BookingCreateRequest.builder()
                .serviceId(publishedService.getId())
                .eventDate(LocalDateTime.now().plusDays(5))
                .build();

        assertThatThrownBy(() -> bookingService.create(request)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("create() persiste une réservation PENDING et publie un événement")
    void shouldCreatePendingBooking() {
        when(prestationService.getEntityById(publishedService.getId())).thenReturn(publishedService);
        when(userService.getEntityById(client.getId())).thenReturn(client);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        authenticateAs(client);

        BookingCreateRequest request = BookingCreateRequest.builder()
                .serviceId(publishedService.getId())
                .eventDate(LocalDateTime.now().plusDays(5))
                .description("Mariage 200 invités")
                .build();

        bookingService.create(request);

        verify(bookingRepository).save(any(Booking.class));
        verify(historyRepository).save(any());
        verify(eventPublisher).publishEvent(any(com.kungo.booking.event.BookingStatusChangedEvent.class));
    }

    @Test
    @DisplayName("accept() est refusé si l'utilisateur courant n'est pas le professionnel propriétaire")
    void shouldRejectAcceptByNonOwner() {
        Booking booking = withId(Booking.builder()
                .client(client)
                .professional(professionalProfile)
                .service(publishedService)
                .status(BookingStatus.PENDING)
                .eventDate(LocalDateTime.now().plusDays(5))
                .build(), UUID.randomUUID());
        when(bookingRepository.findById(booking.getId())).thenReturn(java.util.Optional.of(booking));

        // Un autre client tente d'accepter à la place du professionnel
        authenticateAs(client);

        assertThatThrownBy(() -> bookingService.accept(booking.getId())).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("accept() fonctionne pour le professionnel propriétaire")
    void shouldAcceptForOwner() {
        Booking booking = withId(Booking.builder()
                .client(client)
                .professional(professionalProfile)
                .service(publishedService)
                .status(BookingStatus.PENDING)
                .eventDate(LocalDateTime.now().plusDays(5))
                .build(), UUID.randomUUID());
        when(bookingRepository.findById(booking.getId())).thenReturn(java.util.Optional.of(booking));
        authenticateAs(professionalUser);

        bookingService.accept(booking.getId());

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.ACCEPTED);
    }
}
