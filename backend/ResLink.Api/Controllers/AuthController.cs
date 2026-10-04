using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ResLink.Api.Auth;
using ResLink.Api.Contracts;
using ResLink.Api.Data;
using ResLink.Api.Domain;
using ResLink.Api.Services;
using System.Security.Cryptography;
using System.Text;
using Microsoft.AspNetCore.WebUtilities;

namespace ResLink.Api.Controllers;

[ApiController]
[Route("api/auth")]
public class AuthController(
    AppDbContext db,
    JwtTokenService tokens,
    IConfiguration configuration,
    CurrentUser currentUser,
    EmailService emailService) : ControllerBase
{
    // Identity PasswordHasher stores a salted hash rather than the raw password (Microsoft, 2024; OWASP, 2023).
    private readonly PasswordHasher<AppUser> _hasher = new();

    [HttpGet("residences")]
    public async Task<ActionResult<IReadOnlyList<ResidenceDto>>> Residences()
    {
        var items = await db.Residences
            .OrderBy(r => r.Name)
            .Select(r => new ResidenceDto(r.Id, r.Name, r.Campus, r.Address))
            .ToListAsync();
        return Ok(items);
    }

    [HttpPost("register/student")]
    public async Task<ActionResult<AuthResponse>> RegisterStudent(StudentRegisterRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Email) || string.IsNullOrWhiteSpace(request.Password))
        {
            return BadRequest("Email and password are required.");
        }

        if (!await db.Residences.AnyAsync(r => r.Id == request.ResidenceId))
        {
            return BadRequest("Residence was not found.");
        }

        if (await db.Users.AnyAsync(u => u.Email == request.Email.Trim().ToLower()))
        {
            return Conflict("An account with that email already exists.");
        }

        var user = new AppUser
        {
            Id = Guid.NewGuid(),
            FullName = request.FullName.Trim(),
            Email = request.Email.Trim().ToLower(),
            Role = Roles.Student,
            StudentNumber = request.StudentNumber.Trim(),
            Room = request.Room.Trim(),
            ResidenceId = request.ResidenceId,
            Points = 10
        };
        user.PasswordHash = _hasher.HashPassword(user, request.Password);
        db.Users.Add(user);
        await db.SaveChangesAsync();
        await db.Entry(user).Reference(u => u.Residence).LoadAsync();

        return Ok(DtoMapper.ToAuth(user, tokens.CreateToken(user)));
    }

    [HttpPost("register/staff")]
    public async Task<ActionResult<AuthResponse>> RegisterStaff(StaffRegisterRequest request)
    {
        var expectedCode = configuration["Staff:AccessCode"];
        if (!string.Equals(request.AccessCode, expectedCode, StringComparison.Ordinal))
        {
            return Unauthorized("Invalid staff access code.");
        }

        if (!Roles.Staff.Contains(request.Role))
        {
            return BadRequest("Staff role must be Admin, Security, or Maintenance.");
        }

        if (!await db.Residences.AnyAsync(r => r.Id == request.ResidenceId))
        {
            return BadRequest("Residence was not found.");
        }

        if (await db.Users.AnyAsync(u => u.Email == request.Email.Trim().ToLower()))
        {
            return Conflict("An account with that email already exists.");
        }

        var user = new AppUser
        {
            Id = Guid.NewGuid(),
            FullName = request.FullName.Trim(),
            Email = request.Email.Trim().ToLower(),
            Role = request.Role,
            StaffId = request.StaffId.Trim(),
            ResidenceId = request.ResidenceId
        };
        user.PasswordHash = _hasher.HashPassword(user, request.Password);
        db.Users.Add(user);
        await db.SaveChangesAsync();
        await db.Entry(user).Reference(u => u.Residence).LoadAsync();

        return Ok(DtoMapper.ToAuth(user, tokens.CreateToken(user)));
    }

    [HttpPost("login")]
    public async Task<ActionResult<AuthResponse>> Login(LoginRequest request)
    {
        var user = await db.Users
            .Include(u => u.Residence)
            .FirstOrDefaultAsync(u => u.Email == request.Email.Trim().ToLower());

        if (user is null)
        {
            return Unauthorized("Invalid email or password.");
        }

        var result = _hasher.VerifyHashedPassword(user, user.PasswordHash, request.Password);
        if (result == PasswordVerificationResult.Failed)
        {
            return Unauthorized("Invalid email or password.");
        }

        if (!user.IsActive)
        {
            return Unauthorized("This account has been deactivated.");
        }

        return Ok(DtoMapper.ToAuth(user, tokens.CreateToken(user)));
    }

    [Authorize]
    [HttpGet("me")]
    public async Task<ActionResult<AuthResponse>> Me()
    {
        var user = await db.Users.Include(u => u.Residence).FirstOrDefaultAsync(u => u.Id == currentUser.UserId);
        if (user is null)
        {
            return Unauthorized();
        }

        return Ok(DtoMapper.ToAuth(user, tokens.CreateToken(user)));
    }

    [Authorize]
    [HttpPut("push-token")]
    public async Task<IActionResult> UpdatePushToken(UpdatePushTokenRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Token))
        {
            return BadRequest("A push token is required.");
        }

        var user = await db.Users.FirstOrDefaultAsync(user => user.Id == currentUser.UserId);

        if (user is null)
        {
            return Unauthorized();
        }

        user.PushToken = request.Token.Trim();
        await db.SaveChangesAsync();

        return NoContent();
    }

    [HttpPost("forgot-password")]
    public async Task<IActionResult> ForgotPassword(ForgotPasswordRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Email))
        {
            return BadRequest("Email is required.");
        }

        var normalizedEmail = request.Email.Trim().ToLower();
        var user = await db.Users.FirstOrDefaultAsync(u => u.Email == normalizedEmail);

        // Always return the same message for security.
        if (user is null)
        {
            return Ok(new { message = "If an account exists for that email, a reset link has been sent." });
        }

        var rawToken = Convert.ToBase64String(Guid.NewGuid().ToByteArray()) +
                       "." +
                       Convert.ToBase64String(RandomNumberGenerator.GetBytes(32));

        var tokenHash = Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(rawToken)));

        var existingTokens = db.PasswordResetTokens.Where(t => t.UserId == user.Id && t.UsedAt == null && t.ExpiresAt > DateTime.UtcNow);

        db.PasswordResetTokens.RemoveRange(existingTokens);

        db.PasswordResetTokens.Add(new PasswordResetToken
        {
            Id = Guid.NewGuid(),
            UserId = user.Id,
            TokenHash = tokenHash,
            ExpiresAt = DateTime.UtcNow.AddMinutes(30)
        });

        await db.SaveChangesAsync();

        var baseUrl = configuration["App:BaseUrl"] ?? $"{Request.Scheme}://{Request.Host}";
        var encodedToken = WebEncoders.Base64UrlEncode(Encoding.UTF8.GetBytes(rawToken));
        var resetLink = $"{baseUrl}/reset-password?token={encodedToken}";

        await emailService.SendPasswordResetAsync(user.Email, resetLink);

        return Ok(new { message = "If an account exists for that email, a reset link has been sent." });
    }

    [HttpPost("reset-password")]
    public async Task<IActionResult> ResetPassword(ResetPasswordRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Token))
        {
            return BadRequest("A reset token is required.");
        }

        if (string.IsNullOrWhiteSpace(request.NewPassword) || request.NewPassword.Length < 8)
        {
            return BadRequest("Password must be at least 8 characters long.");
        }

        var tokenBytes = WebEncoders.Base64UrlDecode(request.Token);
        var rawToken = Encoding.UTF8.GetString(tokenBytes);
        var tokenHash = Convert.ToHexString(
            SHA256.HashData(Encoding.UTF8.GetBytes(rawToken)));

        var resetToken = await db.PasswordResetTokens
            .Include(t => t.User)
            .FirstOrDefaultAsync(t =>
                t.TokenHash == tokenHash &&
                t.UsedAt == null &&
                t.ExpiresAt > DateTime.UtcNow);

        if (resetToken is null || resetToken.User is null)
        {
            return BadRequest("This reset link is invalid or has expired.");
        }

        resetToken.User.PasswordHash = _hasher.HashPassword(resetToken.User, request.NewPassword);
        resetToken.UsedAt = DateTime.UtcNow;

        await db.SaveChangesAsync();

        return Ok(new { message = "Your password has been reset." });
    }
}
