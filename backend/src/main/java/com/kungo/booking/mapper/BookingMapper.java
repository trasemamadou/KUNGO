package com.kungo.booking.mapper;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.entity.BookingAttachment;
import com.kungo.booking.entity.BookingDocument;
import com.kungo.booking.entity.BookingMessage;
import com.kungo.booking.entity.BookingProposal;
import com.kungo.booking.entity.BookingStatusHistory;
import com.kungo.booking.dto.BookingAttachmentResponse;
import com.kungo.booking.dto.BookingDocumentResponse;
import com.kungo.booking.dto.BookingMessageResponse;
import com.kungo.booking.dto.BookingProposalResponse;
import com.kungo.booking.dto.BookingResponse;
import com.kungo.booking.dto.BookingStatusHistoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.fullName", target = "clientName")
    @Mapping(source = "client.avatarUrl", target = "clientAvatarUrl")
    @Mapping(source = "professional.id", target = "professionalId")
    @Mapping(source = "professional.user.fullName", target = "professionalName")
    @Mapping(source = "professional.user.avatarUrl", target = "professionalAvatarUrl")
    @Mapping(source = "service.id", target = "serviceId")
    @Mapping(source = "service.title", target = "serviceTitle")
    @Mapping(source = "totalPrice.amount", target = "totalPrice")
    @Mapping(source = "totalPrice.currency", target = "currency")
    BookingResponse toResponse(Booking booking);

    @Mapping(source = "createdAt", target = "changedAt")
    BookingStatusHistoryResponse toResponse(BookingStatusHistory history);

    @Mapping(source = "proposedBy.id", target = "proposedById")
    @Mapping(source = "proposedBy.fullName", target = "proposedByName")
    @Mapping(source = "proposedPrice.amount", target = "proposedPrice")
    @Mapping(source = "proposedPrice.currency", target = "proposedPriceCurrency")
    @Mapping(target = "proposedByClient", ignore = true)
    BookingProposalResponse toResponse(BookingProposal proposal);

    @Mapping(source = "uploadedBy.id", target = "uploadedById")
    BookingAttachmentResponse toResponse(BookingAttachment attachment);

    BookingDocumentResponse toResponse(BookingDocument document);

    @Mapping(source = "sender.id", target = "senderId")
    @Mapping(source = "sender.fullName", target = "senderName")
    @Mapping(source = "sender.avatarUrl", target = "senderAvatarUrl")
    BookingMessageResponse toResponse(BookingMessage message);
}
