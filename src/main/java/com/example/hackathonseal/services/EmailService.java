package com.example.hackathonseal.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send email verification OTP after registration
     */
    @Async
    public void sendVerificationEmail(String toEmail, String fullName, String otp) {
        String subject = "[HackathonSeal] Mã xác thực email của bạn: " + otp;
        String body = buildVerificationEmailBody(fullName, otp);
        sendHtmlEmail(toEmail, subject, body);
    }

    /**
     * Send password reset OTP
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String fullName, String otp) {
        String subject = "[HackathonSeal] Mã đặt lại mật khẩu: " + otp;
        String body = buildPasswordResetEmailBody(fullName, otp);
        sendHtmlEmail(toEmail, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "HackathonSeal");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    private String buildVerificationEmailBody(String fullName, String otp) {
        // Split OTP into individual digits for display
        String[] digits = otp.split("");
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Xác thực Email</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 32px rgba(0,0,0,0.10);">
                        <!-- Header -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:40px 48px;text-align:center;">
                            <h1 style="color:#fff;margin:0;font-size:26px;font-weight:700;letter-spacing:-0.5px;">🏆 HackathonSeal</h1>
                            <p style="color:rgba(255,255,255,0.8);margin:8px 0 0;font-size:14px;">Nền tảng thi đấu hackathon</p>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                          <td style="padding:44px 48px;">
                            <h2 style="color:#1a1a2e;margin:0 0 12px;font-size:20px;font-weight:600;">Xin chào, %s! 👋</h2>
                            <p style="color:#4a5568;font-size:15px;line-height:1.7;margin:0 0 32px;">
                              Vui lòng nhập mã OTP bên dưới để xác thực địa chỉ email và kích hoạt tài khoản của bạn.
                            </p>
                
                            <!-- OTP Box -->
                            <div style="background:linear-gradient(135deg,#f7f4ff,#ede9fe);border:2px dashed #8b5cf6;border-radius:12px;padding:28px;text-align:center;margin:0 0 28px;">
                              <p style="color:#6d28d9;font-size:12px;font-weight:600;letter-spacing:2px;text-transform:uppercase;margin:0 0 16px;">Mã xác thực OTP</p>
                              <div style="display:inline-flex;gap:8px;justify-content:center;">
                                %s%s%s%s%s%s
                              </div>
                              <p style="color:#7c3aed;font-size:13px;margin:16px 0 0;font-weight:500;">⏰ Mã có hiệu lực trong <strong>24 giờ</strong></p>
                            </div>
                
                            <div style="background:#f0fdf4;border-left:4px solid #22c55e;border-radius:4px;padding:14px 16px;margin:0 0 24px;">
                              <p style="color:#15803d;margin:0;font-size:13px;">
                                💡 <strong>Cách dùng:</strong> Nhập mã này vào ô OTP trong ứng dụng để xác thực email.
                              </p>
                            </div>
                
                            <p style="color:#94a3b8;font-size:13px;line-height:1.6;margin:0;">
                              Nếu bạn không đăng ký tài khoản này, hãy bỏ qua email này.
                            </p>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#f8fafc;padding:20px 48px;text-align:center;border-top:1px solid #e2e8f0;">
                            <p style="color:#94a3b8;font-size:12px;margin:0;">© 2026 HackathonSeal. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                fullName,
                otpDigitBox(digits[0]),
                otpDigitBox(digits[1]),
                otpDigitBox(digits[2]),
                otpDigitBox(digits[3]),
                otpDigitBox(digits[4]),
                otpDigitBox(digits[5])
        );
    }

    private String buildPasswordResetEmailBody(String fullName, String otp) {
        String[] digits = otp.split("");
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Đặt lại mật khẩu</title>
                </head>
                <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:'Segoe UI',Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 0;">
                    <tr><td align="center">
                      <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 32px rgba(0,0,0,0.10);">
                        <!-- Header -->
                        <tr>
                          <td style="background:linear-gradient(135deg,#f093fb 0%%,#f5576c 100%%);padding:40px 48px;text-align:center;">
                            <h1 style="color:#fff;margin:0;font-size:26px;font-weight:700;letter-spacing:-0.5px;">🔐 HackathonSeal</h1>
                            <p style="color:rgba(255,255,255,0.8);margin:8px 0 0;font-size:14px;">Đặt lại mật khẩu</p>
                          </td>
                        </tr>
                        <!-- Body -->
                        <tr>
                          <td style="padding:44px 48px;">
                            <h2 style="color:#1a1a2e;margin:0 0 12px;font-size:20px;font-weight:600;">Xin chào, %s! 👋</h2>
                            <p style="color:#4a5568;font-size:15px;line-height:1.7;margin:0 0 32px;">
                              Chúng tôi nhận được yêu cầu đặt lại mật khẩu. Nhập mã OTP bên dưới để tiếp tục.
                            </p>
                
                            <!-- OTP Box -->
                            <div style="background:linear-gradient(135deg,#fff5f5,#ffe4e6);border:2px dashed #f43f5e;border-radius:12px;padding:28px;text-align:center;margin:0 0 28px;">
                              <p style="color:#be123c;font-size:12px;font-weight:600;letter-spacing:2px;text-transform:uppercase;margin:0 0 16px;">Mã OTP đặt lại mật khẩu</p>
                              <div style="display:inline-flex;gap:8px;justify-content:center;">
                                %s%s%s%s%s%s
                              </div>
                              <p style="color:#e11d48;font-size:13px;margin:16px 0 0;font-weight:500;">⏰ Mã có hiệu lực trong <strong>15 phút</strong></p>
                            </div>
                
                            <div style="background:#fff8e1;border-left:4px solid #f59e0b;border-radius:4px;padding:14px 16px;margin:0 0 24px;">
                              <p style="color:#92400e;margin:0;font-size:13px;">
                                ⚠️ <strong>Bảo mật:</strong> Không chia sẻ mã này với bất kỳ ai. Nếu bạn không yêu cầu, hãy bỏ qua email này — tài khoản vẫn an toàn.
                              </p>
                            </div>
                
                            <p style="color:#94a3b8;font-size:13px;line-height:1.6;margin:0;">
                              Sau khi đặt lại mật khẩu thành công, mã OTP này sẽ hết hiệu lực.
                            </p>
                          </td>
                        </tr>
                        <!-- Footer -->
                        <tr>
                          <td style="background:#f8fafc;padding:20px 48px;text-align:center;border-top:1px solid #e2e8f0;">
                            <p style="color:#94a3b8;font-size:12px;margin:0;">© 2026 HackathonSeal. All rights reserved.</p>
                          </td>
                        </tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(
                fullName,
                otpDigitBox(digits[0]),
                otpDigitBox(digits[1]),
                otpDigitBox(digits[2]),
                otpDigitBox(digits[3]),
                otpDigitBox(digits[4]),
                otpDigitBox(digits[5])
        );
    }

    /**
     * Render a single OTP digit as a styled box
     */
    private String otpDigitBox(String digit) {
        return "<span style=\"display:inline-block;width:44px;height:52px;line-height:52px;background:#fff;border:2px solid #c4b5fd;border-radius:8px;font-size:28px;font-weight:700;color:#4c1d95;text-align:center;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
                + digit + "</span>";
    }
}
