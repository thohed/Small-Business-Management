package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.WorkTaskEntity
import com.example.data.model.TaskStatus
import com.example.data.model.UserRole
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
fun WorkManagementScreen(
    currentUser: UserEntity,
    tasks: List<WorkTaskEntity>,
    allUsers: List<UserEntity>,
    onBack: () -> Unit,
    onOpenAssignTask: () -> Unit,
    onMarkTaskDone: (taskId: Long) -> Unit,
    onDeleteTask: (Long) -> Unit
) {
    BackHandler { onBack() }

    val isAdminOrManager = currentUser.role == UserRole.ADMIN.name || currentUser.role == UserRole.MANAGER.name
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedStaffFilterId by remember { mutableStateOf<Long?>(null) }

    // If staff, only show their tasks. If admin/manager, show all or filtered.
    val baseTasks = if (isAdminOrManager) {
        if (selectedStaffFilterId != null) tasks.filter { it.assignedUserId == selectedStaffFilterId } else tasks
    } else {
        tasks.filter { it.assignedUserId == currentUser.id }
    }

    val doneTasks = baseTasks.filter { it.status == TaskStatus.COMPLETED.name }
    val dueTasks = baseTasks.filter { it.status == TaskStatus.DUE.name }
    val lateTasks = baseTasks.filter { it.status == TaskStatus.LATE.name }

    val displayedTasks = when (selectedTabIndex) {
        1 -> doneTasks
        2 -> dueTasks
        3 -> lateTasks
        else -> baseTasks
    }

    val totalEarnedDone = doneTasks.sumOf { it.earnedPay }
    val totalHolidayBonus = doneTasks.filter { it.isHoliday }.sumOf { it.earnedPay - (it.workUnits * it.baseRate) }

    Scaffold(
        floatingActionButton = {
            if (isAdminOrManager) {
                FloatingActionButton(
                    onClick = onOpenAssignTask,
                    containerColor = BrandBlue,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_assign_task")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Assign Work")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("work_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isAdminOrManager) "Staff Work Oversight" else "My Work & Pay History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Due work, Late work, Done work & Holiday compensation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Summary Metric Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Done Work", style = MaterialTheme.typography.labelSmall, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        Text("${doneTasks.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EmeraldGreen)
                        Text(currencyFormat.format(totalEarnedDone), fontSize = 11.sp, color = Slate700)
                    }
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Due Work", style = MaterialTheme.typography.labelSmall, color = AmberWarning, fontWeight = FontWeight.Bold)
                        Text("${dueTasks.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = AmberWarning)
                        Text("In Progress", fontSize = 11.sp, color = Slate700)
                    }
                    Box(modifier = Modifier.width(1.dp).height(36.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Late Work", style = MaterialTheme.typography.labelSmall, color = RoseExpense, fontWeight = FontWeight.Bold)
                        Text("${lateTasks.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RoseExpense)
                        Text("Overdue", fontSize = 11.sp, color = Slate700)
                    }
                }
            }

            // If Admin/Manager, show staff filter chips
            if (isAdminOrManager) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedStaffFilterId == null,
                        onClick = { selectedStaffFilterId = null },
                        label = { Text("All Staff") }
                    )
                    allUsers.filter { it.role == UserRole.STAFF.name }.forEach { staff ->
                        FilterChip(
                            selected = selectedStaffFilterId == staff.id,
                            onClick = { selectedStaffFilterId = staff.id },
                            label = { Text(staff.fullName.split(" ").firstOrNull() ?: staff.fullName) }
                        )
                    }
                }
            }

            // Tabs: All, Done, Due, Late
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("All (${baseTasks.size})", fontSize = 12.sp) },
                    modifier = Modifier.testTag("tab_work_all")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Done (${doneTasks.size})", fontSize = 12.sp, color = EmeraldGreen) },
                    modifier = Modifier.testTag("tab_work_done")
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Due (${dueTasks.size})", fontSize = 12.sp, color = AmberWarning) },
                    modifier = Modifier.testTag("tab_work_due")
                )
                Tab(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    text = { Text("Late (${lateTasks.size})", fontSize = 12.sp, color = RoseExpense) },
                    modifier = Modifier.testTag("tab_work_late")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Task list
            if (displayedTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No work tasks in this view", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedTasks, key = { it.id }) { task ->
                        val isTaskLate = task.status == TaskStatus.LATE.name
                        val isTaskDone = task.status == TaskStatus.COMPLETED.name

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("task_card_${task.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isTaskLate -> RoseContainer.copy(alpha = 0.4f)
                                    isTaskDone -> MaterialTheme.colorScheme.surface
                                    else -> AmberContainer.copy(alpha = 0.2f)
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
                                            text = "Assigned to: ${task.assignedUserName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    StatusBadge(
                                        text = when (task.status) {
                                            TaskStatus.COMPLETED.name -> "DONE WORK"
                                            TaskStatus.LATE.name -> "LATE WORK"
                                            else -> "DUE WORK"
                                        },
                                        containerColor = when (task.status) {
                                            TaskStatus.COMPLETED.name -> EmeraldContainer
                                            TaskStatus.LATE.name -> RoseContainer
                                            else -> AmberContainer
                                        },
                                        contentColor = when (task.status) {
                                            TaskStatus.COMPLETED.name -> EmeraldGreen
                                            TaskStatus.LATE.name -> RoseExpense
                                            else -> AmberWarning
                                        }
                                    )
                                }

                                if (task.description.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = task.description,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))

                                // Pay Breakdown row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${task.workUnits} unit(s) @ $${task.baseRate}/unit",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate700
                                        )
                                        if (task.isHoliday) {
                                            Text(
                                                text = "★ Holiday Rate (${task.holidayMultiplier}x multiplier applied)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PurpleAccent
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Calculated Pay", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    text = "Due: ${dateFormat.format(Date(task.dueDateMillis))}" +
                                            if (task.completedDateMillis != null) " • Completed: ${dateFormat.format(Date(task.completedDateMillis))}" else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = if (isTaskLate) RoseExpense else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                // Action buttons
                                if (!isTaskDone) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = { onMarkTaskDone(task.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("mark_task_done_${task.id}")
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Mark as Done Work", fontSize = 12.sp)
                                        }
                                    }
                                }

                                if (isAdminOrManager) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        IconButton(
                                            onClick = { onDeleteTask(task.id) },
                                            modifier = Modifier.size(28.dp).testTag("delete_task_${task.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = RoseExpense, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
