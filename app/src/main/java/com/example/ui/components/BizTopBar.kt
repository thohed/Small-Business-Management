package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.RoseExpense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BizTopBar(
    title: String,
    currentUser: UserEntity?,
    allUsers: List<UserEntity>,
    unreadNotifCount: Int = 0,
    onNavigate: (String) -> Unit,
    onSwitchUser: (UserEntity) -> Unit,
    onLogout: () -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onNavigate("DASHBOARD") }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = "BizOps",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "BizOps",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Notification Icon with Unread Badge
                    IconButton(
                        onClick = { onNavigate("NOTIFICATIONS") },
                        modifier = Modifier.testTag("topbar_btn_notifications")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotifCount > 0) {
                                    Badge(containerColor = RoseExpense) {
                                        Text("$unreadNotifCount", fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }
                    }

                    currentUser?.let { user ->
                        val roleColor = when (user.role) {
                            UserRole.SUPER_ADMIN.name, UserRole.ADMIN.name -> BrandBlue
                            UserRole.MANAGER.name -> EmeraldGreen
                            UserRole.STAFF.name -> PurpleAccent
                            else -> AmberWarning
                        }

                        // User profile pill with dropdown for switching accounts
                        Box {
                            Row(
                                modifier = Modifier
                                    .testTag("user_profile_pill")
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { showUserMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(roleColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.fullName.take(1).uppercase(),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = user.fullName.split(" ").firstOrNull() ?: user.fullName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = user.role.replace("_", " "),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = roleColor
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch User",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showUserMenu,
                                onDismissRequest = { showUserMenu = false },
                                modifier = Modifier.testTag("user_dropdown_menu")
                            ) {
                                Text(
                                    text = "Modules & Tools",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Celebration, contentDescription = null, tint = PurpleAccent) },
                                    text = { Text("Holidays & Overtime") },
                                    onClick = {
                                        showUserMenu = false
                                        onNavigate("HOLIDAYS")
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.History, contentDescription = null, tint = BrandBlue) },
                                    text = { Text("Security Audit Logs") },
                                    onClick = {
                                        showUserMenu = false
                                        onNavigate("AUDIT_LOGS")
                                    }
                                )
                                DropdownMenuItem(
                                    leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = EmeraldGreen) },
                                    text = { Text("Business Settings") },
                                    onClick = {
                                        showUserMenu = false
                                        onNavigate("SETTINGS")
                                    }
                                )

                                HorizontalDivider()
                                Text(
                                    text = "Switch Account (Role Demo)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )

                                allUsers.forEach { otherUser ->
                                    val isSelected = otherUser.id == user.id
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            when (otherUser.role) {
                                                                UserRole.SUPER_ADMIN.name, UserRole.ADMIN.name -> BrandBlue
                                                                UserRole.MANAGER.name -> EmeraldGreen
                                                                UserRole.STAFF.name -> PurpleAccent
                                                                else -> AmberWarning
                                                            }
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "${otherUser.fullName} ${if (isSelected) "✓" else ""}",
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "${otherUser.role.replace("_", " ")} • ${otherUser.department}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        },
                                        onClick = {
                                            showUserMenu = false
                                            onSwitchUser(otherUser)
                                        }
                                    )
                                }

                                HorizontalDivider()
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Logout",
                                            tint = RoseExpense
                                        )
                                    },
                                    text = { Text("Log Out", color = RoseExpense) },
                                    onClick = {
                                        showUserMenu = false
                                        onLogout()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}
