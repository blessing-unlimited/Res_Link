using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ResLink.Api.Migrations
{
    /// <inheritdoc />
    public partial class AddRewardRedemptionVoucherFields : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<bool>(
                name: "IsUsed",
                table: "Redemptions",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<bool>(
                name: "IsVoided",
                table: "Redemptions",
                type: "boolean",
                nullable: false,
                defaultValue: false);

            migrationBuilder.AddColumn<DateTime>(
                name: "UsedAt",
                table: "Redemptions",
                type: "timestamp with time zone",
                nullable: true);

            migrationBuilder.AddColumn<DateTime>(
                name: "VoidedAt",
                table: "Redemptions",
                type: "timestamp with time zone",
                nullable: true);

            migrationBuilder.AddColumn<string>(
                name: "VoucherCode",
                table: "Redemptions",
                type: "text",
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "IsUsed",
                table: "Redemptions");

            migrationBuilder.DropColumn(
                name: "IsVoided",
                table: "Redemptions");

            migrationBuilder.DropColumn(
                name: "UsedAt",
                table: "Redemptions");

            migrationBuilder.DropColumn(
                name: "VoidedAt",
                table: "Redemptions");

            migrationBuilder.DropColumn(
                name: "VoucherCode",
                table: "Redemptions");
        }
    }
}
