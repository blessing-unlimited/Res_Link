using SendGrid;
using SendGrid.Helpers.Mail;

namespace ResLink.Api.Services;

public class EmailService(IConfiguration configuration, ILogger<EmailService> logger)
{
    // Email waits for a residence domain, so a missing key skips sending; admins can create reset links instead.
    public async Task<bool> SendPasswordResetAsync(string toEmail, string resetLink)
    {
        var apiKey = configuration["Email:ApiKey"];
        var from = configuration["Email:From"];

        if (string.IsNullOrWhiteSpace(apiKey) || string.IsNullOrWhiteSpace(from))
        {
            logger.LogWarning("Email is not configured, so the password reset email was not sent.");
            return false;
        }

        var client = new SendGridClient(apiKey);
        var message = new SendGridMessage
        {
            From = new EmailAddress(from, "ResLink"),
            Subject = "Reset your ResLink password",
            PlainTextContent = $"Use this link to reset your password: {resetLink}\n\nThis link expires in 30 minutes.",
            HtmlContent = $@"
                <p>Use this link to reset your password:</p>
                <p><a href='{resetLink}'>Reset password</a></p>
                <p>This link expires in 30 minutes.</p>"
        };

        message.AddTo(new EmailAddress(toEmail));
        var response = await client.SendEmailAsync(message);

        if ((int)response.StatusCode < 200 || (int)response.StatusCode >= 300)
        {
            logger.LogError("SendGrid rejected the password reset email with status {Status}.", response.StatusCode);
            return false;
        }

        return true;
    }
}
