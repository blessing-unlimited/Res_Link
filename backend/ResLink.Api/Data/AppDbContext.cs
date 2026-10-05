using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage.ValueConversion;
using ResLink.Api.Domain;

namespace ResLink.Api.Data;

public class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<Residence> Residences => Set<Residence>();
    public DbSet<AppUser> Users => Set<AppUser>();
    public DbSet<Post> Posts => Set<Post>();
    public DbSet<Comment> Comments => Set<Comment>();
    public DbSet<PollVote> PollVotes => Set<PollVote>();
    public DbSet<ResidenceEvent> Events => Set<ResidenceEvent>();
    public DbSet<EventRsvp> Rsvps => Set<EventRsvp>();
    public DbSet<MarketplaceItem> MarketplaceItems => Set<MarketplaceItem>();
    public DbSet<MaintenanceTicket> MaintenanceTickets => Set<MaintenanceTicket>();
    public DbSet<NoiseComplaint> NoiseComplaints => Set<NoiseComplaint>();
    public DbSet<EmergencyAlert> EmergencyAlerts => Set<EmergencyAlert>();
    public DbSet<StudyGroup> StudyGroups => Set<StudyGroup>();
    public DbSet<StudyGroupMember> StudyGroupMembers => Set<StudyGroupMember>();
    public DbSet<Reward> Rewards => Set<Reward>();
    public DbSet<Redemption> Redemptions => Set<Redemption>();
    public DbSet<AppNotification> Notifications => Set<AppNotification>();
    public DbSet<Visitor> Visitors => Set<Visitor>();
    public DbSet<PasswordResetToken> PasswordResetTokens => Set<PasswordResetToken>();
    public DbSet<MaintenancePhoto> MaintenancePhotos => Set<MaintenancePhoto>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<AppUser>()
            .HasIndex(u => u.Email)
            .IsUnique();

        modelBuilder.Entity<AppUser>()
            .HasOne(u => u.Residence)
            .WithMany()
            .HasForeignKey(u => u.ResidenceId)
            .OnDelete(DeleteBehavior.SetNull);

        modelBuilder.Entity<Post>()
            .HasMany(p => p.Comments)
            .WithOne(c => c.Post)
            .HasForeignKey(c => c.PostId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<Post>()
            .HasMany(p => p.Votes)
            .WithOne()
            .HasForeignKey(v => v.PostId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<PollVote>()
            .HasIndex(v => new { v.PostId, v.UserId })
            .IsUnique();

        modelBuilder.Entity<ResidenceEvent>()
            .HasMany(e => e.Rsvps)
            .WithOne(r => r.Event)
            .HasForeignKey(r => r.EventId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<EventRsvp>()
            .HasIndex(r => new { r.EventId, r.UserId })
            .IsUnique();

        modelBuilder.Entity<StudyGroup>()
            .HasMany(g => g.Members)
            .WithOne(m => m.StudyGroup)
            .HasForeignKey(m => m.StudyGroupId)
            .OnDelete(DeleteBehavior.Cascade);

        modelBuilder.Entity<StudyGroupMember>()
            .HasIndex(m => new { m.StudyGroupId, m.UserId })
            .IsUnique();

        modelBuilder.Entity<MarketplaceItem>()
            .Property(i => i.Price)
            .HasPrecision(10, 2);

        modelBuilder.Entity<PasswordResetToken>()
            .HasIndex(t => t.TokenHash)
            .IsUnique();

        modelBuilder.Entity<PasswordResetToken>()
            .HasOne(t => t.User)
            .WithMany()
            .HasForeignKey(t => t.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        // SQLite stores dates without a time zone, so read them back as UTC; otherwise the JSON has no "Z"
        // and browsers treat the time as local, showing "2 h ago" for something just created (Microsoft, 2025c).
        if (Database.IsSqlite())
        {
            var utc = new ValueConverter<DateTime, DateTime>(v => v, v => DateTime.SpecifyKind(v, DateTimeKind.Utc));
            var utcNullable = new ValueConverter<DateTime?, DateTime?>(v => v, v => v.HasValue ? DateTime.SpecifyKind(v.Value, DateTimeKind.Utc) : v);
            foreach (var property in modelBuilder.Model.GetEntityTypes().SelectMany(t => t.GetProperties()))
            {
                if (property.ClrType == typeof(DateTime))
                {
                    property.SetValueConverter(utc);
                }
                else if (property.ClrType == typeof(DateTime?))
                {
                    property.SetValueConverter(utcNullable);
                }
            }
        }
    }
}
