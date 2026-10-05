package com.dynamicdevelopers.reslink.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dynamicdevelopers.reslink.R
import com.dynamicdevelopers.reslink.data.ResLinkApi
import com.dynamicdevelopers.reslink.data.SessionStore
import com.dynamicdevelopers.reslink.ui.auth.ForgotPasswordScreen
import com.dynamicdevelopers.reslink.ui.auth.LoginScreen
import com.dynamicdevelopers.reslink.ui.auth.StaffRegisterScreen
import com.dynamicdevelopers.reslink.ui.auth.StudentRegisterScreen
import com.dynamicdevelopers.reslink.ui.screens.EventsScreen
import com.dynamicdevelopers.reslink.ui.screens.FeedScreen
import com.dynamicdevelopers.reslink.ui.screens.HomeScreen
import com.dynamicdevelopers.reslink.ui.screens.MaintenanceScreen
import com.dynamicdevelopers.reslink.ui.screens.MarketplaceScreen
import com.dynamicdevelopers.reslink.ui.screens.MoreMenuScreen
import com.dynamicdevelopers.reslink.ui.screens.NoiseScreen
import com.dynamicdevelopers.reslink.ui.screens.NotificationsScreen
import com.dynamicdevelopers.reslink.ui.screens.PanicScreen
import com.dynamicdevelopers.reslink.ui.screens.ProfileScreen
import com.dynamicdevelopers.reslink.ui.screens.RewardsScreen
import com.dynamicdevelopers.reslink.ui.screens.SecurityQueuesScreen
import com.dynamicdevelopers.reslink.ui.screens.StudyGroupsScreen
import com.dynamicdevelopers.reslink.ui.screens.UsersScreen
import com.dynamicdevelopers.reslink.ui.common.Avatar
import com.dynamicdevelopers.reslink.ui.theme.BrandNight
import com.dynamicdevelopers.reslink.ui.theme.CardWhite
import com.dynamicdevelopers.reslink.ui.theme.Forest
import com.dynamicdevelopers.reslink.ui.theme.LeafLight
import com.dynamicdevelopers.reslink.ui.theme.Mint
import com.dynamicdevelopers.reslink.ui.theme.Muted
import com.dynamicdevelopers.reslink.ui.theme.OnDarkMuted
import com.dynamicdevelopers.reslink.ui.theme.Page

data class Tab(val route: String, val label: String, val icon: ImageVector)

// Bottom navigation and role shells follow Jetpack Compose Navigation (Android Developers, 2025a).

@Composable
fun ResLinkNav(api: ResLinkApi, session: SessionStore) {
    val nav = rememberNavController()
    val start = if (session.isLoggedIn()) roleHome(session.role()) else "login"

    NavHost(navController = nav, startDestination = start) {
        composable("login") {
            LoginScreen(
                api, session,
                onLoggedIn = { role -> nav.navigate(roleHome(role)) { popUpTo("login") { inclusive = true } } },
                onStudentSignUp = { nav.navigate("registerStudent") },
                onStaffSignUp = { nav.navigate("registerStaff") },
                onForgotPassword = { nav.navigate("forgotPassword") }
            )
        }
        composable("forgotPassword") {
            ForgotPasswordScreen(api, onBack = { nav.popBackStack() })
        }
        composable("registerStudent") {
            StudentRegisterScreen(api, session, onDone = { role -> nav.navigate(roleHome(role)) { popUpTo("login") { inclusive = true } } }, onBack = { nav.popBackStack() })
        }
        composable("registerStaff") {
            StaffRegisterScreen(api, session, onDone = { role -> nav.navigate(roleHome(role)) { popUpTo("login") { inclusive = true } } }, onBack = { nav.popBackStack() })
        }
        composable("student") { RoleShell("Student", studentTabs, api, session, onLogout = { logout(session, nav) }) }
        composable("admin") { RoleShell("Admin", adminTabs, api, session, onLogout = { logout(session, nav) }) }
        composable("security") { RoleShell("Security", securityTabs, api, session, onLogout = { logout(session, nav) }) }
        composable("maintenance") { RoleShell("Maintenance", maintenanceTabs, api, session, onLogout = { logout(session, nav) }) }
    }
}

private val studentTabs = listOf(
    Tab("home", "Home", Icons.Outlined.Home),
    Tab("feed", "Community", Icons.Outlined.Forum),
    Tab("events", "Events", Icons.Outlined.Event),
    Tab("market", "Market", Icons.Outlined.Storefront),
    Tab("more", "More", Icons.Outlined.MoreHoriz)
)

private val adminTabs = listOf(
    Tab("home", "Home", Icons.Outlined.Home),
    Tab("users", "Users", Icons.Outlined.Person),
    Tab("events", "Events", Icons.Outlined.Event),
    Tab("tickets", "Tickets", Icons.Outlined.Build),
    Tab("more", "More", Icons.Outlined.MoreHoriz)
)

private val securityTabs = listOf(
    Tab("home", "Home", Icons.Outlined.Home),
    Tab("desk", "Desk", Icons.Outlined.Security),
    Tab("notifications", "Alerts", Icons.Outlined.Notifications),
    Tab("profile", "Profile", Icons.Outlined.Person)
)

private val maintenanceTabs = listOf(
    Tab("home", "Home", Icons.Outlined.Home),
    Tab("tickets", "Tickets", Icons.Outlined.Build),
    Tab("notifications", "Alerts", Icons.Outlined.Notifications),
    Tab("profile", "Profile", Icons.Outlined.Person)
)

// Phones get a dark top bar and a bottom bar; tablets get a dark side rail that matches the web sidebar (Android Developers, 2025c).
@Composable
private fun RoleShell(
    role: String,
    tabs: List<Tab>,
    api: ResLinkApi,
    session: SessionStore,
    onLogout: () -> Unit
) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route ?: tabs.first().route
    val onTab = tabs.any { it.route == current }
    val goTo: (String) -> Unit = { route ->
        nav.navigate(route) {
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(Page)) {
        if (maxWidth >= 600.dp) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = BrandNight,
                    header = {
                        Image(
                            painterResource(R.drawable.brand_mark), "ResLink",
                            Modifier.padding(top = 12.dp, bottom = 20.dp).size(44.dp)
                        )
                    }
                ) {
                    tabs.forEach { tab ->
                        NavigationRailItem(
                            selected = current == tab.route,
                            onClick = { goTo(tab.route) },
                            icon = { Icon(tab.icon, tab.label) },
                            label = { Text(tab.label, fontSize = 12.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = BrandNight,
                                selectedTextColor = Color.White,
                                indicatorColor = LeafLight,
                                unselectedIconColor = OnDarkMuted,
                                unselectedTextColor = OnDarkMuted
                            )
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box(Modifier.padding(bottom = 16.dp)) { Avatar(session.fullName(), 38.dp) }
                }
                Box(Modifier.weight(1f).fillMaxHeight().statusBarsPadding()) {
                    RoleNavHost(nav, role, api, session, onLogout, Modifier.fillMaxSize())
                }
            }
        } else {
            Scaffold(
                containerColor = Page,
                topBar = { PhoneTopBar(showBack = !onTab, name = session.fullName(), onBack = { nav.popBackStack() }) },
                bottomBar = {
                    if (onTab) {
                        NavigationBar(containerColor = CardWhite, tonalElevation = 0.dp) {
                            tabs.forEach { tab ->
                                NavigationBarItem(
                                    selected = current == tab.route,
                                    onClick = { goTo(tab.route) },
                                    icon = { Icon(tab.icon, tab.label) },
                                    label = {
                                        // Five labels share a phone's width, so a large system font would wrap "Community".
                                        val fontScale = LocalDensity.current.fontScale
                                        Text(
                                            tab.label, fontSize = (11f / maxOf(fontScale, 1f)).sp, fontWeight = FontWeight.SemiBold,
                                            letterSpacing = 0.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Forest,
                                        selectedTextColor = Forest,
                                        indicatorColor = Mint,
                                        unselectedIconColor = Muted,
                                        unselectedTextColor = Muted
                                    )
                                )
                            }
                        }
                    }
                }
            ) { padding ->
                RoleNavHost(nav, role, api, session, onLogout, Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun PhoneTopBar(showBack: Boolean, name: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(BrandNight)
            .statusBarsPadding()
            .height(60.dp)
            .padding(horizontal = if (showBack) 4.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White) }
        }
        Image(painterResource(R.drawable.brand_horizontal), "ResLink", Modifier.height(30.dp))
        Spacer(Modifier.weight(1f))
        Box(Modifier.padding(end = if (showBack) 12.dp else 0.dp)) { Avatar(name, 34.dp) }
    }
}

@Composable
private fun RoleNavHost(
    nav: NavHostController,
    role: String,
    api: ResLinkApi,
    session: SessionStore,
    onLogout: () -> Unit,
    modifier: Modifier
) {
    NavHost(navController = nav, startDestination = "home", modifier = modifier) {
        composable("home") { HomeScreen(api, session, role) }
        composable("feed") { FeedScreen(api, canPost = role == "Student" || role == "Admin", canModerate = role == "Admin", userId = session.userId()) }
        composable("events") { EventsScreen(api, isStudent = role == "Student", isAdmin = role == "Admin") }
        composable("market") { MarketplaceScreen(api, canSell = role == "Student" || role == "Admin", userId = session.userId(), isAdmin = role == "Admin") }
        composable("tickets") { MaintenanceScreen(api, isStudent = role == "Student", canUpdate = role == "Maintenance" || role == "Admin") }
        composable("maintenance") { MaintenanceScreen(api, isStudent = true, canUpdate = false) }
        composable("panic") { PanicScreen(api, session) }
        composable("noise") { NoiseScreen(api) }
        composable("rewards") { RewardsScreen(api, session) }
        composable("groups") { StudyGroupsScreen(api) }
        composable("notifications") { NotificationsScreen(api) }
        composable("users") { UsersScreen(api) }
        composable("desk") { SecurityQueuesScreen(api) }
        composable("profile") { ProfileScreen(api, session, onLogout) }
        composable("more") {
            MoreMenuScreen(role) { route -> nav.navigate(route) }
        }
    }
}

private fun roleHome(role: String?): String = when (role) {
    "Admin" -> "admin"
    "Security" -> "security"
    "Maintenance" -> "maintenance"
    else -> "student"
}

private fun logout(session: SessionStore, nav: androidx.navigation.NavHostController) {
    session.clear()
    nav.navigate("login") { popUpTo(0) { inclusive = true } }
}
