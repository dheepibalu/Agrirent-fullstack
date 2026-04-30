package com.agriculture.rental.service;

import com.agriculture.rental.model.Booking;
import com.agriculture.rental.model.Payment;
import com.agriculture.rental.repository.BookingRepository;
import com.agriculture.rental.repository.PaymentRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
public class InvoiceService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final Font TITLE_FONT    = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, new BaseColor(27, 94, 32));
    private static final Font HEADER_FONT   = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    private static final Font NORMAL_FONT   = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.DARK_GRAY);
    private static final Font BOLD_FONT     = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.DARK_GRAY);
    private static final Font AMOUNT_FONT   = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, new BaseColor(27, 94, 32));
    private static final Font SMALL_FONT    = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.GRAY);

    public byte[] generateInvoice(Long bookingId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Optional<Payment> paymentOpt = paymentRepository.findByBookingId(bookingId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document, baos);

        document.open();

        // ===== HEADER =====
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1.5f, 1f});

        // Company info
        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setPadding(10);
        companyCell.setBackgroundColor(new BaseColor(27, 94, 32));

        Paragraph companyName = new Paragraph("🌾 AgriRent", TITLE_FONT);
        companyName.getFont().setColor(BaseColor.WHITE);
        companyCell.addElement(companyName);

        Paragraph companyDetails = new Paragraph(
            "Agriculture Equipment Rental System\n" +
            "📧 info@agrirent.com\n" +
            "📞 +91 98765 43210\n" +
            "📍 Agricultural Zone, Farm City",
            new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE));
        companyCell.addElement(companyDetails);
        headerTable.addCell(companyCell);

        // Invoice info
        PdfPCell invoiceCell = new PdfPCell();
        invoiceCell.setBorder(Rectangle.NO_BORDER);
        invoiceCell.setPadding(10);
        invoiceCell.setBackgroundColor(new BaseColor(46, 125, 50));
        invoiceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph invoiceTitle = new Paragraph("INVOICE",
            new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.WHITE));
        invoiceTitle.setAlignment(Element.ALIGN_RIGHT);
        invoiceCell.addElement(invoiceTitle);

        String bookingRef = "AGRI-" + booking.getCreatedAt().getYear() + "-" +
                            String.format("%03d", booking.getId());
        Paragraph invoiceNum = new Paragraph("# " + bookingRef,
            new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, new BaseColor(255, 193, 7)));
        invoiceNum.setAlignment(Element.ALIGN_RIGHT);
        invoiceCell.addElement(invoiceNum);

        Paragraph invoiceDate = new Paragraph(
            "Date: " + booking.getCreatedAt().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
            new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE));
        invoiceDate.setAlignment(Element.ALIGN_RIGHT);
        invoiceCell.addElement(invoiceDate);

        headerTable.addCell(invoiceCell);
        document.add(headerTable);

        document.add(Chunk.NEWLINE);

        // ===== CUSTOMER & BOOKING INFO =====
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingBefore(10);

        // Customer info
        PdfPCell customerCell = new PdfPCell();
        customerCell.setBorder(Rectangle.BOX);
        customerCell.setBorderColor(new BaseColor(200, 230, 201));
        customerCell.setPadding(12);
        customerCell.setBackgroundColor(new BaseColor(232, 245, 233));

        customerCell.addElement(new Paragraph("BILL TO:", BOLD_FONT));
        customerCell.addElement(new Paragraph(booking.getUser().getFullName(), BOLD_FONT));
        customerCell.addElement(new Paragraph("Username: " + booking.getUser().getUsername(), NORMAL_FONT));
        customerCell.addElement(new Paragraph("Email: " + booking.getUser().getEmail(), NORMAL_FONT));
        if (booking.getUser().getPhone() != null) {
            customerCell.addElement(new Paragraph("Phone: " + booking.getUser().getPhone(), NORMAL_FONT));
        }
        infoTable.addCell(customerCell);

        // Booking info
        PdfPCell bookingCell = new PdfPCell();
        bookingCell.setBorder(Rectangle.BOX);
        bookingCell.setBorderColor(new BaseColor(200, 230, 201));
        bookingCell.setPadding(12);
        bookingCell.setBackgroundColor(new BaseColor(241, 248, 233));

        bookingCell.addElement(new Paragraph("BOOKING DETAILS:", BOLD_FONT));
        bookingCell.addElement(new Paragraph("Booking Ref: " + bookingRef, BOLD_FONT));
        bookingCell.addElement(new Paragraph("Status: " + booking.getStatus(), NORMAL_FONT));
        bookingCell.addElement(new Paragraph("Start Date: " + booking.getStartDate(), NORMAL_FONT));
        bookingCell.addElement(new Paragraph("End Date: " + booking.getEndDate(), NORMAL_FONT));
        bookingCell.addElement(new Paragraph("Duration: " + booking.getTotalDays() + " day(s)", NORMAL_FONT));
        infoTable.addCell(bookingCell);

        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        // ===== EQUIPMENT TABLE =====
        PdfPTable itemTable = new PdfPTable(5);
        itemTable.setWidthPercentage(100);
        itemTable.setWidths(new float[]{3f, 1.5f, 1.5f, 1.5f, 1.5f});
        itemTable.setSpacingBefore(10);

        // Table headers
        String[] headers = {"Equipment", "Category", "Daily Rate", "Days", "Total"};
        for (String h : headers) {
            PdfPCell hCell = new PdfPCell(new Phrase(h, HEADER_FONT));
            hCell.setBackgroundColor(new BaseColor(27, 94, 32));
            hCell.setPadding(10);
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemTable.addCell(hCell);
        }

        // Equipment row
        addTableCell(itemTable, booking.getEquipment().getName(), Element.ALIGN_LEFT, false);
        addTableCell(itemTable, booking.getEquipment().getCategory(), Element.ALIGN_CENTER, false);
        addTableCell(itemTable, "₹" + booking.getEquipment().getDailyRate().toPlainString(), Element.ALIGN_CENTER, false);
        addTableCell(itemTable, String.valueOf(booking.getTotalDays()), Element.ALIGN_CENTER, false);
        addTableCell(itemTable, "₹" + booking.getTotalAmount().toPlainString(), Element.ALIGN_CENTER, true);

        // Empty row
        for (int i = 0; i < 5; i++) {
            PdfPCell empty = new PdfPCell(new Phrase(" "));
            empty.setBorder(Rectangle.NO_BORDER);
            empty.setFixedHeight(10);
            itemTable.addCell(empty);
        }

        // Total row
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL AMOUNT", BOLD_FONT));
        totalLabelCell.setColspan(4);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setPadding(10);
        totalLabelCell.setBackgroundColor(new BaseColor(232, 245, 233));
        totalLabelCell.setBorderColor(new BaseColor(200, 230, 201));
        itemTable.addCell(totalLabelCell);

        PdfPCell totalAmountCell = new PdfPCell(
            new Phrase("₹" + booking.getTotalAmount().toPlainString(), AMOUNT_FONT));
        totalAmountCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalAmountCell.setPadding(10);
        totalAmountCell.setBackgroundColor(new BaseColor(200, 230, 201));
        totalAmountCell.setBorderColor(new BaseColor(200, 230, 201));
        itemTable.addCell(totalAmountCell);

        document.add(itemTable);
        document.add(Chunk.NEWLINE);

        // ===== PAYMENT INFO =====
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            PdfPTable payTable = new PdfPTable(2);
            payTable.setWidthPercentage(60);
            payTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addPayRow(payTable, "Payment Status:", payment.getPaymentStatus().name());
            addPayRow(payTable, "Payment Method:", payment.getPaymentMethod().name());
            if (payment.getTransactionId() != null) {
                addPayRow(payTable, "Transaction ID:", payment.getTransactionId());
            }
            if (payment.getFarmerTransactionId() != null) {
                addPayRow(payTable, "UPI Ref:", payment.getFarmerTransactionId());
            }
            if (payment.getPaymentDate() != null) {
                addPayRow(payTable, "Payment Date:",
                    payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            }
            document.add(payTable);
        }

        document.add(Chunk.NEWLINE);

        // ===== NOTES =====
        if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
            Paragraph notesTitle = new Paragraph("Notes:", BOLD_FONT);
            document.add(notesTitle);
            document.add(new Paragraph(booking.getNotes(), NORMAL_FONT));
            document.add(Chunk.NEWLINE);
        }

        // ===== FOOTER =====
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);
        footerTable.setSpacingBefore(20);

        PdfPCell footerCell = new PdfPCell();
        footerCell.setBackgroundColor(new BaseColor(27, 94, 32));
        footerCell.setPadding(12);
        footerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph footer = new Paragraph(
            "Thank you for choosing AgriRent! 🌾\n" +
            "For support: info@agrirent.com | +91 98765 43210\n" +
            "This is a computer-generated invoice.",
            new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE));
        footer.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(footer);
        footerTable.addCell(footerCell);
        document.add(footerTable);

        document.close();
        return baos.toByteArray();
    }

    private void addTableCell(PdfPTable table, String text, int align, boolean highlight) {
        PdfPCell cell = new PdfPCell(new Phrase(text, highlight ? BOLD_FONT : NORMAL_FONT));
        cell.setPadding(8);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new BaseColor(200, 230, 201));
        if (highlight) cell.setBackgroundColor(new BaseColor(232, 245, 233));
        table.addCell(cell);
    }

    private void addPayRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setPadding(6);
        labelCell.setBorderColor(new BaseColor(200, 230, 201));
        labelCell.setBackgroundColor(new BaseColor(232, 245, 233));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setPadding(6);
        valueCell.setBorderColor(new BaseColor(200, 230, 201));
        table.addCell(valueCell);
    }
}
