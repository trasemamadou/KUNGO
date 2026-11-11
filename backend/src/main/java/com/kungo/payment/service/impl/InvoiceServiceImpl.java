package com.kungo.payment.service.impl;

import com.kungo.booking.entity.Booking;
import com.kungo.common.exception.BadRequestException;
import com.kungo.common.util.QrCodeGenerator;
import com.kungo.payment.domain.entity.Invoice;
import com.kungo.payment.domain.entity.Payment;
import com.kungo.payment.repository.InvoiceRepository;
import com.kungo.payment.service.InvoiceService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public Invoice generate(Payment payment, Booking booking) {
        String invoiceNumber = "KGO-" + Instant.now().atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + "-" + payment.getId().toString().substring(0, 8).toUpperCase();

        String html = buildHtml(invoiceNumber, payment, booking);
        String relativeUrl = renderAndStore(invoiceNumber, html);

        Invoice invoice = Invoice.builder()
                .payment(payment)
                .invoiceNumber(invoiceNumber)
                .pdfUrl(relativeUrl)
                .build();
        return invoiceRepository.save(invoice);
    }

    private String renderAndStore(String invoiceNumber, String html) {
        try {
            Path targetDir = Paths.get(uploadDir, "invoices").toAbsolutePath().normalize();
            Files.createDirectories(targetDir);
            Path targetFile = targetDir.resolve(invoiceNumber + ".pdf");

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
            return "/uploads/invoices/" + invoiceNumber + ".pdf";
        } catch (IOException e) {
            log.error("Failed to generate invoice PDF", e);
            throw new BadRequestException("payment.failed");
        }
    }

    private String buildHtml(String invoiceNumber, Payment payment, Booking booking) {
        String qrCode = qrCodeGenerator.toBase64Png("KUNGO|INVOICE|" + invoiceNumber + "|" + booking.getId(), 160);
        return """
                <html><body style="font-family:Arial">
                <h1>Facture %s</h1>
                <p>Réservation : %s</p>
                <p>Client : %s</p>
                <p>Professionnel : %s</p>
                <p>Prestation : %s</p>
                <p>Montant total : %s %s</p>
                <p>Commission plateforme : %s %s</p>
                <p>Montant net professionnel : %s %s</p>
                <p>Date : %s</p>
                <img src="%s" width="120" height="120" alt="QR code de vérification" />
                </body></html>
                """.formatted(
                invoiceNumber,
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
