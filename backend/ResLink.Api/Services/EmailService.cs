using SendGrid;
using SendGrid.Helpers.Mail;

namespace ResLink.Api.Services;

public class EmailService(IConfiguration configuration)
{
    public async Task SendPasswordResetAsync(string toEmail, string resetLink)
    {
        var apiKey = configuration["Email:ApiKey"];
        var from = configuration["Email:From"];

        if (string.IsNullOrWhiteSpace(apiKey) || string.IsNullOrWhiteSpace(from))
        {
            throw new InvalidOperationException("Email configuration is missing.");
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
            throw new InvalidOperationException($"Failed to send email: {response.StatusCode}");
        }
    }
}