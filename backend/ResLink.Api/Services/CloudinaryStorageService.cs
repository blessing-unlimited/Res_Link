using CloudinaryDotNet;
using CloudinaryDotNet.Actions;

namespace ResLink.Api.Services
{
    // Uploads go through the Cloudinary .NET SDK into a maintenance folder (Cloudinary, 2026).
    public class CloudinaryStorageService
    {
        private readonly Cloudinary? _cloudinary;

        public CloudinaryStorageService(IConfiguration configuration)
        {
            var cloudName = configuration["Cloudinary:CloudName"];
            var apiKey = configuration["Cloudinary:ApiKey"];
            var apiSecret = configuration["Cloudinary:ApiSecret"];

            if (string.IsNullOrWhiteSpace(cloudName) || string.IsNullOrWhiteSpace(apiKey) || string.IsNullOrWhiteSpace(apiSecret))
            {
                return;
            }

            var account = new Account(cloudName, apiKey, apiSecret);
            _cloudinary = new Cloudinary(account);
        }

        public bool IsConfigured => _cloudinary is not null;

        public async Task<string> UploadAsync(Stream fileStream, string filename)
        {
            if (_cloudinary is null)
                throw new InvalidOperationException("Cloudinary is not configured.");

            var uploadParameters = new ImageUploadParams
            {
                File = new FileDescription(filename, fileStream),
                Folder = "reslink/maintenance"
            };

            var result = await _cloudinary.UploadAsync(uploadParameters);

            if (result.Error is not null)
                throw new InvalidOperationException(result.Error.Message);

            return result.SecureUrl.ToString();
        }
    }
}
