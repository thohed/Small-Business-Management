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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.PayRequestEntity
import com.example.data.local.UserEntity
import com.example.data.local.WorkTaskEntity
import com.example.data.model.PayRequestStatus
import com.example.data.model.TaskStatus
import com.example.data.model.UserEarningsSummary
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.Slate700
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmployeeDashboardScreen(
    currentUser: UserEntity,
    userEarnings: UserEarningsSummary?,
    allTasks: List<WorkTaskEntity>,
    allPayRequests: List<PayRequestEntity>,
    onOpenRequestPay: () -> Unit,
    onMarkTaskDone: (Long) -> Unit,
    onNavigate: (String) -> Unit
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    val userTasks = allTasks.filter { it.assignedUserId == currentUser.id }
    val userRequests = allPayRequests.filter { it.userId == currentUser.id }

    val now = System.currentTimeMillis()
    val completedTasks = userTasks.filter { it.status == TaskStatus.COMPLETED.name }
    val lateTasks = userTasks.filter {
        it.status == TaskStatus.LATE.name ||
        (it.status != TaskStatus.COMPLETED.name && it.status != TaskStatus.CANCELLED.name && it.dueDateMillis < now)
    }
    val dueTasks = userTasks.filter {
        (it.status == TaskStatus.DUE.name || it.status == TaskStatus.PENDING.name || it.status == TaskStatus.IN_PROGRESS.name) &&
        it.dueDateMillis >= now
    }

    var selectedFilterTab by remember { mutableIntStateOf(0) } // 0: Due, 1: Late, 2: Completed, 3: All

    val filteredTasks = when (selectedFilterTab) {
        0 -> dueTasks
        1 -> lateTasks
        2 -> completedTasks
        else -> userTasks
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("employee_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("employee_welcome_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Welcome, ${currentUser.fullName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${currentUser.jobPosition} • ${currentUser.department}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            if (currentUser.assignedManagerName.isNotEmpty()) {
                                Text(
                                    text = "Manager: ${currentUser.assignedManagerName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        StatusBadge(
                            text = currentUser.role,
                            containerColor = BrandBlue,
                            contentColor = Color.White
                        )
                    }
                }
            }
        }

        // 2. Personal Earnings & Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("employee_earnings_card"),
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
                        Column {
                            Text(
                                text = "Available Balance Due",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currencyFormat.format(userEarnings?.balanceDue ?: 0.0),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen
                            )
                        }

                        Button(
                            onClick = onOpenRequestPay,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("employee_btn_request_pay")
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Request Payout")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Work Earnings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormat.format(userEarnings?.workBasedEarnings ?: 0.0), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Holiday Bonus", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormat.format(userEarnings?.holidayEarnings ?: 0.0), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PurpleAccent)
                        }
                        Column {
                            Text("Total Paid Out", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormat.format(userEarnings?.totalPaidOut ?: 0.0), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    if ((userEarnings?.pendingRequestAmount ?: 0.0) > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pending payout request: ${currencyFormat.format(userEarnings?.pendingRequestAmount ?: 0.0)}",
                            fontSize = 11.sp,
                            color = AmberWarning,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 3. Work Status Pills
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Due Work", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                        Text("${dueTasks.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = AmberWarning)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Late Work", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoseExpense)
                        Text("${lateTasks.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = RoseExpense)
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Completed", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                        Text("${completedTasks.size}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                    }
                }
            }
        }

        // 4. Task Filter Chips
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = selectedFilterTab == 0,
                    onClick = { selectedFilterTab = 0 },
                    label = { Text("Due Work (${dueTasks.size})") }
                )
                FilterChip(
                    selected = selectedFilterTab == 1,
                    onClick = { selectedFilterTab = 1 },
                    label = { Text("Late Work (${lateTasks.size})") }
                )
                FilterChip(
                    selected = selectedFilterTab == 2,
                    onClick = { selectedFilterTab = 2 },
                    label = { Text("Completed (${completedTasks.size})") }
                )
                FilterChip(
                    selected = selectedFilterTab == 3,
                    onClick = { selectedFilterTab = 3 },
                    label = { Text("All (${userTasks.size})") }
                )
            }
        }

        // 5. Work Items List
        if (filteredTasks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No work tasks in this view", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                val isDone = task.status == TaskStatus.COMPLETED.name
                val isLate = task.status == TaskStatus.LATE.name || (task.status != TaskStatus.COMPLETED.name && task.dueDateMillis < now)

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("employee_task_card_${task.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isLate -> RoseContainer.copy(alpha = 0.35f)
                            isDone -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Department: ${task.department}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            StatusBadge(
                                text = when {
                                    isDone -> "COMPLETED"
                                    isLate -> "LATE WORK"
                                    else -> "DUE WORK"
                                },
                                containerColor = when {
                                    isDone -> EmeraldContainer
                                    isLate -> RoseContainer
                                    else -> AmberContainer
                                },
                                contentColor = when {
                                    isDone -> EmeraldGreen
                                    isLate -> RoseExpense
                                    else -> AmberWarning
                                }
                            )
                        }

                        if (task.description.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = task.description, style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${task.workUnits} units @ $${task.baseRate}/unit",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (task.isHoliday) {
                                    Text(
                                        text = "★ Holiday Overtime (${task.holidayMultiplier}x multiplier applied)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleAccent
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Work Pay:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = currencyFormat.format(task.earnedPay),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = EmeraldGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Due Date: ${dateFormat.format(Date(task.dueDateMillis))}" +
                                    if (task.completedDateMillis != null) " • Completed: ${dateFormat.format(Date(task.completedDateMillis))}" else "",
                            fontSize = 10.sp,
                            color = if (isLate) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!isDone) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onMarkTaskDone(task.id) },
                                modifier = Modifier.fillMaxWidth().testTag("employee_complete_task_${task.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark Work Completed")
                            }
                        }
                    }
                }
            }
        }
    }
}
