using System.Net;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Mvc.Testing;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using ResLink.Api.Contracts;
using ResLink.Api.Data;

namespace ResLink.Api.Tests;

[TestClass]
public class AuthRoleTests
{
    private WebApplicationFactory<Program> _factory = null!;
    private string _dbPath = null!;

    [TestInitialize]
    public void Setup()
    {
        _dbPath = Path.Combine(Path.GetTempPath(), $"reslink-tests-{Guid.NewGuid():N}.db");
        _factory = new WebApplicationFactory<Program>().WithWebHostBuilder(builder =>
        {
            builder.UseSetting("ConnectionStrings:Default", $"Data Source={_dbPath}");
            builder.UseSetting("Jwt:Key", "ResLink-DynamicDevelopers-Prototype-Jwt-Key-2026!");
            builder.UseSetting("Jwt:Issuer", "ResLink.Api");
            builder.UseSetting("Jwt:Audience", "ResLink.Android");
            builder.UseSetting("Staff:AccessCode", "RESLINK-STAFF-2026");
        });
    }

    [TestCleanup]
    public void Cleanup()
    {
        _factory.Dispose();
        foreach (var suffix in new[] { "", "-shm", "-wal" })
        {
            var path = _dbPath + suffix;
            try
            {
                if (File.Exists(path))
                {
                    File.Delete(path);
                }
            }
            catch (IOException)
            {
                // SQLite can keep a short lock after dispose; temp files are disposable.
            }
        }
    }

    [TestMethod]
    public async Task Student_Can_Login_And_Is_Blocked_From_Admin_Analytics()
    {
        var client = _factory.CreateClient();
        var auth = await Login(client, "student@reslink.app", "Student123!");
        Assert.AreEqual("Student", auth.Role);

        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", auth.Token);
        var analytics = await client.GetAsync("/api/admin/analytics");
        Assert.AreEqual(HttpStatusCode.Forbidden, analytics.StatusCode);
    }

    [TestMethod]
    public async Task Staff_Register_Fails_Without_Access_Code()
    {
        var client = _factory.CreateClient();
        var residences = await client.GetFromJsonAsync<List<ResidenceDto>>("/api/auth/residences");
        Assert.IsNotNull(residences);
        Assert.IsTrue(residences.Count > 0);

        var response = await client.PostAsJsonAsync("/api/auth/register/staff", new StaffRegisterRequest(
            "New Guard",
            "guard@reslink.app",
            "Guard123!",
            "SEC-099",
            "Security",
            "WRONG-CODE",
            residences[0].Id));

        Assert.AreEqual(HttpStatusCode.Unauthorized, response.StatusCode);
    }

    [TestMethod]
    public async Task Security_Can_Read_Emergency_Queue()
    {
        var client = _factory.CreateClient();
        var auth = await Login(client, "security@reslink.app", "Security123!");
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", auth.Token);

        var response = await client.GetAsync("/api/emergencies");
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
        var alerts = await response.Content.ReadFromJsonAsync<List<EmergencyAlertDto>>();
        Assert.IsNotNull(alerts);
        Assert.IsTrue(alerts.Count >= 1);
    }

    [TestMethod]
    public async Task Student_Can_Register_And_Receive_Jwt()
    {
        var client = _factory.CreateClient();
        var residences = await client.GetFromJsonAsync<List<ResidenceDto>>("/api/auth/residences");
        var response = await client.PostAsJsonAsync("/api/auth/register/student", new StudentRegisterRequest(
            "New Student",
            "new.student@reslink.app",
            "Student123!",
            "STU999",
            residences![0].Id,
            "C-101"));

        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
        var auth = await response.Content.ReadFromJsonAsync<AuthResponse>();
        Assert.IsNotNull(auth);
        Assert.AreEqual("Student", auth.Role);
        Assert.IsFalse(string.IsNullOrWhiteSpace(auth.Token));
    }

    [TestMethod]
    public async Task Forgot_Password_Succeeds_Without_Email_Configured()
    {
        var client = _factory.CreateClient();
        var response = await client.PostAsJsonAsync("/api/auth/forgot-password", new ForgotPasswordRequest("student@reslink.app"));
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
    }

    [TestMethod]
    public async Task Admin_Reset_Link_Sets_A_New_Password_Once()
    {
        var client = _factory.CreateClient();
        var admin = await Login(client, "admin@reslink.app", "Admin123!");
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", admin.Token);

        var users = await client.GetFromJsonAsync<List<UserSummaryDto>>("/api/admin/users");
        var student = users!.First(u => u.Email == "student@reslink.app");
        var linkResponse = await client.PostAsync($"/api/admin/users/{student.Id}/reset-link", null);
        Assert.AreEqual(HttpStatusCode.OK, linkResponse.StatusCode);
        var link = await linkResponse.Content.ReadFromJsonAsync<ResetLinkDto>();
        var token = Uri.UnescapeDataString(new Uri(link!.Link).Query.Split("token=")[1]);

        client.DefaultRequestHeaders.Authorization = null;
        var reset = await client.PostAsJsonAsync("/api/auth/reset-password", new ResetPasswordRequest(token, "NewPass123!"));
        Assert.AreEqual(HttpStatusCode.OK, reset.StatusCode);
        await Login(client, "student@reslink.app", "NewPass123!");

        var reused = await client.PostAsJsonAsync("/api/auth/reset-password", new ResetPasswordRequest(token, "Another123!"));
        Assert.AreEqual(HttpStatusCode.BadRequest, reused.StatusCode);

        var malformed = await client.PostAsJsonAsync("/api/auth/reset-password", new ResetPasswordRequest("not-a-token!", "Another123!"));
        Assert.AreEqual(HttpStatusCode.BadRequest, malformed.StatusCode);
    }

    [TestMethod]
    public async Task Reset_Password_Page_Is_Served()
    {
        var client = _factory.CreateClient();
        var response = await client.GetAsync("/reset-password?token=abc");
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode);
        StringAssert.Contains(await response.Content.ReadAsStringAsync(), "<div id=\"app\">");
    }

    [TestMethod]
    public async Task Cancelling_An_Rsvp_Takes_Back_Its_Points()
    {
        var client = await StudentClient();
        var start = await Points(client);
        var events = await client.GetFromJsonAsync<List<EventDto>>("/api/events");
        var ev = events!.First(e => !e.HasRsvp);

        await client.PostAsync($"/api/events/{ev.Id}/rsvp", null);
        Assert.AreEqual(start + 10, await Points(client));
        await client.DeleteAsync($"/api/events/{ev.Id}/rsvp");
        Assert.AreEqual(start, await Points(client));

        await client.PostAsync($"/api/events/{ev.Id}/rsvp", null);
        await client.DeleteAsync($"/api/events/{ev.Id}/rsvp");
        await client.DeleteAsync($"/api/events/{ev.Id}/rsvp");
        Assert.AreEqual(start, await Points(client));
    }

    [TestMethod]
    public async Task Leaving_A_Study_Group_Takes_Back_Its_Points()
    {
        var client = await StudentClient();
        var start = await Points(client);
        var groups = await client.GetFromJsonAsync<List<StudyGroupDto>>("/api/study-groups");
        var group = groups!.First(g => !g.IsMember);

        await client.PostAsync($"/api/study-groups/{group.Id}/join", null);
        Assert.AreEqual(start + 5, await Points(client));
        await client.DeleteAsync($"/api/study-groups/{group.Id}/leave");
        Assert.AreEqual(start, await Points(client));
    }

    [TestMethod]
    public async Task Deleting_A_Post_Takes_Back_Its_Points()
    {
        var client = await StudentClient();
        var start = await Points(client);
        var response = await client.PostAsJsonAsync("/api/posts", new CreatePostRequest("Points check", "Temporary post", null, false, null));
        var post = await response.Content.ReadFromJsonAsync<PostDto>();
        Assert.AreEqual(start + 5, await Points(client));

        await client.DeleteAsync($"/api/posts/{post!.Id}");
        Assert.AreEqual(start, await Points(client));
    }

    [TestMethod]
    public async Task Cancelling_A_Ticket_Takes_Back_Its_Points()
    {
        var client = await StudentClient();
        var start = await Points(client);
        var response = await client.PostAsJsonAsync("/api/maintenance",
            new CreateMaintenanceRequest("Broken window latch", "Will not close", "Room B-204", null, null, null));
        var ticket = await response.Content.ReadFromJsonAsync<MaintenanceTicketDto>();
        Assert.AreEqual(start + 8, await Points(client));

        await client.PutAsJsonAsync($"/api/maintenance/{ticket!.Id}/status", new UpdateTicketStatusRequest("Cancelled", "Cancelled by student"));
        Assert.AreEqual(start, await Points(client));
    }

    [TestMethod]
    public async Task Withdrawing_A_Noise_Complaint_Takes_Back_Its_Points_Once()
    {
        var client = await StudentClient();
        var start = await Points(client);
        await client.PostAsJsonAsync("/api/noise", new CreateNoiseRequest("Block C corridor", "Loud music", false));
        Assert.AreEqual(start + 8, await Points(client));

        var mine = await client.GetFromJsonAsync<List<NoiseComplaintDto>>("/api/noise/mine");
        var complaint = mine!.First(n => n.Status == "Open");
        await client.PutAsync($"/api/noise/{complaint.Id}/withdraw", null);
        await client.PutAsync($"/api/noise/{complaint.Id}/withdraw", null);
        Assert.AreEqual(start, await Points(client));
    }

    [TestMethod]
    public async Task Maintenance_Photo_Uploads_Without_Cloudinary_And_Can_Be_Viewed()
    {
        var client = await StudentClient();
        byte[] png = [0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D];

        using var form = new MultipartFormDataContent();
        var file = new ByteArrayContent(png);
        file.Headers.ContentType = new MediaTypeHeaderValue("image/png");
        form.Add(file, "file", "tap.png");
        var upload = await client.PostAsync("/api/maintenance/photos", form);
        Assert.AreEqual(HttpStatusCode.OK, upload.StatusCode, await upload.Content.ReadAsStringAsync());
        var url = (await upload.Content.ReadFromJsonAsync<Dictionary<string, string>>())!["url"];
        StringAssert.StartsWith(url, "/api/maintenance/photos/");

        var anonymous = _factory.CreateClient();
        var photo = await anonymous.GetAsync(url);
        Assert.AreEqual(HttpStatusCode.OK, photo.StatusCode);
        Assert.AreEqual("image/png", photo.Content.Headers.ContentType?.MediaType);

        var ticket = await client.PostAsJsonAsync("/api/maintenance",
            new CreateMaintenanceRequest("Dripping tap", "Bathroom tap drips", "Room B-204", url, null, null));
        Assert.AreEqual(HttpStatusCode.OK, ticket.StatusCode);
        Assert.AreEqual(url, (await ticket.Content.ReadFromJsonAsync<MaintenanceTicketDto>())!.PhotoUrl);
    }

    [TestMethod]
    public async Task Maintenance_Photo_Rejects_Files_That_Are_Not_Images()
    {
        var client = await StudentClient();
        using var form = new MultipartFormDataContent();
        var file = new ByteArrayContent("<script>alert(1)</script>"u8.ToArray());
        file.Headers.ContentType = new MediaTypeHeaderValue("image/png");
        form.Add(file, "file", "fake.png");
        var upload = await client.PostAsync("/api/maintenance/photos", form);
        Assert.AreEqual(HttpStatusCode.BadRequest, upload.StatusCode);

        var ticket = await client.PostAsJsonAsync("/api/maintenance",
            new CreateMaintenanceRequest("Dripping tap", "Bathroom tap drips", "Room B-204", "javascript:alert(1)", null, null));
        Assert.AreEqual(HttpStatusCode.BadRequest, ticket.StatusCode);
    }

    [TestMethod]
    public async Task New_Items_Report_Their_Time_In_Utc()
    {
        var client = await StudentClient();
        var response = await client.PostAsJsonAsync("/api/posts", new CreatePostRequest("Time check", "Temporary post", null, false, null));
        using var json = System.Text.Json.JsonDocument.Parse(await response.Content.ReadAsStringAsync());
        var postId = json.RootElement.GetProperty("id").GetString();

        var listJson = await client.GetStringAsync("/api/posts");
        using var list = System.Text.Json.JsonDocument.Parse(listJson);
        var createdAt = list.RootElement.EnumerateArray()
            .First(p => p.GetProperty("id").GetString() == postId)
            .GetProperty("createdAt").GetString()!;
        StringAssert.EndsWith(createdAt, "Z");
        var age = DateTime.UtcNow - DateTime.Parse(createdAt, null, System.Globalization.DateTimeStyles.AdjustToUniversal);
        Assert.IsTrue(age.Duration() < TimeSpan.FromMinutes(5), $"createdAt was {createdAt}");
    }

    [TestMethod]
    public async Task A_Full_Study_Group_Cannot_Be_Joined()
    {
        var client = await StudentClient();
        var groups = await client.GetFromJsonAsync<List<StudyGroupDto>>("/api/study-groups");
        var group = groups!.First(g => !g.IsMember);
        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            var entity = await db.StudyGroups.FindAsync(group.Id);
            entity!.MaxMembers = group.MemberCount;
            await db.SaveChangesAsync();
        }
        var start = await Points(client);

        var join = await client.PostAsync($"/api/study-groups/{group.Id}/join", null);
        Assert.AreEqual(HttpStatusCode.BadRequest, join.StatusCode);
        Assert.AreEqual("This study group is full.", await join.Content.ReadAsStringAsync());
        Assert.AreEqual(start, await Points(client));
    }

    [TestMethod]
    public async Task A_Reward_With_No_Stock_Cannot_Be_Redeemed()
    {
        var client = await StudentClient();
        var rewards = await client.GetFromJsonAsync<List<RewardDto>>("/api/rewards");
        var reward = rewards!.OrderBy(r => r.PointsCost).First();
        using (var scope = _factory.Services.CreateScope())
        {
            var db = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            var entity = await db.Rewards.FindAsync(reward.Id);
            entity!.Stock = 0;
            var student = await db.Users.FirstAsync(u => u.Email == "student@reslink.app");
            student.Points = reward.PointsCost;
            await db.SaveChangesAsync();
        }

        var redeem = await client.PostAsync($"/api/rewards/{reward.Id}/redeem", null);
        Assert.AreEqual(HttpStatusCode.BadRequest, redeem.StatusCode);
        Assert.AreEqual("This reward is out of stock.", await redeem.Content.ReadAsStringAsync());
        Assert.AreEqual(reward.PointsCost, await Points(client));
    }

    [TestMethod]
    public async Task A_Panic_Alert_Without_A_Location_Uses_The_Room_And_Residence()
    {
        var student = await StudentClient();
        var panic = await student.PostAsJsonAsync("/api/emergencies", new CreateEmergencyRequest(null, null, null, null));
        Assert.AreEqual(HttpStatusCode.NoContent, panic.StatusCode);

        var security = _factory.CreateClient();
        var auth = await Login(security, "security@reslink.app", "Security123!");
        security.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", auth.Token);
        var alerts = await security.GetFromJsonAsync<List<EmergencyAlertDto>>("/api/emergencies");
        var latest = alerts!.OrderByDescending(a => a.CreatedAt).First();
        Assert.AreEqual("A-214, Campus Flow Residence", latest.Location);
    }

    private async Task<HttpClient> StudentClient()
    {
        var client = _factory.CreateClient();
        var auth = await Login(client, "student@reslink.app", "Student123!");
        client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", auth.Token);
        return client;
    }

    private static async Task<int> Points(HttpClient client) =>
        (await client.GetFromJsonAsync<UserSummaryDto>("/api/profile"))!.Points;

    private static async Task<AuthResponse> Login(HttpClient client, string email, string password)
    {
        var response = await client.PostAsJsonAsync("/api/auth/login", new LoginRequest(email, password));
        Assert.AreEqual(HttpStatusCode.OK, response.StatusCode, await response.Content.ReadAsStringAsync());
        var auth = await response.Content.ReadFromJsonAsync<AuthResponse>();
        Assert.IsNotNull(auth);
        return auth;
    }
}
