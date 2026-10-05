using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using ResLink.Api.Auth;
using ResLink.Api.Contracts;
using ResLink.Api.Data;
using ResLink.Api.Domain;
using ResLink.Api.Services;

namespace ResLink.Api.Controllers;

[ApiController]
[Authorize]
public class CommunityController(
    AppDbContext db,
    CurrentUser current,
    PointsService points) : ControllerBase
{
    // Study groups can be joined and left, and leaving takes back the joining points; a redeemed reward can only be voided by an admin (Microsoft, 2025a).
    [HttpGet("api/study-groups")]
    [Authorize(Roles = $"{Roles.Student},{Roles.Admin}")]
    public async Task<ActionResult<IReadOnlyList<StudyGroupDto>>> Groups()
    {
        var groups = await db.StudyGroups.Include(g => g.Members).OrderByDescending(g => g.CreatedAt).ToListAsync();
        return Ok(groups.Select(g => new StudyGroupDto(
            g.Id, g.Name, g.Topic, g.CourseCode, g.Description, g.Schedule, g.Location,
            g.Members.Count, g.MaxMembers,
            g.Members.Any(m => m.UserId == current.UserId), g.CreatedAt)).ToList());
    }

    [HttpPost("api/study-groups")]
    [Authorize(Roles = $"{Roles.Student},{Roles.Admin}")]
    public async Task<ActionResult<StudyGroupDto>> CreateGroup(CreateStudyGroupRequest request)
    {
        var user = await db.Users.FirstAsync(u => u.Id == current.UserId);
        var group = new StudyGroup
        {
            Id = Guid.NewGuid(),
            ResidenceId = user.ResidenceId ?? Guid.Empty,
            CreatedById = user.Id,
            Name = request.Name.Trim(),
            Topic = request.Topic.Trim(),
            CourseCode = request.CourseCode?.Trim() ?? "",
            Description = request.Description.Trim(),
            Schedule = request.Schedule?.Trim() ?? "",
            Location = request.Location?.Trim() ?? "",
            MaxMembers = request.MaxMembers ?? 12
        };
        db.StudyGroups.Add(group);
        db.StudyGroupMembers.Add(new StudyGroupMember { Id = Guid.NewGuid(), StudyGroupId = group.Id, UserId = user.Id });
        await points.AwardAsync(user, PointValues.StudyGroup);
        return Ok(new StudyGroupDto(group.Id, group.Name, group.Topic, group.CourseCode, group.Description,
            group.Schedule, group.Location, 1, group.MaxMembers, true, group.CreatedAt));
    }

    [HttpPost("api/study-groups/{id:guid}/join")]
    [Authorize(Roles = Roles.Student)]
    public async Task<IActionResult> Join(Guid id)
    {
        var group = await db.StudyGroups.Include(g => g.Members).FirstOrDefaultAsync(g => g.Id == id);
        if (group is null)
        {
            return NotFound();
        }

        if (!group.Members.Any(m => m.UserId == current.UserId))
        {
            if (group.Members.Count >= group.MaxMembers)
            {
                return BadRequest("This study group is full.");
            }

            db.StudyGroupMembers.Add(new StudyGroupMember { Id = Guid.NewGuid(), StudyGroupId = id, UserId = current.UserId });
            var user = await db.Users.FirstAsync(u => u.Id == current.UserId);
            await points.AwardAsync(user, PointValues.StudyGroup);
        }

        return NoContent();
    }

    [HttpDelete("api/study-groups/{id:guid}/leave")]
    [Authorize(Roles = Roles.Student)]
    public async Task<IActionResult> Leave(Guid id)
    {
        var member = await db.StudyGroupMembers.FirstOrDefaultAsync(m => m.StudyGroupId == id && m.UserId == current.UserId);
        if (member is not null)
        {
            db.StudyGroupMembers.Remove(member);
            await points.DeductAsync(current.UserId, PointValues.StudyGroup);
        }

        return NoContent();
    }

    [HttpGet("api/rewards")]
    [Authorize(Roles = $"{Roles.Student},{Roles.Admin}")]
    public async Task<ActionResult<IReadOnlyList<RewardDto>>> Rewards()
    {
        var items = await db.Rewards.OrderBy(r => r.PointsCost)
            .Select(r => new RewardDto(r.Id, r.Name, r.Description, r.Category, r.PointsCost, r.Stock))
            .ToListAsync();
        return Ok(items);
    }

    [HttpGet("api/rewards/redemptions")]
    [Authorize(Roles = Roles.Student)]
    public async Task<ActionResult<IReadOnlyList<RedemptionDto>>> Redemptions()
    {
        var items = await db.Redemptions.Include(r => r.Reward)
            .Where(r => r.UserId == current.UserId)
            .OrderByDescending(r => r.CreatedAt)
            .Select(r => new RedemptionDto(
                r.Id,
                r.RewardId,
                r.Reward!.Name,
                r.Reward.PointsCost,
                r.VoucherCode,
                r.IsUsed,
                r.IsVoided,
                r.CreatedAt))
            .ToListAsync();

        return Ok(items);
    }

    [HttpPost("api/rewards/{id:guid}/redeem")]
    [Authorize(Roles = Roles.Student)]
    public async Task<IActionResult> Redeem(Guid id)
    {
        var reward = await db.Rewards.FirstOrDefaultAsync(r => r.Id == id);
        var user = await db.Users.FirstAsync(u => u.Id == current.UserId);

        if (reward is null)
        {
            return NotFound();
        }

        if (reward.Stock <= 0)
        {
            return BadRequest("This reward is out of stock.");
        }

        if (user.Points < reward.PointsCost)
        {
            return BadRequest("Not enough points to redeem this reward.");
        }

        var code = $"RSL-{Guid.NewGuid().ToString("N")[..8].ToUpperInvariant()}";

        user.Points -= reward.PointsCost;
        reward.Stock -= 1;

        var redemption = new Redemption
        {
            Id = Guid.NewGuid(),
            UserId = user.Id,
            RewardId = reward.Id,
            VoucherCode = code,
            IsUsed = false,
            IsVoided = false
        };

        db.Redemptions.Add(redemption);
        await db.SaveChangesAsync();

        return Ok(new
        {
            voucherCode = redemption.VoucherCode,
            rewardName = reward.Name,
            pointsCost = reward.PointsCost
        });
    }

    [HttpGet("api/rewards/redemptions/admin")]
    [Authorize(Roles = Roles.Admin)]
    public async Task<ActionResult<IReadOnlyList<RedemptionAdminDto>>> AdminRedemptions()
    {
        var items = await db.Redemptions
            .Include(r => r.User)
            .Include(r => r.Reward)
            .OrderByDescending(r => r.CreatedAt)
            .Select(r => new RedemptionAdminDto(
                r.Id,
                r.UserId,
                r.User!.FullName,
                r.RewardId,
                r.Reward!.Name,
                r.Reward.PointsCost,
                r.VoucherCode,
                r.IsUsed,
                r.IsVoided,
                r.CreatedAt))
            .ToListAsync();

        return Ok(items);
    }

    [HttpPost("api/rewards/redemptions/{id:guid}/mark-used")]
    [Authorize(Roles = Roles.Admin)]
    public async Task<IActionResult> MarkRedemptionUsed(Guid id)
    {
        var redemption = await db.Redemptions
            .FirstOrDefaultAsync(r => r.Id == id);

        if (redemption is null)
        {
            return NotFound();
        }

        if (redemption.IsUsed || redemption.IsVoided)
        {
            return BadRequest("This redemption is no longer active.");
        }

        redemption.IsUsed = true;
        redemption.UsedAt = DateTime.UtcNow;

        await db.SaveChangesAsync();
        return NoContent();
    }

    [HttpPost("api/rewards/redemptions/{id:guid}/void")]
    [Authorize(Roles = Roles.Admin)]
    public async Task<IActionResult> VoidRedemption(Guid id)
    {
        var redemption = await db.Redemptions
            .Include(r => r.Reward)
            .FirstOrDefaultAsync(r => r.Id == id);

        if (redemption is null)
        {
            return NotFound();
        }

        if (redemption.IsVoided)
        {
            return BadRequest("This redemption has already been voided.");
        }

        if (redemption.IsUsed)
        {
            return BadRequest("A used redemption cannot be voided automatically.");
        }

        var user = await db.Users.FirstAsync(u => u.Id == redemption.UserId);
        user.Points += redemption.Reward?.PointsCost ?? 0;

        if (redemption.Reward is not null)
        {
            redemption.Reward.Stock += 1;
        }

        redemption.IsVoided = true;
        redemption.VoidedAt = DateTime.UtcNow;

        await db.SaveChangesAsync();
        return NoContent();
    }

    [HttpPost("api/rewards")]
    [Authorize(Roles = Roles.Admin)]
    public async Task<ActionResult<RewardDto>> CreateReward(CreateRewardRequest request)
    {
        var reward = new Reward
        {
            Id = Guid.NewGuid(),
            Name = request.Name.Trim(),
            Description = request.Description.Trim(),
            Category = string.IsNullOrWhiteSpace(request.Category) ? "Food" : request.Category.Trim(),
            PointsCost = request.PointsCost,
            Stock = request.Stock ?? 20
        };
        db.Rewards.Add(reward);
        await db.SaveChangesAsync();
        return Ok(new RewardDto(reward.Id, reward.Name, reward.Description, reward.Category, reward.PointsCost, reward.Stock));
    }
}
