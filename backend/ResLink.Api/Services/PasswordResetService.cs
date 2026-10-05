using System.Security.Cryptography;
using System.Text;
using Microsoft.AspNetCore.WebUtilities;
using Microsoft.EntityFrameworkCore;
using ResLink.Api.Data;
using ResLink.Api.Domain;

namespace ResLink.Api.Services;

// One-time reset links store only a SHA-256 hash of the token and expire after 30 minutes (OWASP, 2023).
public class PasswordResetService(AppDbContext db, IConfiguration configuration)
{
    public static readonly TimeSpan Lifetime = TimeSpan.FromMinutes(30);

    public async Task<(string Link, DateTime ExpiresAt)> CreateLinkAsync(AppUser user, HttpRequest request)
    {
        var rawToken = Convert.ToBase64String(Guid.NewGuid().ToByteArray()) +
                       "." +
                       Convert.ToBase64String(RandomNumberGenerator.GetBytes(32));

        var existingTokens = db.PasswordResetTokens
            .Where(t => t.UserId == user.Id && t.UsedAt == null && t.ExpiresAt > DateTime.UtcNow);
        db.PasswordResetTokens.RemoveRange(existingTokens);

        var expiresAt = DateTime.UtcNow.Add(Lifetime);
        db.PasswordResetTokens.Add(new PasswordResetToken
        {
            Id = Guid.NewGuid(),
            UserId = user.Id,
            TokenHash = Hash(rawToken),
            ExpiresAt = expiresAt
        });
        await db.SaveChangesAsync();

        var baseUrl = configuration["App:BaseUrl"];
        if (string.IsNullOrWhiteSpace(baseUrl))
        {
            baseUrl = $"{request.Scheme}://{request.Host}";
        }

        var encodedToken = WebEncoders.Base64UrlEncode(Encoding.UTF8.GetBytes(rawToken));
        return ($"{baseUrl.TrimEnd('/')}/reset-password?token={encodedToken}", expiresAt);
    }

    public async Task<PasswordResetToken?> FindValidTokenAsync(string encodedToken)
    {
        string rawToken;
        try
        {
            rawToken = Encoding.UTF8.GetString(WebEncoders.Base64UrlDecode(encodedToken));
        }
        catch (FormatException)
        {
            return null;
        }

        var tokenHash = Hash(rawToken);
        return await db.PasswordResetTokens
            .Include(t => t.User)
            .FirstOrDefaultAsync(t =>
                t.TokenHash == tokenHash &&
                t.UsedAt == null &&
                t.ExpiresAt > DateTime.UtcNow);
    }

    private static string Hash(string rawToken) =>
        Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(rawToken)));
}
