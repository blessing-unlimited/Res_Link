using FirebaseAdmin;
using FirebaseAdmin.Messaging;
using Google.Apis.Auth.OAuth2;

namespace ResLink.Api.Services;

// (Firebase,2026; Firebase,2026a;Firebase,2026b; Firebase,2026c;)
public class FirebasePushService
{
    private readonly FirebaseMessaging? _messaging;
    private readonly ILogger<FirebasePushService> _logger;

    public FirebasePushService(
        IConfiguration configuration,
        ILogger<FirebasePushService> logger)
    {
        _logger = logger;

        var serviceAccountJson =
            configuration["Firebase:ServiceAccountJson"];

        if (string.IsNullOrWhiteSpace(serviceAccountJson))
        {
            _logger.LogWarning(
                "Firebase is not configured; push notifications are disabled.");
            return;
        }

        var app = FirebaseApp.Create(new AppOptions
        {
            Credential = GoogleCredential.FromJson(serviceAccountJson)
        });

        _messaging = FirebaseMessaging.GetMessaging(app);
    }

    public async Task SendAsync(
        IEnumerable<string?> deviceTokens,
        string title,
        string body,
        string type)
    {
        if (_messaging is null)
        {
            return;
        }

        var tokens = deviceTokens
            .Where(token => !string.IsNullOrWhiteSpace(token))
            .Select(token => token!)
            .Distinct()
            .ToList();

        foreach (var batch in tokens.Chunk(500))
        {
            try
            {
                var response = await _messaging.SendEachForMulticastAsync(
                    new MulticastMessage
                    {
                        Tokens = batch.ToList(),
                        Notification = new FirebaseAdmin.Messaging.Notification
                        {
                            Title = title,
                            Body = body
                        },
                        Data = new Dictionary<string, string>
                        {
                            ["type"] = type
                        }
                    });

                foreach (var result in response.Responses.Where(
                             result => !result.IsSuccess))
                {
                    _logger.LogWarning(
                        "Firebase push delivery failed: {Error}",
                        result.Exception?.Message);
                }
            }
            catch (Exception exception)
            {
                _logger.LogError(
                    exception,
                    "Firebase push delivery failed.");
            }
        }
    }
}