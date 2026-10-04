using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ResLink.Api.Migrations
{
    /// <inheritdoc />
    public partial class AddEmergencyLocationFields : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<double>(
                name: "Latitude",
                table: "EmergencyAlerts",
                type: "double precision",
                nullable: true);

            migrationBuilder.AddColumn<double>(
                name: "Longitude",
                table: "EmergencyAlerts",
                type: "double precision",
                nullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "Latitude",
                table: "EmergencyAlerts");

            migrationBuilder.DropColumn(
                name: "Longitude",
                table: "EmergencyAlerts");
        }
    }
}
