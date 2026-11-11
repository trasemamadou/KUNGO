package com.kungo.booking.service;

import com.kungo.booking.entity.Booking;
import com.kungo.booking.entity.BookingDocument;
import com.kungo.booking.enums.BookingDocumentType;
import com.kungo.booking.dto.BookingDocumentResponse;
import com.kungo.booking.mapper.BookingMapper;
import com.kungo.booking.repository.BookingDocumentRepository;
import com.kungo.booking.repository.BookingRepository;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.exception.ForbiddenException;
import com.kungo.common.exception.ResourceNotFoundException;
import com.kungo.common.security.SecurityUtils;
import com.kungo.common.util.QrCodeGenerator;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Génère les documents PDF (contrat, reçu) attachés au cycle de vie d'une réservation.
 * La facture liée au paiement reste gérée par {@code payment.service.InvoiceService} ;
 * ce service couvre les deux autres pièces requises par la chaîne de réservation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingDocumentServiceImpl implements BookingDocumentService {

    private final BookingDocumentRepository documentRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    // REQUIRES_NEW : voir le commentaire équivalent dans PayoutServiceImpl#settleForCompletedBooking —
    // appelé depuis un listener AFTER_COMMIT, une propagation REQUIRED risque de ne jamais committer.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookingDocument generateContract(UUID bookingId) {
        return documentRepository.findByBookingIdAndType(bookingId, BookingDocumentType.CONTRACT)
                .orElseGet(() -> generate(bookingId, BookingDocumentType.CONTRACT, "CTR", this::buildContractHtml));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BookingDocument generateReceipt(UUID bookingId) {
        return documentRepository.findByBookingIdAndType(bookingId, BookingDocumentType.RECEIPT)
                .orElseGet(() -> generate(bookingId, BookingDocumentType.RECEIPT, "REC", this::buildReceiptHtml));
    }

    @Override
    public List<BookingDocumentResponse> list(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("booking.not-found", bookingId));
        UUID currentUserId = SecurityUtils.currentUserId();
        boolean isParty = booking.getClient().getId().equals(currentUserId)
                || booking.getProfessional().getUser().getId().equals(currentUserId);
        if (!isParty && !SecurityUtils.hasRole("ADMIN") && !SecurityUtils.hasRole("SUPER_ADMIN")) {
            throw new ForbiddenException("error.forbidden");
        }
        return documentRepository.findByBookingIdOrderByGeneratedAtAsc(bookingId).stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    private BookingDocument generate(UUID bookingId, BookingDocumentType type, String prefix,
            java.util.function.BiFunction<String, Booking, String> htmlBuilder) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ResourceNotFoundException.of("booking.not-found", bookingId));

        String documentNumber = "KGO-" + prefix + "-"
                + Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + bookingId.toString().substring(0, 8).toUpperCase();

        String html = htmlBuilder.apply(documentNumber, booking);
        String relativeUrl = renderAndStore(type, documentNumber, html);

        BookingDocument document = BookingDocument.builder()
                .booking(booking)
                .type(type)
                .documentNumber(documentNumber)
                .pdfUrl(relativeUrl)
                .build();
        BookingDocument saved = documentRepository.save(document);
        log.info("Document {} ({}) generated for booking {}", documentNumber, type, bookingId);
        return saved;
    }

    private String renderAndStore(BookingDocumentType type, String documentNumber, String html) {
        try {
            String subFolder = type == BookingDocumentType.CONTRACT ? "contracts" : "receipts";
            Path targetDir = Paths.get(uploadDir, subFolder).toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(documentNumber + ".pdf");

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                Document doc = org.jsoup.Jsoup.parse(html, "UTF-8");
                doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
                var w3cDoc = new W3CDom().fromJsoup(doc);
                builder.withW3cDocument(w3cDoc, null);
                builder.toStream(os);
                builder.run();
                Files.write(targetFile, os.toByteArray());
            }
            return "/uploads/" + subFolder + "/" + documentNumber + ".pdf";
        } catch (IOException e) {
            log.error("Failed to generate {} PDF", type, e);
            throw new BadRequestException("error.validation");
        }
    }

    private String buildContractHtml(String documentNumber, Booking booking) {
        String qrCode = qrCodeGenerator.toBase64Png("KUNGO|CONTRACT|" + documentNumber + "|" + booking.getId(), 160);
        return """
                <html><body style="font-family:Arial">
                <h1>Contrat de prestation %s</h1>
                <p>Entre le client <strong>%s</strong> et le professionnel <strong>%s</strong>.</p>
                <p>Prestation : %s</p>
                <p>Date de l'événement : %s</p>
                <p>Durée estimée : %s minutes</p>
                <p>Lieu : %s</p>
                <p>Montant convenu : %s %s</p>
                <p>Conditions particulières : %s</p>
                <p>Ce contrat matérialise l'accord conclu entre les deux parties via la plateforme KUNGO.
                Le paiement est effectué exclusivement via la plateforme, qui prélève une commission
                et reverse le solde net au professionnel après confirmation de la fin de la prestation
                par les deux parties.</p>
                <p>Émis le %s</p>
                <img src="%s" width="120" height="120" alt="QR code de vérification" />
                </body></html>
                """.formatted(
                documentNumber,
                booking.getClient().getFullName(),
                booking.getProfessional().getUser().getFullName(),
                booking.getService().getTitle(),
                booking.getEventDate(),
                booking.getEstimatedDurationMinutes() != null ? booking.getEstimatedDurationMinutes() : "-",
                booking.getEventAddress() != null ? booking.getEventAddress() : "-",
                booking.getTotalPrice().getAmount(), booking.getTotalPrice().getCurrency(),
                booking.getSpecialRequests() != null ? booking.getSpecialRequests() : "Aucune",
                Instant.now(),
                qrCode);
    }

    private String buildReceiptHtml(String documentNumber, Booking booking) {
        String qrCode = qrCodeGenerator.toBase64Png("KUNGO|RECEIPT|" + documentNumber + "|" + booking.getId(), 160);
        return """
                <html><body style="font-family:Arial">
                <h1>Reçu de prestation %s</h1>
                <p>Réservation : %s</p>
                <p>Client : %s</p>
                <p>Professionnel : %s</p>
                <p>Prestation : %s</p>
                <p>Montant total payé par le client : %s %s</p>
                <p>Commission plateforme : %s %s</p>
                <p>Montant net versé au professionnel : %s %s</p>
                <p>Ce document atteste que la prestation a été réalisée, confirmée par les deux parties,
                et que le versement au professionnel a été effectué par la plateforme KUNGO.</p>
                <p>Émis le %s</p>
                <img src="%s" width="120" height="120" alt="QR code de vérification" />
                </body></html>
                """.formatted(
                documentNumber,
                booking.getId(),
                booking.getClient().getFullName(),
                booking.getProfessional().getUser().getFullName(),
                booking.getService().getTitle(),
                booking.getTotalPrice().getAmount(), booking.getTotalPrice().getCurrency(),
                booking.getCommissionAmount(), booking.getTotalPrice().getCurrency(),
                booking.getProfessionalAmount(), booking.getTotalPrice().getCurrency(),
                Instant.now(),
                qrCode);
    }
}
