package com.kungo.booking.service;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.dto.BookingCreateRequest;
import com.kungo.booking.dto.BookingProposalRequest;
import com.kungo.booking.dto.BookingSearchRequest;
import com.kungo.booking.dto.ReasonRequest;
import com.kungo.booking.dto.BookingAttachmentResponse;
import com.kungo.booking.dto.BookingProposalResponse;
import com.kungo.booking.dto.BookingResponse;
import com.kungo.booking.dto.BookingStatusHistoryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BookingService {

    Booking getEntityById(UUID id);

    BookingResponse getById(UUID id);

    List<BookingResponse> listForCurrentClient();

    List<BookingResponse> listForProfessional(UUID professionalId);

    Page<BookingResponse> search(BookingSearchRequest request, Pageable pageable);

    List<BookingStatusHistoryResponse> getHistory(UUID id);

    BookingResponse create(BookingCreateRequest request);

    BookingResponse accept(UUID id);

    BookingResponse reject(UUID id, ReasonRequest request);

    List<BookingProposalResponse> listProposals(UUID id);

    BookingProposalResponse submitProposal(UUID id, BookingProposalRequest request);

    BookingResponse acceptProposal(UUID id, UUID proposalId);

    BookingResponse rejectProposal(UUID id, UUID proposalId);

    BookingResponse prepare(UUID id);

    BookingResponse startTravel(UUID id);

    BookingResponse markArrived(UUID id);

    BookingResponse start(UUID id);

    BookingResponse confirmCompletion(UUID id);

    BookingResponse cancel(UUID id, ReasonRequest request);

    BookingAttachmentResponse uploadAttachment(UUID id, MultipartFile file);

    List<BookingAttachmentResponse> listAttachments(UUID id);

    /** Envoie un rappel de confirmation de fin aux deux parties pour les prestations passées non confirmées. */
    void sendCompletionReminders();
}
