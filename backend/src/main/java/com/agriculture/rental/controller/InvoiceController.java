package com.agriculture.rental.controller;

import com.agriculture.rental.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/invoice")
@CrossOrigin(origins = "*")
public class InvoiceController {

    @Autowired
    private InvoiceService invoiceService;

    /**
     * GET /api/invoice/{bookingId} - Download PDF invoice
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Long bookingId) {
        try {
            byte[] pdfBytes = invoiceService.generateInvoice(bookingId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                "AgriRent-Invoice-AGRI-" + bookingId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
