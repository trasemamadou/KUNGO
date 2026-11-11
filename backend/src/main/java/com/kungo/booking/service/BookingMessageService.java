package com.kungo.booking.service;

import com.kungo.booking.dto.BookingMessageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public interface BookingMessageService {

    /** Liste les messages du fil de discussion et marque comme lus ceux reçus par l'utilisateur courant. */
    List<BookingMessageResponse> list(UUID bookingId);

    BookingMessageResponse send(UUID bookingId, String content, MultipartFile file);

    long unreadCount(UUID bookingId);
}
