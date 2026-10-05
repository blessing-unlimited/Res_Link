package com.dynamicdevelopers.reslink.ui.common

// Shared cards, fields and chips follow the web design system so both apps feel like one product (Android Developers, 2025b).

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dynamicdevelopers.reslink.R
import com.dynamicdevelopers.reslink.ui.theme.BrandNight
import com.dynamicdevelopers.reslink.ui.theme.BrandNightRaised
import com.dynamicdevelopers.reslink.ui.theme.CardWhite
import com.dynamicdevelopers.reslink.ui.theme.Danger
import com.dynamicdevelopers.reslink.ui.theme.Forest
import com.dynamicdevelopers.reslink.ui.theme.Ink
import com.dynamicdevelopers.reslink.ui.theme.InkSoft
import com.dynamicdevelopers.reslink.ui.theme.Leaf
import com.dynamicdevelopers.reslink.ui.theme.LeafLight
import com.dynamicdevelopers.reslink.ui.theme.Line
import com.dynamicdevelopers.reslink.ui.theme.Muted
import com.dynamicdevelopers.reslink.ui.theme.OnDark
import com.dynamicdevelopers.reslink.ui.theme.OnDarkMuted
import com.dynamicdevelopers.reslink.ui.theme.Tint
import com.dynamicdevelopers.reslink.ui.theme.Tints

private val CardShape = RoundedCornerShape(20.dp)
private val FieldShape = RoundedCornerShape(12.dp)

val ScreenPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 28.dp)

@Composable
fun ResCard(modifier: Modifier = Modifier, elevated: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (elevated) Modifier.shadow(18.dp, CardShape, ambientColor = BrandNight.copy(alpha = 0.12f), spotColor = BrandNight.copy(alpha = 0.16f)) else Modifier)
            .clip(CardShape)
            .background(CardWhite)
            .border(1.dp, Line, CardShape)
            .padding(18.dp),
        content = content
    )
}

@Composable
fun ResField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    password: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        shape = FieldShape,
        visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Leaf,
            unfocusedBorderColor = Color(0xFFD3D9D1),
            focusedLabelColor = Forest,
            cursorColor = Forest,
            focusedContainerColor = CardWhite,
            unfocusedContainerColor = CardWhite
        )
    )
}

@Composable
fun ResButton(
    text: String,
    loading: Boolean = false,
    enabled: Boolean = true,
    color: Color = Forest,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = Tints.Slate.bg,
            disabledContentColor = Color(0xFF98A39C)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            if (icon != null) {
                Icon(icon, null, Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
            }
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun ResOutlinedButton(text: String, modifier: Modifier = Modifier.fillMaxWidth(), color: Color = InkSoft, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD3D9D1)),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = CardWhite, contentColor = color)
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun ScreenHeader(title: String, subtitle: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Ink, fontSize = 27.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp)
            if (subtitle.isNotBlank()) Text(subtitle, color = Muted, fontSize = 14.sp, modifier = Modifier.padding(top = 2.dp))
        }
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action, color = Forest, fontWeight = FontWeight.SemiBold) }
        }
    }
    Spacer(Modifier.height(18.dp))
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp, modifier = modifier.padding(top = 8.dp, bottom = 2.dp))
}

fun tintFor(text: String): Tint = when (text.lowercase()) {
    "resolved", "used", "member", "departed", "student", "like new" -> Tints.Leaf
    "open", "pending", "medium", "maintenance", "fair" -> Tints.Gold
    "inprogress", "in progress", "acknowledged", "onsite", "new", "good" -> Tints.Sky
    "high", "urgent", "security", "voided", "sold" -> Tints.Rose
    "admin", "announcement", "poll" -> Tints.Plum
    else -> Tints.Slate
}

private fun prettyStatus(text: String) = when (text) {
    "Open" -> "Pending"
    "InProgress" -> "In progress"
    "OnSite" -> "On site"
    "Withdrawn" -> "Withdrawn"
    else -> text
}

@Composable
fun StatusChip(text: String, tint: Tint = tintFor(text)) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(tint.bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(tint.fg))
        Spacer(Modifier.size(6.dp))
        Text(prettyStatus(text), color = tint.fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun IconTile(icon: ImageVector, tint: Tint, size: Dp = 44.dp) {
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.28f)).background(tint.bg),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = tint.fg, modifier = Modifier.size(size * 0.5f)) }
}

@Composable
fun Avatar(name: String, size: Dp = 40.dp) {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifBlank { "R" }
    Box(
        Modifier.size(size).clip(CircleShape).background(Brush.linearGradient(listOf(LeafLight, Leaf))),
        contentAlignment = Alignment.Center
    ) { Text(initials, color = BrandNight, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.36f).sp) }
}

/** Dark brand card with a faint logo mark, used for the dashboard welcome and the rewards balance. */
@Composable
fun HeroCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(BrandNightRaised, BrandNight)))
    ) {
        Image(
            painterResource(R.drawable.brand_mark), null,
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).size(150.dp).alpha(0.07f)
        )
        Column(Modifier.padding(22.dp), content = content)
    }
}

@Composable
fun HeroStat(label: String, value: String, modifier: Modifier = Modifier, valueColor: Color = Color.White) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(LeafLight.copy(alpha = 0.10f))
            .border(1.dp, LeafLight.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, color = OnDarkMuted, fontSize = 12.sp)
        Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
fun HeroText(eyebrow: String, title: String, body: String) {
    Text(eyebrow, color = LeafLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    Text(title, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp, modifier = Modifier.padding(top = 6.dp))
    if (body.isNotBlank()) Text(body, color = OnDark.copy(alpha = 0.72f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
fun EmptyState(title: String, body: String, icon: ImageVector, tint: Tint = Tints.Leaf) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(CardWhite)
            .border(1.dp, Color(0xFFD3D9D1), CardShape)
            .padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconTile(icon, tint, 52.dp)
        Text(title, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.padding(top = 14.dp))
        Text(body, color = Muted, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun ErrorText(message: String?) {
    if (!message.isNullOrBlank()) {
        Text(message, color = Danger, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
    }
}
