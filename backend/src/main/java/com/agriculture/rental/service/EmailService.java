package com.agriculture.rental.service;

import com.agriculture.rental.model.Booking;
import com.agriculture.rental.model.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@agrirent.com}")
    private String fromEmail;

    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmation(Booking booking) {
        if (mailSender == null) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("🌾 Booking Confirmed - AgriRent | " + booking.getEquipment().getName());

            String bookingRef = "AGRI-" + booking.getCreatedAt().getYear() + "-" +
                                String.format("%03d", booking.getId());

            String html = buildBookingEmailHtml(booking, bookingRef);
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("✅ Booking confirmation email sent to: " + booking.getUser().getEmail());
        } catch (Exception e) {
            System.err.println("⚠️ Email sending failed: " + e.getMessage());
        }
    }

    /**
     * Send payment confirmation email
     */
    public void sendPaymentConfirmation(Payment payment) {
        if (mailSender == null) return;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(payment.getBooking().getUser().getEmail());
            helper.setSubject("✅ Payment Confirmed - AgriRent | ₹" + payment.getAmount());

            String html = buildPaymentEmailHtml(payment);
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("✅ Payment confirmation email sent to: " +
                               payment.getBooking().getUser().getEmail());
        } catch (Exception e) {
            System.err.println("⚠️ Email sending failed: " + e.getMessage());
        }
    }

    private String buildBookingEmailHtml(Booking booking, String bookingRef) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #1b5e20, #2e7d32); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .header p { margin: 8px 0 0; opacity: 0.9; }
                    .body { padding: 30px; }
                    .greeting { font-size: 18px; color: #2e7d32; font-weight: bold; margin-bottom: 16px; }
                    .info-box { background: #e8f5e9; border-left: 4px solid #2e7d32; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #c8e6c9; }
                    .info-row:last-child { border-bottom: none; }
                    .label { color: #546e7a; font-size: 14px; }
                    .value { font-weight: bold; color: #263238; font-size: 14px; }
                    .amount { font-size: 24px; font-weight: bold; color: #2e7d32; text-align: center; padding: 16px; background: #f1f8e9; border-radius: 8px; margin: 20px 0; }
                    .footer { background: #1b5e20; color: rgba(255,255,255,0.8); padding: 20px; text-align: center; font-size: 13px; }
                    .btn { display: inline-block; background: #2e7d32; color: white; padding: 12px 28px; border-radius: 25px; text-decoration: none; font-weight: bold; margin: 16px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🌾 AgriRent</h1>
                        <p>Agriculture Equipment Rental System</p>
                    </div>
                    <div class="body">
                        <div class="greeting">Hello, %s! 👋</div>
                        <p>Your equipment rental booking has been <strong style="color:#2e7d32;">confirmed successfully!</strong></p>
                        
                        <div class="info-box">
                            <div class="info-row">
                                <span class="label">Booking Reference</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Equipment</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Category</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Start Date</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">End Date</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Duration</span>
                                <span class="value">%s day(s)</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Status</span>
                                <span class="value" style="color:#2e7d32;">%s</span>
                            </div>
                        </div>
                        
                        <div class="amount">Total Amount: ₹%s</div>
                        
                        <p style="color:#546e7a; font-size:14px;">Please complete your payment to activate the booking. Visit AgriRent and go to "My Rentals" to make payment.</p>
                    </div>
                    <div class="footer">
                        <p>🌾 AgriRent — Agriculture Equipment Rental System</p>
                        <p>📧 info@agrirent.com | 📞 +91 98765 43210</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getUser().getFullName(),
                bookingRef,
                booking.getEquipment().getName(),
                booking.getEquipment().getCategory(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalDays(),
                booking.getStatus(),
                booking.getTotalAmount().toPlainString()
            );
    }

    private String buildPaymentEmailHtml(Payment payment) {
        Booking booking = payment.getBooking();
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 20px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #e65100, #ff6f00); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .body { padding: 30px; }
                    .success-icon { text-align: center; font-size: 60px; margin: 10px 0; }
                    .info-box { background: #fff3e0; border-left: 4px solid #ff6f00; border-radius: 8px; padding: 20px; margin: 20px 0; }
                    .info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid #ffe0b2; }
                    .info-row:last-child { border-bottom: none; }
                    .label { color: #546e7a; font-size: 14px; }
                    .value { font-weight: bold; color: #263238; font-size: 14px; }
                    .amount { font-size: 28px; font-weight: bold; color: #e65100; text-align: center; padding: 16px; background: #fff8e1; border-radius: 8px; margin: 20px 0; }
                    .footer { background: #1b5e20; color: rgba(255,255,255,0.8); padding: 20px; text-align: center; font-size: 13px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Payment Confirmed!</h1>
                        <p>AgriRent — Agriculture Equipment Rental</p>
                    </div>
                    <div class="body">
                        <div class="success-icon">🎉</div>
                        <p style="text-align:center; font-size:18px; color:#2e7d32; font-weight:bold;">
                            Payment received successfully!
                        </p>
                        
                        <div class="info-box">
                            <div class="info-row">
                                <span class="label">Customer</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Equipment</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Transaction ID</span>
                                <span class="value" style="font-family:monospace;">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Payment Method</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">Payment Status</span>
                                <span class="value" style="color:#2e7d32;">PAID ✅</span>
                            </div>
                        </div>
                        
                        <div class="amount">₹%s Paid</div>
                        
                        <p style="color:#546e7a; font-size:14px; text-align:center;">
                            Your equipment rental is now active. Enjoy farming! 🚜
                        </p>
                    </div>
                    <div class="footer">
                        <p>🌾 AgriRent — Agriculture Equipment Rental System</p>
                        <p>📧 info@agrirent.com | 📞 +91 98765 43210</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                booking.getUser().getFullName(),
                booking.getEquipment().getName(),
                payment.getTransactionId() != null ? payment.getTransactionId() : "N/A",
                payment.getPaymentMethod().name(),
                payment.getAmount().toPlainString()
            );
    }
}
