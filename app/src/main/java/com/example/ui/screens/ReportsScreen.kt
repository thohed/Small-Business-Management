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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.example.data.local.UserEntity
import com.example.data.local.WorkTaskEntity
import com.example.data.model.DailySummary
import com.example.data.model.MonthlySummary
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
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReportsScreen(
    dailySummary: DailySummary,
    monthlySummary: MonthlySummary,
    allTasks: List<WorkTaskEntity>,
    allUsers: List<UserEntity>,
    exportMessage: String?,
    onExportCsv: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    var selectedReportTab by remember { mutableIntStateOf(0) } // 0: Financial, 1: Workforce, 2: Departments

    val monthTotalCost = monthlySummary.totalCost.coerceAtLeast(1.0)
    val buysPct = (monthlySummary.totalBuys / monthTotalCost).toFloat().coerceIn(0f, 1f)
    val overheadPct = ((monthlySummary.totalExpenses - monthlySummary.totalExpansionExpenses) / monthTotalCost).toFloat().coerceIn(0f, 1f)
    val expansionPct = (monthlySummary.totalExpansionExpenses / monthTotalCost).toFloat().coerceIn(0f, 1f)
    val laborPct = (monthlySummary.totalLaborPaid / monthTotalCost).toFloat().coerceIn(0f, 1f)

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("reports_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("reports_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Business Intelligence & Reports",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Financial, Workforce & Departmental Analytics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onExportCsv,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_export_csv")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export CSV", fontSize = 11.sp)
                    }
                }
            }

            if (exportMessage != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                    ) {
                        Text(
                            text = "✓ $exportMessage",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Report Scope Tabs
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedReportTab == 0,
                        onClick = { selectedReportTab = 0 },
                        label = { Text("Financial Summary") }
                    )
                    FilterChip(
                        selected = selectedReportTab == 1,
                        onClick = { selectedReportTab = 1 },
                        label = { Text("Workforce Analytics") }
                    )
                    FilterChip(
                        selected = selectedReportTab == 2,
                        onClick = { selectedReportTab = 2 },
                        label = { Text("Departments") }
                    )
                }
            }

            if (selectedReportTab == 0) {
                // 1. Monthly Cost & Profit Headline Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BrandBlue)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = monthlySummary.yearMonth,
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                StatusBadge(
                                    text = if (monthlySummary.netProfit >= 0) "Net Profit" else "Net Loss",
                                    containerColor = if (monthlySummary.netProfit >= 0) EmeraldContainer else RoseContainer,
                                    contentColor = if (monthlySummary.netProfit >= 0) EmeraldGreen else RoseExpense
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Gross Revenue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currencyFormat.format(monthlySummary.totalSales), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = EmeraldGreen)
                                }
                                Column {
                                    Text("Total Business Cost", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currencyFormat.format(monthlySummary.totalCost), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = RoseExpense)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Monthly Net Profit:", fontWeight = FontWeight.Bold)
                                Text(
                                    text = currencyFormat.format(monthlySummary.netProfit),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp,
                                    color = if (monthlySummary.netProfit >= 0) EmeraldGreen else RoseExpense
                                )
                            }
                        }
                    }
                }

                // 2. Cost Distribution Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Cost Distribution", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Automated breakdown of purchases, overhead, expansion and payroll", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(16.dp))

                            CostBarRow("Purchases & Materials", currencyFormat.format(monthlySummary.totalBuys), "${(buysPct * 100).toInt()}%", RoseExpense, buysPct)
                            Spacer(modifier = Modifier.height(10.dp))
                            CostBarRow("Operating Overhead (Rent/Utilities)", currencyFormat.format(monthlySummary.totalExpenses - monthlySummary.totalExpansionExpenses), "${(overheadPct * 100).toInt()}%", AmberWarning, overheadPct)
                            Spacer(modifier = Modifier.height(10.dp))
                            CostBarRow("Capital Expansion & Assets", currencyFormat.format(monthlySummary.totalExpansionExpenses), "${(expansionPct * 100).toInt()}%", PurpleAccent, expansionPct)
                            Spacer(modifier = Modifier.height(10.dp))
                            CostBarRow("Staff Labor & Holiday Payouts", currencyFormat.format(monthlySummary.totalLaborPaid), "${(laborPct * 100).toInt()}%", BrandBlue, laborPct)
                        }
                    }
                }

                // 3. Daily Cost Comparison
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Today's Daily Totals", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Daily Sales:")
                                Text(currencyFormat.format(dailySummary.totalSales), fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Daily Cost:")
                                Text(currencyFormat.format(dailySummary.totalCost), fontWeight = FontWeight.Bold, color = RoseExpense)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Daily Net Profit:")
                                Text(currencyFormat.format(dailySummary.netProfit), fontWeight = FontWeight.ExtraBold, color = if (dailySummary.netProfit >= 0) EmeraldGreen else RoseExpense)
                            }
                        }
                    }
                }
            } else if (selectedReportTab == 1) {
                // Workforce Analytics
                val completed = allTasks.count { it.status == "COMPLETED" }
                val late = allTasks.count { it.status == "LATE" }
                val due = allTasks.count { it.status == "DUE" }
                val totalTasks = allTasks.size.coerceAtLeast(1)
                val completionRate = (completed.toFloat() / totalTasks.toFloat()) * 100

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Workforce Performance Overview", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Work Completion Rate:")
                                Text("${completionRate.toInt()}%", fontWeight = FontWeight.ExtraBold, color = EmeraldGreen)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Completed Tasks:")
                                Text("$completed", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Overdue / Late Tasks:")
                                Text("$late", fontWeight = FontWeight.Bold, color = RoseExpense)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Active Due Tasks:")
                                Text("$due", fontWeight = FontWeight.Bold, color = AmberWarning)
                            }
                        }
                    }
                }
            } else {
                // Department Performance
                val departments = allUsers.map { it.department }.distinct()
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Active Business Departments (${departments.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            departments.forEach { dept ->
                                val staffInDept = allUsers.count { it.department == dept }
                                val tasksInDept = allTasks.count { it.department == dept }
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(dept, fontWeight = FontWeight.Bold)
                                        Text("$staffInDept staff • $tasksInDept active tasks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    StatusBadge(text = "Operational", containerColor = EmeraldContainer, contentColor = EmeraldGreen)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CostBarRow(label: String, amount: String, percentage: String, color: Color, progress: Float) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "$amount ($percentage)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
