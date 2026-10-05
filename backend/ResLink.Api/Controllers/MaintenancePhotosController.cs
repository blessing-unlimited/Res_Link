using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ResLink.Api.Auth;
using ResLink.Api.Data;
using ResLink.Api.Domain;
using ResLink.Api.Services;

namespace ResLink.Api.Controllers
{
    [ApiController]
    [Authorize(Roles = Roles.Student)]
    [Route("api/maintenance/photos")]
    public class MaintenancePhotosController(
    CloudinaryStorageService storage,
    AppDbContext db,
    CurrentUser current) : ControllerBase
    {
        private const int MaxBytes = 5 * 1024 * 1024;

        [HttpPost]
        [RequestSizeLimit(MaxBytes + 64 * 1024)]
        public async Task<IActionResult> Upload(IFormFile file) //(Maze,2024)
        {
            if (file is null || file.Length == 0)
            {
                return BadRequest("An image is required.");
            }

            if (file.Length > MaxBytes)
            {
                return BadRequest("The image must be smaller than 5 MB.");
            }

            using var buffer = new MemoryStream();
            await file.CopyToAsync(buffer);
            var bytes = buffer.ToArray();

            // The declared type can be spoofed, so the first bytes must also match a JPEG or PNG (OWASP, 2023).
            var contentType = Sniff(bytes);
            if (contentType is null)
            {
                return BadRequest("Only JPEG and PNG images are allowed.");
            }

            if (storage.IsConfigured)
            {
                var extension = contentType == "image/png" ? ".png" : ".jpg";
                buffer.Position = 0;
                var url = await storage.UploadAsync(buffer, $"{Guid.NewGuid():N}{extension}");
                return Ok(new { url });
            }

            var photo = new MaintenancePhoto
            {
                Id = Guid.NewGuid(),
                UploadedById = current.UserId,
                ContentType = contentType,
                Data = bytes
            };
            db.MaintenancePhotos.Add(photo);
            await db.SaveChangesAsync();
            return Ok(new { url = $"/api/maintenance/photos/{photo.Id}" });
        }

        // Image tags cannot send a bearer token, so photos are fetched by their unguessable id.
        [HttpGet("{id:guid}")]
        [AllowAnonymous]
        public async Task<IActionResult> Get(Guid id)
        {
            var photo = await db.MaintenancePhotos.AsNoTracking().FirstOrDefaultAsync(p => p.Id == id);
            if (photo is null)
            {
                return NotFound();
            }

            Response.Headers.CacheControl = "public, max-age=604800, immutable";
            Response.Headers.XContentTypeOptions = "nosniff";
            return File(photo.Data, photo.ContentType);
        }

        private static string? Sniff(byte[] bytes)
        {
            if (bytes.Length >= 3 && bytes[0] == 0xFF && bytes[1] == 0xD8 && bytes[2] == 0xFF)
            {
                return "image/jpeg";
            }

            if (bytes.Length >= 8 && bytes[0] == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A)
            {
                return "image/png";
            }

            return null;
        }
    }
}
