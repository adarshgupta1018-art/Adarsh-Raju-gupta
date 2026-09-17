package com.example.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldConfirmed
import com.example.ui.theme.EsportsBlack
import com.example.ui.theme.EsportsSurface
import com.example.ui.theme.EsportsSurfaceVariant
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RedRejected
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.viewmodel.AdminNavTab
import com.example.viewmodel.PlayerNavTab

@Composable
fun PlayerBottomBar(
    currentTab: PlayerNavTab,
    unreadCount: Int,
    onTabSelected: (PlayerNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = EsportsSurface,
        border = BorderStroke(1.dp, Color(0x33FFD700))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentTab == PlayerNavTab.HOME,
                testTag = "nav_home",
                onClick = { onTabSelected(PlayerNavTab.HOME) }
            )
            PlayerNavItem(
                icon = Icons.Default.SportsEsports,
                label = "Schedule",
                isSelected = currentTab == PlayerNavTab.TOURNAMENTS,
                testTag = "nav_schedule",
                onClick = { onTabSelected(PlayerNavTab.TOURNAMENTS) }
            )
            PlayerNavItem(
                icon = Icons.Default.Assignment,
                label = "My Matches",
                isSelected = currentTab == PlayerNavTab.MY_REGISTRATIONS,
                testTag = "nav_my_matches",
                onClick = { onTabSelected(PlayerNavTab.MY_REGISTRATIONS) }
            )
            PlayerNavItem(
                icon = Icons.Default.Notifications,
                label = "Alerts",
                badgeCount = unreadCount,
                isSelected = currentTab == PlayerNavTab.NOTIFICATIONS,
                testTag = "nav_notifications",
                onClick = { onTabSelected(PlayerNavTab.NOTIFICATIONS) }
            )
            PlayerNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = currentTab == PlayerNavTab.PROFILE,
                testTag = "nav_profile",
                onClick = { onTabSelected(PlayerNavTab.PROFILE) }
            )
        }
    }
}

@Composable
fun AdminBottomBar(
    currentTab: AdminNavTab,
    pendingCount: Int,
    onTabSelected: (AdminNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color(0xFF16151E),
        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayerNavItem(
                icon = Icons.Default.Dashboard,
                label = "Command",
                isSelected = currentTab == AdminNavTab.DASHBOARD,
                testTag = "admin_nav_dashboard",
                onClick = { onTabSelected(AdminNavTab.DASHBOARD) }
            )
            PlayerNavItem(
                icon = Icons.Default.Group,
                label = "Registrations",
                badgeCount = pendingCount,
                isSelected = currentTab == AdminNavTab.REGISTRATIONS,
                testTag = "admin_nav_registrations",
                onClick = { onTabSelected(AdminNavTab.REGISTRATIONS) }
            )
            PlayerNavItem(
                icon = Icons.Default.Key,
                label = "Custom Rooms",
                isSelected = currentTab == AdminNavTab.ROOM_MANAGEMENT,
                testTag = "admin_nav_rooms",
                onClick = { onTabSelected(AdminNavTab.ROOM_MANAGEMENT) }
            )
            PlayerNavItem(
                icon = Icons.Default.ViewList,
                label = "Slots",
                isSelected = currentTab == AdminNavTab.TOURNAMENTS,
                testTag = "admin_nav_slots",
                onClick = { onTabSelected(AdminNavTab.TOURNAMENTS) }
            )
            PlayerNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentTab == AdminNavTab.SETTINGS,
                testTag = "admin_nav_settings",
                onClick = { onTabSelected(AdminNavTab.SETTINGS) }
            )
        }
    }
}

@Composable
fun PlayerNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int = 0,
    testTag: String = ""
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (badgeCount > 0) {
            BadgedBox(
                badge = {
                    Badge(
                        containerColor = RedRejected,
                        contentColor = TextWhite
                    ) {
                        Text(text = "$badgeCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) GoldPrimary else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) GoldPrimary else TextMuted,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            color = if (isSelected) GoldPrimary else TextMuted,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun TopEsportsBar(
    title: String,
    isAdminMode: Boolean,
    onAdminToggleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EsportsSurface,
        border = BorderStroke(1.dp, Color(0x26FFD700))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isAdminMode) Color(0xFF7C3AED) else GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAdminMode) "⚡" else "♠",
                        color = EsportsBlack,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "ACE ESPORTS",
                        color = GoldPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isAdminMode) "ADMIN CONTROL PANEL" else "FREE FIRE TOURNAMENTS",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isAdminMode) Color(0x33EF4444) else Color(0x26FFD700),
                border = BorderStroke(1.dp, if (isAdminMode) RedRejected else GoldPrimary.copy(alpha = 0.5f)),
                modifier = Modifier.clickable { onAdminToggleClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = if (isAdminMode) RedRejected else GoldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAdminMode) "EXIT ADMIN" else "ADMIN PANEL",
                        color = if (isAdminMode) RedRejected else GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
