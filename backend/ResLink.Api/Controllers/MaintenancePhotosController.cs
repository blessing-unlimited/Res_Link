using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ResLink.Api.Auth;
using ResLink.Api.Domain;
using ResLink.Api.Services;

namespace ResLink.Api.Controllers
{
    [ApiController]
    [Authorize(Roles = Roles.Student)]
    [Route("api/maintenance/photos")]
    public class MaintenancePhotosController(
    CloudinaryStorageService storage) : ControllerBase
    {
        [HttpPost]
        [RequestSizeLimit(5 * 1024 * 1024)]
        public async Task<IActionResult> Upload(IFormFile file)
        {
            if (file is null || file.Length == 0)
            {
                return BadRequest("An image is required.");
            }

            if (file.Length > 5 * 1024 * 1024)
            {
                return BadRequest("The image must be smaller than 5 MB.");
            }

            var contentType = file.ContentType.ToLowerInvariant();

            if (contentType is not ("image/jpeg" or "image/png"))
            {
                return BadRequest("Only JPEG and PNG images are allowed.");
            }

            var extension = contentType == "image/png"
                ? ".png"
                : ".jpg";

            var fileName = $"{Guid.NewGuid():N}{extension}";

            await using var stream = file.OpenReadStream();

            var url = await storage.UploadAsync(stream, fileName);

            return Ok(new
            {
                url
            });
        }
    }
}
