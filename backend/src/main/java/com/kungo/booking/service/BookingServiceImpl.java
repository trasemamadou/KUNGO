package com.kungo.booking.service;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.entity.BookingAttachment;
import com.kungo.booking.entity.BookingProposal;
import com.kungo.booking.entity.BookingStatusHistory;
import com.kungo.booking.enums.BookingStatus;
import com.kungo.booking.enums.ProposalStatus;
import com.kungo.booking.dto.BookingCreateRequest;
import com.kungo.booking.dto.BookingProposalRequest;
import com.kungo.booking.dto.BookingSearchRequest;
import com.kungo.booking.dto.ReasonRequest;
import com.kungo.booking.dto.BookingAttachmentResponse;
import com.kungo.booking.dto.BookingProposalResponse;
import com.kungo.booking.dto.BookingResponse;
import com.kungo.booking.dto.BookingStatusHistoryResponse;
import com.kungo.booking.event.BookingCompletedEvent;
import com.kungo.booking.event.BookingCompletionReminderEvent;
import com.kungo.booking.event.BookingProposalSubmittedEvent;
import com.kungo.booking.event.BookingStatusChangedEvent;
import com.kungo.booking.mapper.BookingMapper;
import com.kungo.booking.repository.BookingAttachmentRepository;
import com.kungo.booking.repository.BookingProposalRepository;
import com.kungo.booking.repository.BookingRepository;
import com.kungo.booking.repository.BookingStatusHistoryRepository;
import com.kungo.booking.specification.BookingSpecifications;
import com.kungo.common.domain.Money;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ForbiddenException;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.common.util.FileStorageService;
import com.kungo.prestation.domain.entity.EventService;
import com.kungo.prestation.service.PrestationService;
import com.kungo.user.domain.entity.User;
import com.kungo.user.service.UserService;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final String ATTACHMENT_FOLDER = "booking-attachments";

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final BookingProposalRepository proposalRepository;
    private final BookingAttachmentRepository attachmentRepository;
    private final BookingMapper bookingMapper;
    private final PrestationService prestationService;
    private final UserService userService;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Booking getEntityById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("booking.not-found", id));
    }

    @Override
    public BookingResponse getById(UUID id) {
        Booking booking = getEntityById(id);
        requireParty(booking);
        return bookingMapper.toResponse(booking);
    }

    @Override
    public List<BookingResponse> listForCurrentClient() {
        return bookingRepository.findByClientId(SecurityUtils.currentUserId()).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    public List<BookingResponse> listForProfessional(UUID professionalId) {
        return bookingRepository.findByProfessionalId(professionalId).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    public Page<BookingResponse> search(BookingSearchRequest request, Pageable pageable) {
        return bookingRepository.findAll(BookingSpecifications.from(request), pageable)
                .map(bookingMapper::toResponse);
    }

    @Override
    public List<BookingStatusHistoryResponse> getHistory(UUID id) {
        requireParty(getEntityById(id));
        return historyRepository.findByBookingIdOrderByCreatedAtAsc(id).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookingResponse create(BookingCreateRequest request) {
        EventService service = prestationService.getEntityById(request.getServiceId());
        if (!service.isPublished()) {
            throw new BadRequestException("error.validation");
        }
        User client = userService.getEntityById(SecurityUtils.currentUserId());

        Money servicePrice = service.getPrice();
        boolean opensNegotiation = request.getProposedBudget() != null
                && servicePrice != null
                && request.getProposedBudget().compareTo(servicePrice.getAmount()) != 0;
        Money agreedPrice = opensNegotiation
                ? Money.of(request.getProposedBudget(), servicePrice.getCurrency())
                : servicePrice;

        Booking booking = Booking.builder()
                .client(client)
                .professional(service.getProfessional())
                .service(service)
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .eventAddress(request.getEventAddress())
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .participantCount(request.getParticipantCount())
                .description(request.getDescription())
                .specialRequests(request.getSpecialRequests())
                .totalPrice(agreedPrice)
                .build();

        Booking saved = bookingRepository.save(booking);
        recordHistory(saved, null, BookingStatus.PENDING, "Demande de réservation créée");
        publishStatusChange(saved, null, BookingStatus.PENDING);

        if (opensNegotiation) {
            BookingStatus previous = saved.getStatus();
            saved.enterNegotiation();
            bookingRepository.save(saved);

            BookingProposal proposal = BookingProposal.builder()
                    .booking(saved)
                    .proposedBy(client)
                    .proposedPrice(agreedPrice)
                    .message(request.getDescription())
                    .build();
            BookingProposal savedProposal = proposalRepository.save(proposal);

            recordHistory(saved, previous, saved.getStatus(), "Budget proposé par le client : " + agreedPrice.getAmount());
            publishStatusChange(saved, previous, saved.getStatus());
            eventPublisher.publishEvent(new BookingProposalSubmittedEvent(saved.getId(), savedProposal.getId(), client.getId()));
        }

        log.info("Booking {} created by client {} for service {}", saved.getId(), client.getId(), service.getId());
        return bookingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse accept(UUID id) {
        Booking booking = getEntityById(id);
        requireProfessionalOwner(booking);
        BookingStatus previous = booking.getStatus();
        booking.accept();
        recordHistory(booking, previous, booking.getStatus(), null);
        publishStatusChange(booking, previous, booking.getStatus());
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse reject(UUID id, ReasonRequest request) {
        Booking booking = getEntityById(id);
        requireProfessionalOwner(booking);
        BookingStatus previous = booking.getStatus();
        booking.reject(request.getReason());
        recordHistory(booking, previous, booking.getStatus(), request.getReason());
        publishStatusChange(booking, previous, booking.getStatus());
        return bookingMapper.toResponse(booking);
    }

    @Override
    public List<BookingProposalResponse> listProposals(UUID id) {
        Booking booking = getEntityById(id);
        requireParty(booking);
        return proposalRepository.findByBookingIdOrderByCreatedAtAsc(id).stream()
                .map(this::toProposalResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookingProposalResponse submitProposal(UUID id, BookingProposalRequest request) {
        Booking booking = getEntityById(id);
        UUID currentUserId = requireParty(booking);

        if (booking.getStatus() != BookingStatus.PENDING && booking.getStatus() != BookingStatus.NEGOTIATING) {
            throw new BadRequestException("booking.invalid-status-transition", booking.getStatus(), BookingStatus.NEGOTIATING);
        }
        if (request.getPrice() == null && request.getEventDate() == null
                && request.getDurationMinutes() == null && request.getConditions() == null) {
            throw new BadRequestException("error.validation");
        }

        // Toute proposition encore en attente est implicitement répondue par cette nouvelle offre.
        proposalRepository.findFirstByBookingIdAndStatusOrderByCreatedAtDesc(id, ProposalStatus.PENDING)
                .ifPresent(BookingProposal::supersede);

        User author = userService.getEntityById(currentUserId);
        BookingProposal proposal = BookingProposal.builder()
                .booking(booking)
                .proposedBy(author)
                .proposedPrice(request.getPrice() != null
                        ? Money.of(request.getPrice(), booking.getTotalPrice() != null ? booking.getTotalPrice().getCurrency() : "XOF")
                        : null)
                .proposedEventDate(request.getEventDate())
                .proposedDurationMinutes(request.getDurationMinutes())
                .proposedConditions(request.getConditions())
                .message(request.getMessage())
                .build();
        BookingProposal saved = proposalRepository.save(proposal);

        BookingStatus previous = booking.getStatus();
        booking.enterNegotiation();
        if (previous != booking.getStatus()) {
            recordHistory(booking, previous, booking.getStatus(), "Négociation ouverte");
            publishStatusChange(booking, previous, booking.getStatus());
        }

        eventPublisher.publishEvent(new BookingProposalSubmittedEvent(booking.getId(), saved.getId(), currentUserId));
        return toProposalResponse(saved);
    }

    @Override
    @Transactional
    public BookingResponse acceptProposal(UUID id, UUID proposalId) {
        Booking booking = getEntityById(id);
        BookingProposal proposal = getProposalOrThrow(booking, proposalId);
        UUID currentUserId = requireParty(booking);

        if (proposal.getProposedBy().getId().equals(currentUserId)) {
            throw new ForbiddenException("error.forbidden");
        }
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("error.validation");
        }

        proposal.accept();
        proposalRepository.save(proposal);

        BookingStatus previous = booking.getStatus();
        booking.acceptProposal(proposal.getProposedPrice(), proposal.getProposedEventDate(),
                proposal.getProposedDurationMinutes(), proposal.getProposedConditions());
        recordHistory(booking, previous, booking.getStatus(), "Proposition acceptée");
        publishStatusChange(booking, previous, booking.getStatus());

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse rejectProposal(UUID id, UUID proposalId) {
        Booking booking = getEntityById(id);
        BookingProposal proposal = getProposalOrThrow(booking, proposalId);
        UUID currentUserId = requireParty(booking);

        if (proposal.getProposedBy().getId().equals(currentUserId)) {
            throw new ForbiddenException("error.forbidden");
        }
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("error.validation");
        }

        proposal.reject();
        proposalRepository.save(proposal);
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse prepare(UUID id) {
        return applyProfessionalTransition(id, Booking::prepare);
    }

    @Override
    @Transactional
    public BookingResponse startTravel(UUID id) {
        return applyProfessionalTransition(id, Booking::startTravel);
    }

    @Override
    @Transactional
    public BookingResponse markArrived(UUID id) {
        return applyProfessionalTransition(id, Booking::markArrived);
    }

    @Override
    @Transactional
    public BookingResponse start(UUID id) {
        return applyProfessionalTransition(id, Booking::start);
    }

    @Override
    @Transactional
    public BookingResponse confirmCompletion(UUID id) {
        Booking booking = getEntityById(id);
        UUID currentUserId = SecurityUtils.currentUserId();
        boolean isClient = booking.getClient().getId().equals(currentUserId);
        boolean isProfessional = booking.getProfessional().getUser().getId().equals(currentUserId);
        if (!isClient && !isProfessional && !SecurityUtils.hasRole("ADMIN") && !SecurityUtils.hasRole("SUPER_ADMIN")) {
            throw new ForbiddenException("error.forbidden");
        }

        BookingStatus previous = booking.getStatus();
        boolean bothConfirmed = isClient ? booking.clientConfirmCompletion() : booking.professionalConfirmCompletion();

        if (bothConfirmed) {
            recordHistory(booking, previous, booking.getStatus(), "Prestation confirmée terminée par les deux parties");
            publishStatusChange(booking, previous, booking.getStatus());
            eventPublisher.publishEvent(new BookingCompletedEvent(booking.getId()));
        }

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancel(UUID id, ReasonRequest request) {
        Booking booking = getEntityById(id);
        requireParty(booking);
        BookingStatus previous = booking.getStatus();
        booking.cancel(request.getReason());
        recordHistory(booking, previous, booking.getStatus(), request.getReason());
        publishStatusChange(booking, previous, booking.getStatus());
        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    public BookingAttachmentResponse uploadAttachment(UUID id, MultipartFile file) {
        Booking booking = getEntityById(id);
        UUID currentUserId = requireParty(booking);
        User uploader = userService.getEntityById(currentUserId);

        String url = fileStorageService.store(file, ATTACHMENT_FOLDER);
        BookingAttachment attachment = BookingAttachment.builder()
                .booking(booking)
                .uploadedBy(uploader)
                .fileName(file.getOriginalFilename())
                .url(url)
                .build();
        return bookingMapper.toResponse(attachmentRepository.save(attachment));
    }

    @Override
    public List<BookingAttachmentResponse> listAttachments(UUID id) {
        Booking booking = getEntityById(id);
        requireParty(booking);
        return attachmentRepository.findByBookingIdOrderByCreatedAtAsc(id).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void sendCompletionReminders() {
        var statuses = EnumSet.of(BookingStatus.PAID, BookingStatus.PREPARING, BookingStatus.EN_ROUTE,
                BookingStatus.ARRIVED, BookingStatus.IN_PROGRESS);
        List<Booking> elapsed = bookingRepository.findByStatusInAndEventDateBefore(statuses, LocalDateTime.now());
        for (Booking booking : elapsed) {
            if (booking.getClientConfirmedAt() == null || booking.getProfessionalConfirmedAt() == null) {
                eventPublisher.publishEvent(new BookingCompletionReminderEvent(booking.getId()));
            }
        }
        if (!elapsed.isEmpty()) {
            log.info("Sent completion reminders for {} elapsed booking(s)", elapsed.size());
        }
    }

    private BookingResponse applyProfessionalTransition(UUID id, java.util.function.Consumer<Booking> transition) {
        Booking booking = getEntityById(id);
        requireProfessionalOwner(booking);
        BookingStatus previous = booking.getStatus();
        transition.accept(booking);
        recordHistory(booking, previous, booking.getStatus(), null);
        publishStatusChange(booking, previous, booking.getStatus());
        return bookingMapper.toResponse(booking);
    }

    private BookingProposalResponse toProposalResponse(BookingProposal proposal) {
        BookingProposalResponse response = bookingMapper.toResponse(proposal);
        response.setProposedByClient(proposal.getBooking().getClient().getId().equals(proposal.getProposedBy().getId()));
        return response;
    }

    private BookingProposal getProposalOrThrow(Booking booking, UUID proposalId) {
        return proposalRepository.findById(proposalId)
                .filter(p -> p.getBooking().getId().equals(booking.getId()))
                .orElseThrow(() -> ResourceNotFoundException.of("error.not-found", proposalId));
    }

    private void requireProfessionalOwner(Booking booking) {
        SecurityUtils.requireOwnerOrRole(booking.getProfessional().getUser().getId(), "ADMIN", "SUPER_ADMIN");
    }

    /** Vérifie que l'utilisateur courant est le client, le professionnel, ou un admin ; retourne son id. */
    private UUID requireParty(Booking booking) {
        UUID currentUserId = SecurityUtils.currentUserId();
        boolean isParty = booking.getClient().getId().equals(currentUserId)
                || booking.getProfessional().getUser().getId().equals(currentUserId);
        if (!isParty && !SecurityUtils.hasRole("ADMIN") && !SecurityUtils.hasRole("SUPER_ADMIN")) {
            throw new ForbiddenException("error.forbidden");
        }
        return currentUserId;
    }

    private void recordHistory(Booking booking, BookingStatus from, BookingStatus to, String comment) {
        BookingStatusHistory history = BookingStatusHistory.builder()
                .booking(booking)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(SecurityUtils.getCurrentPrincipal().map(p -> p.getId()).orElse(null))
                .comment(comment)
                .build();
        historyRepository.save(history);
    }

    private void publishStatusChange(Booking booking, BookingStatus from, BookingStatus to) {
        eventPublisher.publishEvent(new BookingStatusChangedEvent(booking.getId(), from, to));
    }
}
