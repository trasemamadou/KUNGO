package com.kungo.booking.dto;

import com.kungo.booking.enums.BookingDocumentType;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDocumentResponse {
    private UUID id;
    private BookingDocumentType type;
    private String documentNumber;
    private String pdfUrl;
    private Instant generatedAt;
}
