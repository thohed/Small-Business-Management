package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PayRequestEntity
import com.example.data.local.UserEntity
import com.example.data.local.WorkTaskEntity
import com.example.data.model.PayRequestStatus
import com.example.data.model.TaskStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.Slate700
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ManagerDashboardScreen(
    currentUser: UserEntity,
    allUsers: List<UserEntity>,
    allTasks: List<WorkTaskEntity>,
    allPayRequests: List<PayRequestEntity>,
    onNavigate: (String) -> Unit,
    onOpenAssignTask: () -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    // Subordinate team members (assigned to this manager or within same department)
    val teamMembers = allUsers.filter { it.assignedManagerId == currentUser.id || it.department == currentUser.department }
    val teamMemberIds = teamMembers.map { it.id }.toSet()

    val teamTasks = allTasks.filter { teamMemberIds.contains(it.assignedUserId) || it.assignedManagerId == currentUser.id }
    val pendingPayRequests = allPayRequests.filter { teamMemberIds.contains(it.userId) && it.status == PayRequestStatus.PENDING.name }

    val now = System.currentTimeMillis()
    val completedTasks = teamTasks.filter { it.status == TaskStatus.COMPLETED.name }
    val lateTasks = teamTasks.filter {
        it.status == TaskStatus.LATE.name ||
        (it.status != TaskStatus.COMPLETED.name && it.status != TaskStatus.CANCELLED.name && it.dueDateMillis < now)
    }
    val dueTasks = teamTasks.filter {
        (it.status == TaskStatus.DUE.name || it.status == TaskStatus.PENDING.name || it.status == TaskStatus.IN_PROGRESS.name) &&
        it.dueDateMillis >= now
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("manager_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Manager Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("manager_welcome_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Manager: ${currentUser.fullName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Department: ${currentUser.department} • Team size: ${teamMembers.size}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        StatusBadge(text = "MANAGER", containerColor = EmeraldGreen, contentColor = Color.White)
                    }
                }
            }
        }

        // 2. Urgent Attention Row
        if (pendingPayRequests.isNotEmpty() || lateTasks.isNotEmpty()) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (pendingPayRequests.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("PAYMENTS") }
                                .testTag("manager_alert_pay_requests"),
                            colors = CardDefaults.cardColors(containerColor = AmberContainer)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${pendingPayRequests.size} Pay Requests", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberWarning)
                                    Text("Awaiting review", fontSize = 11.sp, color = Slate700)
                                }
                            }
                        }
                    }

                    if (lateTasks.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("WORK_TASKS") }
                                .testTag("manager_alert_late_tasks"),
                            colors = CardDefaults.cardColors(containerColor = RoseContainer)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RoseExpense, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${lateTasks.size} Late Tasks", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoseExpense)
                                    Text("Team overdue work", fontSize = 11.sp, color = Slate700)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Team Work Status Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Department Work Status", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Button(
                            onClick = onOpenAssignTask,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("manager_btn_assign_task")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Assign Work")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Completed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            Text("${completedTasks.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                        }
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Due Tasks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                            Text("${dueTasks.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AmberWarning)
                        }
                        Box(modifier = Modifier.width(1.dp).height(32.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Late Tasks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseExpense)
                            Text("${lateTasks.size}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = RoseExpense)
                        }
                    }
                }
            }
        }

        // 4. Managed Team Members
        item {
            Text("Department Staff Members (${teamMembers.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        items(teamMembers, key = { it.id }) { staff ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("manager_team_member_${staff.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(BrandBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(staff.fullName.take(1), fontWeight = FontWeight.Bold, color = BrandBlue)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(staff.fullName, fontWeight = FontWeight.Bold)
                            Text("${staff.jobPosition} • Rate: $${staff.perWorkRate}/unit", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    StatusBadge(
                        text = staff.userStatus,
                        containerColor = if (staff.userStatus == "ACTIVE") EmeraldContainer else AmberContainer,
                        contentColor = if (staff.userStatus == "ACTIVE") EmeraldGreen else AmberWarning
                    )
                }
            }
        }
    }
}
