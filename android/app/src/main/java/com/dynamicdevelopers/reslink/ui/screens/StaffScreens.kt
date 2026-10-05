package com.dynamicdevelopers.reslink.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynamicdevelopers.reslink.data.EmergencyAlertDto
import com.dynamicdevelopers.reslink.data.NoiseComplaintDto
import com.dynamicdevelopers.reslink.data.ResLinkApi
import com.dynamicdevelopers.reslink.data.VisitorDto
import com.dynamicdevelopers.reslink.data.UpdateActiveRequest
import com.dynamicdevelopers.reslink.data.UpdateStatusRequest
import com.dynamicdevelopers.reslink.data.UserSummaryDto
import com.dynamicdevelopers.reslink.ui.auth.userMessage
import com.dynamicdevelopers.reslink.ui.common.Avatar
import com.dynamicdevelopers.reslink.ui.common.EmptyState
import com.dynamicdevelopers.reslink.ui.common.ErrorText
import com.dynamicdevelopers.reslink.ui.common.IconTile
import com.dynamicdevelopers.reslink.ui.common.ResCard
import com.dynamicdevelopers.reslink.ui.common.ScreenHeader
import com.dynamicdevelopers.reslink.ui.common.ScreenPadding
import com.dynamicdevelopers.reslink.ui.common.SectionTitle
import com.dynamicdevelopers.reslink.ui.common.StatusChip
import com.dynamicdevelopers.reslink.ui.theme.CardWhite
import com.dynamicdevelopers.reslink.ui.theme.Danger
import com.dynamicdevelopers.reslink.ui.theme.Forest
import com.dynamicdevelopers.reslink.ui.theme.Ink
import com.dynamicdevelopers.reslink.ui.theme.InkSoft
import com.dynamicdevelopers.reslink.ui.theme.Line
import com.dynamicdevelopers.reslink.ui.theme.Muted
import com.dynamicdevelopers.reslink.ui.theme.Tint
import com.dynamicdevelopers.reslink.ui.theme.Tints
import kotlinx.coroutines.launch

@Composable
fun UsersScreen(api: ResLinkApi) {
    var users by remember { mutableStateOf(listOf<UserSummaryDto>()) }
    var resetFor by remember { mutableStateOf<Pair<String, String>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    fun reload() = scope.launch { users = runCatching { api.users() }.getOrDefault(emptyList()) }
    LaunchedEffect(Unit) { reload() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Residents & staff", "Activate accounts and send password reset links")
            ErrorText(error)
        }
        items(users) { user ->
            ResCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(user.fullName, 40.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(user.fullName, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text(user.email + (user.room?.let { " · Room $it" } ?: ""), color = Muted, fontSize = 13.sp)
                    }
                    StatusChip(if (user.isActive) user.role else "Inactive")
                }
                Row(Modifier.padding(top = 6.dp)) {
                    TextButton(onClick = {
                        scope.launch {
                            try {
                                resetFor = user.fullName to api.resetLink(user.id).link
                            } catch (ex: Exception) {
                                error = userMessage(ex)
                            }
                        }
                    }) { Text("Reset link", color = Forest, fontWeight = FontWeight.SemiBold) }
                    TextButton(onClick = {
                        scope.launch { api.setActive(user.id, UpdateActiveRequest(!user.isActive)); reload() }
                    }) { Text(if (user.isActive) "Deactivate" else "Activate", color = if (user.isActive) Danger else Forest, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }

    resetFor?.let { (name, link) ->
        AlertDialog(
            onDismissRequest = { resetFor = null },
            containerColor = CardWhite,
            title = { Text("Reset link for $name", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Send this one-time link to the resident, for example on WhatsApp. It works once and expires in 30 minutes.", color = InkSoft)
                    Text(link, color = Forest, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val share = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, "Reset your ResLink password: $link")
                    context.startActivity(Intent.createChooser(share, "Send reset link"))
                }) { Text("Share", color = Forest, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = { TextButton(onClick = { resetFor = null }) { Text("Done", color = Muted) } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SecurityQueuesScreen(api: ResLinkApi) {
    var alerts by remember { mutableStateOf(listOf<EmergencyAlertDto>()) }
    var complaints by remember { mutableStateOf(listOf<NoiseComplaintDto>()) }
    var visitors by remember { mutableStateOf(listOf<VisitorDto>()) }
    val scope = rememberCoroutineScope()
    fun reload() = scope.launch {
        alerts = runCatching { api.emergencies() }.getOrDefault(emptyList())
        complaints = runCatching { api.noise() }.getOrDefault(emptyList())
        visitors = runCatching { api.visitors() }.getOrDefault(emptyList())
    }
    LaunchedEffect(Unit) { reload() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Security desk", "Panic alerts first, then noise complaints and visitors") }
        item { SectionTitle("Panic alerts") }
        if (alerts.isEmpty()) item { EmptyState("No panic alerts", "Alerts from residents appear here straight away.", Icons.Outlined.Sos, Tints.Rose) }
        items(alerts) { alert ->
            PanicAlertCard(alert, onStatus = { status -> scope.launch { api.updateEmergency(alert.id, UpdateStatusRequest(status)); reload() } })
        }
        item { SectionTitle("Noise complaints", Modifier.padding(top = 8.dp)) }
        if (complaints.isEmpty()) item { EmptyState("No noise complaints", "It's quiet tonight.", Icons.AutoMirrored.Outlined.VolumeUp, Tints.Sky) }
        items(complaints) { item ->
            ResCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(item.location, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(end = 8.dp))
                    StatusChip(item.status)
                }
                Text("${item.reporterName} · ${if (item.isAnonymous) "Anonymous" else "Named"}", color = Muted)
                Text(item.description, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Open", "Resolved", "Withdrawn").forEach { status ->
                        FilterChip(
                            selected = item.status == status,
                            onClick = { scope.launch { api.updateNoise(item.id, UpdateStatusRequest(status)); reload() } },
                            label = { Text(if (status == "Withdrawn") "Withdraw" else status, maxLines = 1) }
                        )
                    }
                }
            }
        }
        item { SectionTitle("Visitors", Modifier.padding(top = 8.dp)) }
        if (visitors.isEmpty()) item { EmptyState("No visitors", "Signed-in visitors show up here.", Icons.Outlined.Badge, Tints.Slate) }
        items(visitors) { visitor ->
            ResCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(visitor.visitorName, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(end = 8.dp))
                    StatusChip(visitor.status)
                }
                Text("Host ${visitor.hostName} · ${visitor.hostRoom}", color = Muted)
                if (visitor.status == "OnSite") {
                    TextButton(onClick = { scope.launch { api.updateVisitor(visitor.id, UpdateStatusRequest("Departed")); reload() } }) {
                        Text("Check out", color = Forest)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PanicAlertCard(alert: EmergencyAlertDto, onStatus: (String) -> Unit) {
    val context = LocalContext.current
    val active = alert.status == "Open"
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) Tints.Rose.bg else CardWhite)
            .border(1.dp, if (active) Tints.Rose.fg.copy(alpha = 0.35f) else Line, RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Outlined.Sos, Tints.Rose, 40.dp)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(alert.reporterName, color = Ink, fontWeight = FontWeight.Bold)
                Text(alert.location, color = InkSoft, fontSize = 13.sp)
            }
            StatusChip(alert.status)
        }
        Text(alert.message, color = InkSoft, modifier = Modifier.padding(top = 10.dp))
        if (alert.latitude != null && alert.longitude != null) {
            TextButton(
                onClick = {
                    val mapsUri = Uri.parse("https://www.google.com/maps?q=${alert.latitude},${alert.longitude}")
                    context.startActivity(Intent(Intent.ACTION_VIEW, mapsUri))
                },
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(Icons.Outlined.Place, null, tint = Forest, modifier = Modifier.size(18.dp))
                Text("Open live location in Maps", color = Forest, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 6.dp))
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("Open", "Acknowledged", "Resolved", "Withdrawn").forEach { status ->
                FilterChip(
                    selected = alert.status == status,
                    onClick = { onStatus(status) },
                    label = { Text(if (status == "Withdrawn") "False alarm" else status) }
                )
            }
        }
    }
}

private data class MoreLink(val route: String, val label: String, val hint: String, val icon: ImageVector, val tint: Tint)

@Composable
fun MoreMenuScreen(role: String, onOpen: (String) -> Unit) {
    val profile = MoreLink("profile", "Profile", "Your account and sign out", Icons.Outlined.Person, Tints.Slate)
    val notifications = MoreLink("notifications", "Notifications", "Alerts and ticket updates", Icons.Outlined.Notifications, Tints.Leaf)
    val links = when (role) {
        "Student" -> listOf(
            MoreLink("panic", "Emergency", "One tap alerts security", Icons.Outlined.Sos, Tints.Rose),
            MoreLink("maintenance", "Maintenance", "Report and track repairs", Icons.Outlined.Build, Tints.Gold),
            MoreLink("noise", "Noise complaint", "Anonymous report to security", Icons.AutoMirrored.Outlined.VolumeUp, Tints.Sky),
            MoreLink("rewards", "Rewards", "Swap points for vouchers", Icons.Outlined.CardGiftcard, Tints.Gold),
            MoreLink("groups", "Study groups", "Find partners by module", Icons.Outlined.Groups, Tints.Sky),
            notifications, profile
        )
        "Admin" -> listOf(
            MoreLink("feed", "Community feed", "Post announcements and moderate", Icons.Outlined.Forum, Tints.Plum),
            MoreLink("market", "Marketplace", "Oversee listings", Icons.Outlined.Storefront, Tints.Plum),
            MoreLink("users", "Users", "Activate accounts and reset passwords", Icons.Outlined.ManageAccounts, Tints.Leaf),
            notifications, profile
        )
        else -> listOf(notifications, profile)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(ScreenPadding)) {
        ScreenHeader("More", "Everything else for $role")
        ResCard {
            links.forEachIndexed { i, link ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable { onOpen(link.route) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconTile(link.icon, link.tint, 42.dp)
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(link.label, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text(link.hint, color = Muted, fontSize = 13.sp)
                    }
                    Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = Muted)
                }
                if (i < links.lastIndex) HorizontalDivider(color = Line, modifier = Modifier.padding(start = 56.dp))
            }
        }
    }
}
