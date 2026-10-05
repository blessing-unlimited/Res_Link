package com.dynamicdevelopers.reslink.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Sos
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.core.content.ContextCompat
import com.dynamicdevelopers.reslink.data.CreateCommentRequest
import com.dynamicdevelopers.reslink.data.CreateEmergencyRequest
import com.dynamicdevelopers.reslink.data.CreateMaintenanceRequest
import com.dynamicdevelopers.reslink.data.CreateMarketplaceItemRequest
import com.dynamicdevelopers.reslink.data.CreateNoiseRequest
import com.dynamicdevelopers.reslink.data.CreatePostRequest
import com.dynamicdevelopers.reslink.data.CreateStudyGroupRequest
import com.dynamicdevelopers.reslink.data.DashboardDto
import com.dynamicdevelopers.reslink.data.EmergencyAlertDto
import com.dynamicdevelopers.reslink.data.EventDto
import com.dynamicdevelopers.reslink.data.MaintenanceTicketDto
import com.dynamicdevelopers.reslink.data.MarketplaceItemDto
import com.dynamicdevelopers.reslink.data.NotificationDto
import com.dynamicdevelopers.reslink.data.PostDto
import com.dynamicdevelopers.reslink.data.ResLinkApi
import com.dynamicdevelopers.reslink.data.RewardDto
import com.dynamicdevelopers.reslink.data.SessionStore
import com.dynamicdevelopers.reslink.data.StudyGroupDto
import com.dynamicdevelopers.reslink.data.VoteRequest
import com.dynamicdevelopers.reslink.ui.auth.userMessage
import com.dynamicdevelopers.reslink.ui.common.Avatar
import com.dynamicdevelopers.reslink.ui.common.EmptyState
import com.dynamicdevelopers.reslink.ui.common.ErrorText
import com.dynamicdevelopers.reslink.ui.common.HeroCard
import com.dynamicdevelopers.reslink.ui.common.HeroStat
import com.dynamicdevelopers.reslink.ui.common.HeroText
import com.dynamicdevelopers.reslink.ui.common.IconTile
import com.dynamicdevelopers.reslink.ui.common.ResButton
import com.dynamicdevelopers.reslink.ui.common.ResCard
import com.dynamicdevelopers.reslink.ui.common.ResField
import com.dynamicdevelopers.reslink.ui.common.ResOutlinedButton
import com.dynamicdevelopers.reslink.ui.common.ScreenHeader
import com.dynamicdevelopers.reslink.ui.common.ScreenPadding
import com.dynamicdevelopers.reslink.ui.common.SectionTitle
import com.dynamicdevelopers.reslink.ui.common.StatusChip
import com.dynamicdevelopers.reslink.ui.common.tintFor
import com.dynamicdevelopers.reslink.ui.theme.Danger
import com.dynamicdevelopers.reslink.ui.theme.Forest
import com.dynamicdevelopers.reslink.ui.theme.Ink
import com.dynamicdevelopers.reslink.ui.theme.InkSoft
import com.dynamicdevelopers.reslink.ui.theme.Muted
import com.dynamicdevelopers.reslink.ui.theme.PointsGold
import com.dynamicdevelopers.reslink.ui.theme.Tint
import com.dynamicdevelopers.reslink.ui.theme.Tints
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(api: ResLinkApi, session: SessionStore, role: String) {
    var dash by remember { mutableStateOf<DashboardDto?>(null) }
    var announcements by remember { mutableStateOf(listOf<PostDto>()) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        runCatching { api.dashboard() }
            .onSuccess { dash = it; session.updatePoints(it.points) }
            .onFailure { error = userMessage(it) }
        announcements = runCatching { api.posts() }.getOrDefault(emptyList()).filter { it.kind == "Announcement" }
    }
    val hour = remember { java.time.LocalTime.now().hour }
    val greeting = when (hour) { in 5..11 -> "Good morning"; in 12..17 -> "Good afternoon"; else -> "Good evening" }
    val isStudent = role == "Student"

    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HeroCard {
                HeroText(
                    session.residenceName(),
                    "$greeting, ${session.fullName().substringBefore(" ")}",
                    if (isStudent) "Here's what's happening at your residence today." else "Your $role tools are ready."
                )
                Row(Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isStudent) {
                        HeroStat("Reward points", "${dash?.points ?: session.points()}", Modifier.weight(1f), valueColor = PointsGold)
                        HeroStat("Room", session.room().ifBlank { "Not set" }, Modifier.weight(1f))
                    } else {
                        HeroStat("Role", role, Modifier.weight(1f))
                        dash?.analytics?.let { HeroStat("Residents", "${it.totalUsers}", Modifier.weight(1f)) }
                    }
                }
            }
            ErrorText(error)
        }
        if (announcements.isNotEmpty()) {
            item { SectionTitle("Announcements") }
        }
        items(announcements) { post ->
            ResCard {
                Row(verticalAlignment = Alignment.Top) {
                    IconTile(Icons.Outlined.Campaign, Tints.Plum, 40.dp)
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(post.title, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text(post.body, color = InkSoft, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                        Text(post.createdAt.take(10), color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
        if (dash?.highlights.orEmpty().isNotEmpty()) {
            item { SectionTitle("For you") }
        }
        items(dash?.highlights.orEmpty()) { tip ->
            ResCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Lightbulb, Tints.Gold, 36.dp)
                    Text(tip, color = InkSoft, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
                }
            }
        }
        dash?.analytics?.let { stats ->
            item { SectionTitle("Live residence pulse") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PulseTile("Open tickets", stats.openTickets, Icons.Outlined.Build, Tints.Gold, Modifier.weight(1f))
                    PulseTile("Complaints", stats.openComplaints, Icons.AutoMirrored.Outlined.VolumeUp, Tints.Sky, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PulseTile("Panic alerts", stats.openEmergencies, Icons.Outlined.Sos, Tints.Rose, Modifier.weight(1f))
                    PulseTile("Event RSVPs", stats.totalRsvps, Icons.Outlined.Event, Tints.Leaf, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PulseTile(label: String, value: Int, icon: ImageVector, tint: Tint, modifier: Modifier) {
    ResCard(modifier) {
        IconTile(icon, tint, 36.dp)
        Text("$value", color = Ink, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
        Text(label, color = Muted, fontSize = 13.sp)
    }
}
@Composable
fun FeedScreen(api: ResLinkApi, canPost: Boolean, canModerate: Boolean, userId: String = "") {
    var posts by remember { mutableStateOf(listOf<PostDto>()) }
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var isPoll by remember { mutableStateOf(false) }
    var options by remember { mutableStateOf("Braai, Movie night") }
    var comment by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf(false) }
    fun reload() = scope.launch { posts = runCatching { api.posts() }.getOrDefault(emptyList()); loaded = true }
    LaunchedEffect(Unit) { reload() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Community Feed", "Residence discussions and polls")
            if (canPost) {
                ResCard {
                    ResField(title, { title = it }, "Title")
                    Spacer(Modifier.height(8.dp))
                    ResField(body, { body = it }, "What's happening?")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(isPoll, { isPoll = it })
                        Text("This is a poll")
                    }
                    if (isPoll) ResField(options, { options = it }, "Options, comma separated")
                    ErrorText(error)
                    Spacer(Modifier.height(8.dp))
                    ResButton("Publish") {
                        scope.launch {
                            try {
                                api.createPost(
                                    CreatePostRequest(
                                        title,
                                        body,
                                        null,
                                        isPoll,
                                        if (isPoll) options.split(",").map { it.trim() } else null
                                    )
                                )
                                title = ""; body = ""; reload()
                            } catch (ex: Exception) { error = userMessage(ex) }
                        }
                    }
                }
            }
        }
        if (loaded && posts.isEmpty()) {
            item { EmptyState("No posts yet", "Start the conversation for your residence.", Icons.Outlined.Forum, Tints.Leaf) }
        }
        items(posts) { post ->
            ResCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(post.authorName, 38.dp)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(post.authorName, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(post.createdAt.take(10), color = Muted, fontSize = 12.sp)
                    }
                    if (post.kind != "Post") StatusChip(post.kind)
                }
                Text(post.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 12.dp))
                Text(post.body, color = InkSoft, modifier = Modifier.padding(top = 4.dp))
                post.pollOptions.forEach { option ->
                    Text(
                        "${option.label} · ${option.votes}",
                        color = Forest,
                        modifier = Modifier.padding(top = 6.dp).clickable {
                            scope.launch { api.vote(post.id, VoteRequest(option.index)); reload() }
                        }
                    )
                }
                post.comments.forEach { Text("${it.authorName}: ${it.body}", color = Muted, fontSize = 13.sp) }
                if (canPost) {
                    ResField(comment, { comment = it }, "Comment")
                    TextButton(onClick = {
                        scope.launch { api.comment(post.id, CreateCommentRequest(comment)); comment = ""; reload() }
                    }) { Text("Reply", color = Forest) }
                }
                if (canModerate || post.authorId == userId) {
                    TextButton(onClick = { scope.launch { api.deletePost(post.id); reload() } }) { Text("Remove post") }
                }
            }
        }
    }
}


@Composable
fun EventsScreen(api: ResLinkApi, isStudent: Boolean, isAdmin: Boolean) {
    var events by remember { mutableStateOf(listOf<EventDto>()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startsAt by remember {
        mutableStateOf(java.time.OffsetDateTime.now().plusDays(7).withHour(17).withMinute(0).withSecond(0).withNano(0).toString())
    }
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf(false) }
    fun reload() = scope.launch { events = runCatching { api.events() }.getOrDefault(emptyList()); loaded = true }
    LaunchedEffect(Unit) { reload() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Residence events", "RSVP and demo check-in codes")
            if (isAdmin) {
                ResCard {
                    ResField(title, { title = it }, "Event title")
                    Spacer(Modifier.height(8.dp))
                    ResField(description, { description = it }, "Description")
                    Spacer(Modifier.height(8.dp))
                    ResField(location, { location = it }, "Location")
                    Spacer(Modifier.height(8.dp))
                    ResField(startsAt, { startsAt = it }, "Starts at (date, time and offset)")
                    Spacer(Modifier.height(8.dp))
                    ResButton("Create event") {
                        scope.launch { api.createEvent(com.dynamicdevelopers.reslink.data.CreateEventRequest(title, description, location, startsAt)); reload() }
                    }
                }
            }
        }
        if (loaded && events.isEmpty()) {
            item { EmptyState("No events yet", "New residence events will show up here.", Icons.Outlined.Event, Tints.Sky) }
        }
        items(events) { event ->
            ResCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DateBadge(event.startsAt)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(event.title, color = Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${event.location} · ${localTime(event.startsAt)?.toLocalTime()?.toString()?.take(5) ?: ""}", color = Muted, fontSize = 14.sp)
                    }
                    if (event.hasRsvp) StatusChip("Going", Tints.Leaf)
                }
                Text(event.description, color = InkSoft, modifier = Modifier.padding(top = 10.dp))
                Text("${event.rsvpCount} going", color = Forest, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                Text("Check-in code: ${event.checkInCode}", color = Muted, fontSize = 12.sp)
                if (isStudent) {
                    TextButton(onClick = {
                        scope.launch {
                            if (event.hasRsvp) api.cancelRsvp(event.id) else api.rsvp(event.id)
                            reload()
                        }
                    }) { Text(if (event.hasRsvp) "Cancel RSVP" else "RSVP (+10 pts)", color = Forest) }
                }
            }
        }
    }
}

private fun localTime(iso: String): java.time.LocalDateTime? = runCatching {
    java.time.OffsetDateTime.parse(iso).atZoneSameInstant(java.time.ZoneId.systemDefault()).toLocalDateTime()
}.getOrElse { runCatching { java.time.LocalDateTime.parse(iso.take(19)) }.getOrNull() }

@Composable
private fun DateBadge(iso: String) {
    val date = localTime(iso)
    Column(
        Modifier.width(50.dp).clip(RoundedCornerShape(14.dp)).background(Tints.Sky.bg).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(date?.month?.name?.take(3) ?: "", color = Tints.Sky.fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(date?.dayOfMonth?.toString() ?: "?", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MarketplaceScreen(api: ResLinkApi, canSell: Boolean, userId: String = "", isAdmin: Boolean = false) {
    var items by remember { mutableStateOf(listOf<MarketplaceItemDto>()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Textbooks") }
    var price by remember { mutableStateOf("150") }
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf(false) }
    fun reload() = scope.launch { items = runCatching { api.marketplace() }.getOrDefault(emptyList()); loaded = true }
    LaunchedEffect(Unit) { reload() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Student marketplace", "Textbooks, furniture and extras")
            if (canSell) {
                ResCard {
                    ResField(title, { title = it }, "Item")
                    Spacer(Modifier.height(8.dp))
                    ResField(description, { description = it }, "Description")
                    Spacer(Modifier.height(8.dp))
                    ResField(category, { category = it }, "Category")
                    Spacer(Modifier.height(8.dp))
                    ResField(price, { price = it }, "Price (R)")
                    Spacer(Modifier.height(8.dp))
                    ResButton("List item") {
                        scope.launch {
                            api.createListing(CreateMarketplaceItemRequest(title, description, category, price.toDoubleOrNull() ?: 0.0))
                            reload()
                        }
                    }
                }
            }
        }
        if (loaded && items.isEmpty()) {
            item { EmptyState("No listings yet", "List something you no longer need.", Icons.Outlined.Storefront, Tints.Plum) }
        }
        items(items) { item ->
            val canManage = canSell && (item.sellerId == userId || isAdmin)
            ResCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Sell, Tints.Plum, 44.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(item.title, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text("${item.category} · ${item.condition}", color = Muted, fontSize = 13.sp)
                    }
                    Text(if (item.price == 0.0) "Free" else "R${item.price.toInt()}", color = Forest, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Text(item.description, color = InkSoft, modifier = Modifier.padding(top = 10.dp))
                Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(item.sellerName + (item.sellerRoom?.let { " · Room $it" } ?: ""), color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    if (item.isSold) StatusChip("Sold")
                }
                if (canManage && !item.isSold) {
                    TextButton(onClick = { scope.launch { api.markSold(item.id); reload() } }) { Text("Mark sold", color = Forest, fontWeight = FontWeight.SemiBold) }
                }
                if (canManage && item.isSold) {
                    TextButton(onClick = { scope.launch { api.markAvailable(item.id); reload() } }) { Text("Mark available", color = Forest, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}

@Composable
fun MaintenanceScreen(api: ResLinkApi, isStudent: Boolean, canUpdate: Boolean) {
    var tickets by remember { mutableStateOf(listOf<MaintenanceTicketDto>()) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var photo by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf(false) }
    fun reload() = scope.launch { tickets = runCatching { api.tickets() }.getOrDefault(emptyList()); loaded = true }
    LaunchedEffect(Unit) { reload() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Maintenance", if (isStudent) "Photo-based reporting with live status" else "Ticket board")
            if (isStudent) {
                ResCard {
                    ResField(title, { title = it }, "Issue")
                    Spacer(Modifier.height(8.dp))
                    ResField(description, { description = it }, "Details")
                    Spacer(Modifier.height(8.dp))
                    ResField(location, { location = it }, "Location")
                    Spacer(Modifier.height(8.dp))
                    ResField(photo, { photo = it }, "Photo URL (optional)")
                    Spacer(Modifier.height(8.dp))
                    ResButton("Submit ticket") {
                        scope.launch {
                            api.createTicket(CreateMaintenanceRequest(title, description, location, photo.ifBlank { null }))
                            reload()
                        }
                    }
                }
            }
        }
        if (loaded && tickets.isEmpty()) {
            item { EmptyState("No tickets", if (isStudent) "Report a problem and track it here." else "Nothing needs fixing right now.", Icons.Outlined.Build, Tints.Gold) }
        }
        items(tickets) { ticket ->
            ResCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Build, tintFor(ticket.priority), 40.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(ticket.title, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text("${ticket.location} · ${ticket.reporterName}", color = Muted, fontSize = 13.sp)
                    }
                    StatusChip(ticket.status)
                }
                Text(ticket.description, color = InkSoft, modifier = Modifier.padding(top = 10.dp))
                ticket.resolutionNotes?.let { Text("Notes: $it", color = Muted) }
                if (canUpdate) {
                    Row {
                        listOf("Open", "InProgress", "Resolved").forEach { status ->
                            FilterChip(selected = ticket.status == status, onClick = {
                                scope.launch { api.updateTicket(ticket.id, com.dynamicdevelopers.reslink.data.UpdateTicketStatusRequest(status, "Updated by staff")); reload() }
                            }, label = { Text(status) }, modifier = Modifier.padding(end = 6.dp))
                        }
                    }
                }
                if (isStudent && ticket.status == "Open") {
                    TextButton(onClick = {
                        scope.launch { api.updateTicket(ticket.id, com.dynamicdevelopers.reslink.data.UpdateTicketStatusRequest("Cancelled", "Cancelled by student")); reload() }
                    }) { Text("Cancel request", color = Forest) }
                }
            }
        }
    }
}

@Composable
fun PanicScreen(api: ResLinkApi, session: SessionStore) {
    var sent by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var mine by remember { mutableStateOf(listOf<EmergencyAlertDto>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (!granted) {
            error = "Location permission was denied. Room fallback will be used."
            scope.launch {
                api.panic(
                    CreateEmergencyRequest(
                        location = null,
                        message = "Panic button activated"
                    )
                )
                sent = true
            }
            return@rememberLauncherForActivityResult
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnSuccessListener { location ->
            scope.launch {
                try {
                    api.panic(
                        CreateEmergencyRequest(
                            location = null,
                            message = "Panic button activated",
                            latitude = location?.latitude,
                            longitude = location?.longitude
                        )
                    )
                    sent = true
                    mine = runCatching { api.myEmergencies() }.getOrDefault(emptyList())
                } catch (ex: Exception) {
                    error = userMessage(ex)
                }
            }
        }
    }

    fun reload() = scope.launch {
        mine = runCatching { api.myEmergencies() }.getOrDefault(emptyList())
    }

    LaunchedEffect(Unit) { reload() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Emergency", "One tap alerts residence security")
            ResCard(elevated = true) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Sos, Tints.Rose, 48.dp)
                    Column(Modifier.padding(start = 14.dp)) {
                        Text(if (sent) "Security has your alert" else "Need help right now?", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("${session.room().ifBlank { "Your room" }}, ${session.residenceName()}", color = Muted, fontSize = 14.sp)
                    }
                }
                listOf(
                    "Security is alerted straight away.",
                    "Your live location is shared if you allow it. Otherwise your room is used.",
                    "Mark it as a false alarm below if you pressed it by mistake."
                ).forEachIndexed { i, step ->
                    Row(Modifier.padding(top = if (i == 0) 16.dp else 8.dp), verticalAlignment = Alignment.Top) {
                        Text("${i + 1}", color = Tints.Rose.fg, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(18.dp))
                        Text(step, color = InkSoft, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(18.dp))

                ResButton(if (sent) "Alert sent. Send again" else "Send panic alert", color = Danger, icon = Icons.Outlined.Sos) {
                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                    if (hasLocationPermission) {
                        val client = LocationServices.getFusedLocationProviderClient(context)
                        client.lastLocation.addOnSuccessListener { location ->
                            scope.launch {
                                try {
                                    api.panic(
                                        CreateEmergencyRequest(
                                            location = null,
                                            message = "Panic button activated",
                                            latitude = location?.latitude,
                                            longitude = location?.longitude
                                        )
                                    )
                                    sent = true
                                    reload()
                                } catch (ex: Exception) {
                                    error = userMessage(ex)
                                }
                            }
                        }
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                }

                ErrorText(error)
            }
            if (mine.isNotEmpty()) SectionTitle("Your alerts", Modifier.padding(top = 8.dp))
        }

        items(mine) { alert ->
            ResCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(alert.location, color = Ink, fontWeight = FontWeight.SemiBold)
                    StatusChip(alert.status)
                }
                if (alert.status == "Open") {
                    TextButton(onClick = {
                        scope.launch { api.withdrawEmergency(alert.id); reload() }
                    }) {
                        Text("False alarm", color = Forest)
                    }
                }
            }
        }
    }
}

@Composable
fun NoiseScreen(api: ResLinkApi) {
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(true) }
    var sent by remember { mutableStateOf(false) }
    var mine by remember { mutableStateOf(listOf<com.dynamicdevelopers.reslink.data.NoiseComplaintDto>()) }
    val scope = rememberCoroutineScope()
    fun reload() = scope.launch { mine = runCatching { api.myNoise() }.getOrDefault(emptyList()) }
    LaunchedEffect(Unit) { reload() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Noise complaint", "Anonymous reporting to security")
            ResCard {
                ResField(location, { location = it }, "Location")
                Spacer(Modifier.height(8.dp))
                ResField(description, { description = it }, "What is happening?")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(anonymous, { anonymous = it })
                    Text("Report anonymously")
                }
                ResButton(if (sent) "Sent to security" else "Submit complaint") {
                    scope.launch {
                        api.createNoise(CreateNoiseRequest(location, description, anonymous))
                        sent = true
                        reload()
                    }
                }
            }
        }
        items(mine) { item ->
            ResCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(item.location, color = Ink, fontWeight = FontWeight.SemiBold)
                    StatusChip(item.status)
                }
                if (item.status == "Open") {
                    TextButton(onClick = { scope.launch { api.withdrawNoise(item.id); reload() } }) {
                        Text("Withdraw", color = Forest)
                    }
                }
            }
        }
    }
}

@Composable
fun RewardsScreen(api: ResLinkApi, session: SessionStore) {
    var rewards by remember { mutableStateOf(listOf<RewardDto>()) }
    var redemptions by remember { mutableStateOf(listOf<com.dynamicdevelopers.reslink.data.RedemptionDto>()) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    fun reload() = scope.launch {
        rewards = runCatching { api.rewards() }.getOrDefault(emptyList())
        redemptions = runCatching { api.redemptions() }.getOrDefault(emptyList())
    }
    LaunchedEffect(Unit) { reload() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            HeroCard {
                HeroText("Rewards", "${session.points()} points", "Earn points by joining events and study groups, then swap them for vouchers.")
            }
            ErrorText(error)
        }
        if (rewards.isNotEmpty()) item { SectionTitle("Available rewards") }
        items(rewards) { reward ->
            val affordable = session.points() >= reward.pointsCost
            ResCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.CardGiftcard, Tints.Gold, 44.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(reward.name, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text(reward.description, color = Muted, fontSize = 14.sp)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${reward.pointsCost} pts", color = Tints.Gold.fg, fontWeight = FontWeight.Bold)
                    TextButton(enabled = affordable, onClick = {
                        scope.launch {
                            try {
                                api.redeem(reward.id)
                                session.updatePoints(session.points() - reward.pointsCost)
                                reload()
                            } catch (ex: Exception) {
                                error = userMessage(ex)
                            }
                        }
                    }) { Text(if (affordable) "Redeem" else "Not enough points", color = if (affordable) Forest else Muted, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
        if (redemptions.isNotEmpty()) {
            item { SectionTitle("Your vouchers") }
        }
        items(redemptions) { item ->
            ResCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(item.rewardName, color = Ink, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    StatusChip(if (item.isUsed) "Used" else if (item.isVoided) "Voided" else "Pending")
                }
                Text(item.voucherCode, color = Forest, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp, modifier = Modifier.padding(top = 8.dp))
                Text("${item.pointsCost} pts. Show this code at the residence office.", color = Muted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun StudyGroupsScreen(api: ResLinkApi) {
    var groups by remember { mutableStateOf(listOf<StudyGroupDto>()) }
    var name by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf(false) }
    fun reload() = scope.launch { groups = runCatching { api.studyGroups() }.getOrDefault(emptyList()); loaded = true }
    LaunchedEffect(Unit) { reload() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            ScreenHeader("Study groups", "Find partners by module or interest")
            ResCard {
                ResField(name, { name = it }, "Group name")
                Spacer(Modifier.height(8.dp))
                ResField(topic, { topic = it }, "Topic")
                Spacer(Modifier.height(8.dp))
                ResField(description, { description = it }, "Description")
                Spacer(Modifier.height(8.dp))
                ResButton("Create group") {
                    scope.launch { api.createGroup(CreateStudyGroupRequest(name, topic, description)); reload() }
                }
            }
        }
        if (loaded && groups.isEmpty()) {
            item { EmptyState("No study groups yet", "Create the first one for your module.", Icons.Outlined.Groups, Tints.Sky) }
        }
        items(groups) { group ->
            ResCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Groups, Tints.Sky, 44.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(group.name, color = Ink, fontWeight = FontWeight.SemiBold)
                        Text("${group.topic} · ${group.memberCount}/${group.maxMembers} members", color = Muted, fontSize = 13.sp)
                    }
                    if (group.isMember) StatusChip("Member")
                }
                Text(group.description, color = InkSoft, modifier = Modifier.padding(top = 10.dp))
                if (!group.isMember) {
                    TextButton(onClick = { scope.launch { api.joinGroup(group.id); reload() } }) { Text("Join (+5 pts)", color = Forest, fontWeight = FontWeight.SemiBold) }
                } else {
                    TextButton(onClick = { scope.launch { api.leaveGroup(group.id); reload() } }) { Text("Leave group", color = Muted) }
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(api: ResLinkApi) {
    var items by remember { mutableStateOf(listOf<NotificationDto>()) }
    val scope = rememberCoroutineScope()
    var loaded by remember { mutableStateOf(false) }
    fun reload() = scope.launch { items = runCatching { api.notifications() }.getOrDefault(emptyList()); loaded = true }
    LaunchedEffect(Unit) { reload() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = ScreenPadding, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { ScreenHeader("Notifications", "Residence alerts and ticket updates") }
        if (loaded && items.isEmpty()) {
            item { EmptyState("You're all caught up", "New alerts and updates will appear here.", Icons.Outlined.Notifications, Tints.Leaf) }
        }
        items(items) { item ->
            ResCard(Modifier.clickable { scope.launch { api.readNotification(item.id); reload() } }) {
                Row(verticalAlignment = Alignment.Top) {
                    IconTile(Icons.Outlined.Notifications, if (item.isRead) Tints.Slate else Tints.Leaf, 38.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(item.title, color = Ink, fontWeight = if (item.isRead) FontWeight.Medium else FontWeight.Bold)
                        Text(item.body, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                    if (!item.isRead) StatusChip("New")
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(session: SessionStore, onLogout: () -> Unit) {
    val role = session.role() ?: ""
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(ScreenPadding)) {
        ScreenHeader("Profile", "Your account at ${session.residenceName()}")
        ResCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(session.fullName(), 56.dp)
                Column(Modifier.padding(start = 14.dp)) {
                    Text(session.fullName(), color = Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(session.email(), color = Muted, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    StatusChip(role)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        ResCard {
            ProfileRow(Icons.Outlined.Apartment, Tints.Leaf, "Residence", session.residenceName())
            if (session.room().isNotBlank()) ProfileRow(Icons.Outlined.MeetingRoom, Tints.Sky, "Room", session.room())
            if (role == "Student") ProfileRow(Icons.Outlined.Star, Tints.Gold, "Reward points", "${session.points()}")
        }
        Spacer(Modifier.height(20.dp))
        ResOutlinedButton("Log out", color = Danger, onClick = onLogout)
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, tint: Tint, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint, 36.dp)
        Text(label, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp).weight(1f))
        Text(value, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}
