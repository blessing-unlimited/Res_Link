package com.dynamicdevelopers.reslink.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynamicdevelopers.reslink.R
import com.dynamicdevelopers.reslink.ResLinkApp
import com.dynamicdevelopers.reslink.data.ForgotPasswordRequest
import com.dynamicdevelopers.reslink.data.LoginRequest
import com.dynamicdevelopers.reslink.data.ResidenceDto
import com.dynamicdevelopers.reslink.data.ResLinkApi
import com.dynamicdevelopers.reslink.data.SessionStore
import com.dynamicdevelopers.reslink.data.StaffRegisterRequest
import com.dynamicdevelopers.reslink.data.StudentRegisterRequest
import com.dynamicdevelopers.reslink.ui.common.ErrorText
import com.dynamicdevelopers.reslink.ui.common.ResButton
import com.dynamicdevelopers.reslink.ui.common.ResCard
import com.dynamicdevelopers.reslink.ui.common.ResField
import com.dynamicdevelopers.reslink.ui.common.ResOutlinedButton
import com.dynamicdevelopers.reslink.ui.theme.BrandNight
import com.dynamicdevelopers.reslink.ui.theme.CardWhite
import com.dynamicdevelopers.reslink.ui.theme.Forest
import com.dynamicdevelopers.reslink.ui.theme.Ink
import com.dynamicdevelopers.reslink.ui.theme.InkSoft
import com.dynamicdevelopers.reslink.ui.theme.Leaf
import com.dynamicdevelopers.reslink.ui.theme.Line
import com.dynamicdevelopers.reslink.ui.theme.Mint
import com.dynamicdevelopers.reslink.ui.theme.Muted
import com.dynamicdevelopers.reslink.ui.theme.OnDarkMuted
import com.dynamicdevelopers.reslink.ui.theme.Page
import com.dynamicdevelopers.reslink.ui.theme.Tints
import kotlinx.coroutines.launch
import retrofit2.HttpException

// Sign-in screens share one layout: the cropped logo on the brand colour, then a card that overlaps it (Android Developers, 2025a).
@Composable
private fun AuthLayout(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    below: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Page)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(BrandNight)
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 76.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.brand_lockup), "ResLink. Find. Connect. Live.", Modifier.width(150.dp))
                Text(
                    "Community, safety and maintenance for your residence.",
                    color = OnDarkMuted, fontSize = 14.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp, start = 40.dp, end = 40.dp)
                )
            }
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(start = 4.dp)) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White)
                }
            }
        }
        Column(
            Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 480.dp)
                .offset(y = (-52).dp)
                .padding(horizontal = 16.dp)
        ) {
            ResCard(elevated = true) {
                Text(title, color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp)
                Text(subtitle, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))
                content()
            }
            below()
        }
    }
}

private val demoAccounts = listOf(
    Triple("Student", "student@reslink.app", "Student123!"),
    Triple("Admin", "admin@reslink.app", "Admin123!"),
    Triple("Security", "security@reslink.app", "Security123!"),
    Triple("Maintenance", "maintenance@reslink.app", "Maintenance123!")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    api: ResLinkApi,
    session: SessionStore,
    onLoggedIn: (String) -> Unit,
    onStudentSignUp: () -> Unit,
    onStaffSignUp: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun signIn() {
        scope.launch {
            loading = true
            error = null
            var successRole: String? = null
            try {
                val auth = api.login(LoginRequest(email.trim(), password))
                session.save(auth)
                successRole = auth.role
                (context.applicationContext as? ResLinkApp)?.syncPushToken()
            } catch (ex: Exception) {
                error = userMessage(ex)
            } finally {
                loading = false
            }
            successRole?.let(onLoggedIn)
        }
    }

    AuthLayout(
        title = "Welcome back",
        subtitle = "Sign in to open the dashboard for your role.",
        below = {
            Text("New to ResLink?", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 22.dp, bottom = 10.dp, start = 4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResOutlinedButton("Student sign-up", Modifier.weight(1f), color = Forest, onClick = onStudentSignUp)
                ResOutlinedButton("Staff sign-up", Modifier.weight(1f), onClick = onStaffSignUp)
            }
            Text("Explore with a demo account", color = Muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 22.dp, bottom = 6.dp, start = 4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                demoAccounts.forEachIndexed { i, (label, demoEmail, demoPassword) ->
                    val dot = listOf(Leaf, Tints.Plum.fg, Tints.Rose.fg, Tints.Gold.fg)[i]
                    AssistChip(
                        onClick = { email = demoEmail; password = demoPassword; signIn() },
                        label = { Text(label, fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Box(Modifier.size(8.dp).clip(CircleShape).background(dot)) },
                        shape = RoundedCornerShape(999.dp),
                        colors = AssistChipDefaults.assistChipColors(containerColor = CardWhite, labelColor = InkSoft),
                        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = Line)
                    )
                }
            }
        }
    ) {
        ResField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(12.dp))
        ResField(password, { password = it }, "Password", password = true)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onForgotPassword) { Text("Forgot password?", color = Forest, fontWeight = FontWeight.SemiBold) }
        }
        ErrorText(error)
        Spacer(Modifier.height(8.dp))
        ResButton("Sign in", loading, enabled = email.isNotBlank() && password.isNotBlank()) { signIn() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> ResDropdown(label: String, selected: String, options: List<T>, optionLabel: (T) -> String, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Leaf, unfocusedBorderColor = Color(0xFFD3D9D1), focusedLabelColor = Forest),
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(optionLabel(option)) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

private suspend fun loadResidences(api: ResLinkApi, onError: (String) -> Unit): List<ResidenceDto> {
    val residences = runCatching { api.residences() }.getOrElse {
        onError("Residences could not be loaded. Check your connection, then go back and try again.")
        return emptyList()
    }
    if (residences.isEmpty()) onError("No residences are set up yet. Ask the residence admin to add one.")
    return residences
}

@Composable
fun StudentRegisterScreen(
    api: ResLinkApi,
    session: SessionStore,
    onDone: (String) -> Unit,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var studentNumber by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var residences by remember { mutableStateOf(listOf<ResidenceDto>()) }
    var selected by remember { mutableStateOf<ResidenceDto?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        residences = loadResidences(api) { error = it }
        selected = residences.firstOrNull()
    }

    AuthLayout("Join your residence", "Create a student account in under a minute.", onBack = onBack) {
        ResField(fullName, { fullName = it }, "Full name")
        Spacer(Modifier.height(10.dp))
        ResField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        ResField(password, { password = it }, "Password (8+ characters)", password = true)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { ResField(studentNumber, { studentNumber = it }, "Student no.") }
            Box(Modifier.weight(1f)) { ResField(room, { room = it }, "Room") }
        }
        Spacer(Modifier.height(10.dp))
        ResDropdown("Residence", selected?.name ?: "Select residence", residences, { it.name }) { selected = it }
        ErrorText(error)
        Spacer(Modifier.height(16.dp))
        ResButton("Create student account", loading) {
            val residence = selected
            if (residence == null) {
                error = "Choose a residence."
                return@ResButton
            }
            scope.launch {
                loading = true
                error = null
                try {
                    val auth = api.registerStudent(StudentRegisterRequest(fullName, email, password, studentNumber, residence.id, room))
                    session.save(auth)
                    (context.applicationContext as? ResLinkApp)?.syncPushToken()
                    onDone(auth.role)
                } catch (ex: Exception) {
                    error = userMessage(ex)
                } finally {
                    loading = false
                }
            }
        }
    }
}

@Composable
fun StaffRegisterScreen(api: ResLinkApi, session: SessionStore, onDone: (String) -> Unit, onBack: () -> Unit) {
    val roles = listOf("Admin", "Security", "Maintenance")
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var staffId by remember { mutableStateOf("") }
    var accessCode by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Security") }
    var residences by remember { mutableStateOf(listOf<ResidenceDto>()) }
    var selected by remember { mutableStateOf<ResidenceDto?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        residences = loadResidences(api) { error = it }
        selected = residences.firstOrNull()
    }

    AuthLayout("Staff sign-up", "Admins, security and maintenance staff need the access code from the residence.", onBack = onBack) {
        ResField(fullName, { fullName = it }, "Full name")
        Spacer(Modifier.height(10.dp))
        ResField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        Spacer(Modifier.height(10.dp))
        ResField(password, { password = it }, "Password (8+ characters)", password = true)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { ResField(staffId, { staffId = it }, "Staff ID") }
            Box(Modifier.weight(1f)) { ResDropdown("Role", role, roles, { it }) { role = it } }
        }
        Spacer(Modifier.height(10.dp))
        ResField(accessCode, { accessCode = it }, "Staff access code", password = true)
        Spacer(Modifier.height(10.dp))
        ResDropdown("Residence", selected?.name ?: "Select residence", residences, { it.name }) { selected = it }
        ErrorText(error)
        Spacer(Modifier.height(16.dp))
        ResButton("Create staff account", loading) {
            val residence = selected
            if (residence == null) {
                error = "Choose a residence."
                return@ResButton
            }
            scope.launch {
                loading = true
                error = null
                try {
                    val auth = api.registerStaff(StaffRegisterRequest(fullName, email, password, staffId, role, accessCode, residence.id))
                    session.save(auth)
                    (context.applicationContext as? ResLinkApp)?.syncPushToken()
                    onDone(auth.role)
                } catch (ex: Exception) {
                    error = userMessage(ex)
                } finally {
                    loading = false
                }
            }
        }
        Text("Demo staff code: RESLINK-STAFF-2026", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
fun ForgotPasswordScreen(api: ResLinkApi, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AuthLayout(
        "Reset your password",
        "Enter your account email and we'll send a one-time link. If email isn't set up at your residence yet, ask the residence admin for a reset link.",
        onBack = onBack
    ) {
        ResField(email, { email = it }, "Email", keyboardType = KeyboardType.Email)
        ErrorText(error)
        message?.let {
            Text(
                it, color = Forest, fontSize = 14.sp,
                modifier = Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Mint).padding(12.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        ResButton("Send reset link", loading, enabled = email.isNotBlank(), icon = Icons.Outlined.Email) {
            scope.launch {
                loading = true
                error = null
                try {
                    message = api.forgotPassword(ForgotPasswordRequest(email.trim())).message
                } catch (ex: Exception) {
                    error = userMessage(ex)
                } finally {
                    loading = false
                }
            }
        }
    }
}

fun userMessage(ex: Throwable): String {
    return when (ex) {
        is HttpException -> when (ex.code()) {
            401 -> "Invalid credentials or access code."
            403 -> "Your role cannot use this action."
            409 -> "That email is already registered."
            400 -> serverText(ex) ?: "Please check the details and try again."
            else -> "Request failed (${ex.code()})."
        }
        else -> ex.message ?: "Could not reach ResLink. Check your connection and try again."
    }
}

// The API sends short plain-text reasons for 400s; validation errors arrive as JSON and are not shown.
private fun serverText(ex: HttpException): String? {
    val text = runCatching { ex.response()?.errorBody()?.string() }.getOrNull()?.trim()
    return text?.takeIf { it.isNotEmpty() && it.length <= 200 && !it.startsWith("{") }
}
