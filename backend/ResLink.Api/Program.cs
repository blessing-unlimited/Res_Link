using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Scaffolding.Metadata;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using ResLink.Api.Auth;
using ResLink.Api.Data;
using ResLink.Api.Services;

var builder = WebApplication.CreateBuilder(args);

// Controllers and JWT bearer auth follow the ASP.NET Core 9 pipeline (Microsoft, 2025a; Microsoft, 2025b).
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "ResLink API",
        Version = "v1",
        Description = "Student residence engagement API for Dynamic Developers."
    });
    options.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Description = "JWT Authorization header using the Bearer scheme.",
        Name = "Authorization",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT"
    });
    options.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference { Type = ReferenceType.SecurityScheme, Id = "Bearer" }
            },
            Array.Empty<string>()
        }
    });
});

// Local SQLite for development; hosted runs use a writable folder (Microsoft, 2025c; SQLite Consortium, 2025).
var hosted = !string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("RENDER"))
    || !string.IsNullOrWhiteSpace(Environment.GetEnvironmentVariable("PORT"));
var dataDir = Environment.GetEnvironmentVariable("RESLINK_DATA_DIR");
if (string.IsNullOrWhiteSpace(dataDir))
{
    dataDir = Directory.Exists("/home/data") ? "/home/data" : hosted ? "/tmp/reslink" : ".";
}
Directory.CreateDirectory(dataDir);
var sqlitePath = hosted
    ? $"Data Source={Path.Combine(dataDir, "reslink.db")}"
    : builder.Configuration.GetConnectionString("Default") ?? $"Data Source={Path.Combine(dataDir, "reslink.db")}";

var port = Environment.GetEnvironmentVariable("PORT");
if (!string.IsNullOrWhiteSpace(port))
{
    builder.WebHost.UseUrls($"http://0.0.0.0:{port}");
}

var databaseUrl = Environment.GetEnvironmentVariable("DATABASE_URL");

builder.Services.AddDbContext<AppDbContext>(options =>
{
    if (string.IsNullOrWhiteSpace(databaseUrl))
    {
        options.UseSqlite(sqlitePath);
        return;
    }

    var databaseUri = new Uri(databaseUrl);
    var userInfo = databaseUri.UserInfo;
    var separatorIndex = userInfo.IndexOf(':');

    if (separatorIndex < 0)
    {
        throw new InvalidOperationException(
            "DATABASE_URL must include a username and password.");
    }

    var username = Uri.UnescapeDataString(userInfo[..separatorIndex]);
    var password = Uri.UnescapeDataString(userInfo[(separatorIndex + 1)..]);
    var databaseName = Uri.UnescapeDataString(
        databaseUri.AbsolutePath.Trim('/'));
    var databasePort = databaseUri.Port > 0 ? databaseUri.Port : 5432;

    var connectionString = new Npgsql.NpgsqlConnectionStringBuilder
    {
        Host = databaseUri.Host,
        Port = databasePort,
        Database = databaseName,
        Username = username,
        Password = password,
        SslMode = Npgsql.SslMode.Require,
        TrustServerCertificate = true
    }.ConnectionString;

    options.UseNpgsql(connectionString);
});

builder.Services.AddHttpContextAccessor();
builder.Services.AddScoped<JwtTokenService>();
builder.Services.AddScoped<CurrentUser>();
builder.Services.AddScoped<NotificationService>();
builder.Services.AddScoped<PointsService>();
builder.Services.AddScoped<EmailService>();

// Issuer, audience and signing-key checks match Microsoft's JWT bearer guidance (Microsoft, 2025b; OWASP, 2023).
var jwt = builder.Configuration.GetSection("Jwt");
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateLifetime = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = jwt["Issuer"],
            ValidAudience = jwt["Audience"],
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwt["Key"]!)),
            RoleClaimType = System.Security.Claims.ClaimTypes.Role
        };
    });
builder.Services.AddAuthorization();

// CORS is open for the prototype so the browser UI and emulator can share one API (Microsoft, 2025a).

builder.Services.AddCors(options =>
{
    options.AddPolicy("Client", policy =>
        policy.AllowAnyHeader().AllowAnyMethod().AllowAnyOrigin());
});

builder.Services.AddSingleton<CloudinaryStorageService>();
builder.Services.AddSingleton<FirebasePushService>();

var app = builder.Build();

using (var scope = app.Services.CreateScope())
{
    var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();

    var connection = db.Database.GetDbConnection();
    app.Logger.LogInformation(
        "Active database: provider={Provider}, host/dataSource={DataSource}, database={Database}",
        db.Database.ProviderName,
        connection.DataSource,
        connection.Database);
    if (string.IsNullOrWhiteSpace(databaseUrl))
    {
        await db.Database.EnsureCreatedAsync();
    }
    else
    {
        await db.Database.MigrateAsync();
    }
    await DbSeeder.SeedAsync(db);
}

// OpenAPI/Swagger documents the REST surface for marking (Microsoft, 2025d; OpenAPI Initiative, 2024).
app.UseSwagger();
app.UseSwaggerUI(options =>
{
    options.SwaggerEndpoint("/swagger/v1/swagger.json", "ResLink API v1");
    options.RoutePrefix = "swagger";
});

app.UseDefaultFiles();
// Revalidate the browser files on each visit so an old saved page is not kept (MDN, 2025).
app.UseStaticFiles(new StaticFileOptions
{
    OnPrepareResponse = context =>
    {
        var file = context.File.Name;
        if (file is "index.html" or "app.js" or "app.css")
        {
            context.Context.Response.Headers.CacheControl = "no-cache, must-revalidate";
        }
    }
});
app.UseCors("Client");
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

app.Run();

public partial class Program;
