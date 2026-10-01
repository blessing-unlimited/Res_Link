using CloudinaryDotNet;
using CloudinaryDotNet.Actions;

namespace ResLink.Api.Services
{
    public class CloudinaryStorageService
    {
        private readonly Cloudinary _cloudinary;

        public CloudinaryStorageService(IConfiguration configuration)
        {
            var cloudName = configuration["Cloudinary:CloudName"] ?? throw new InvalidOperationException("Cloudinary cloud name is missing.");

            var apiKey = configuration["Cloudinary:ApiKey"] ?? throw new InvalidOperationException("Cloudinary API key is missing");

            var apiSecret = configuration["Cloudinary:ApiSecret"] ?? throw new InvalidOperationException("Cloudinary API secret is missing");

            var account = new Account(cloudName, apiKey, apiSecret);
            _cloudinary = new Cloudinary(account);
        }

        public async Task<string> UploadAsync(Stream fileStream, string filename)
        {
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
