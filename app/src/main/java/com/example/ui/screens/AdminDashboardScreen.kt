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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PayRequestEntity
import com.example.data.local.UserEntity
import com.example.data.local.WorkTaskEntity
import com.example.data.model.DailySummary
import com.example.data.model.MonthlySummary
import com.example.data.model.PayRequestStatus
import com.example.data.model.TaskStatus
import com.example.data.model.UserEarningsSummary
import com.example.data.model.UserRole
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandBlueDark
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

/**
 * Initial Dummy Business Metrics for Admin Dashboard aggregation
 */
data class AdminDummyMetrics(
    val totalDailySales: Double = 3450.00,
    val salesGrowthPercent: Double = 14.8,
    val totalDailyCosts: Double = 1820.00,
    val dailyBuysCost: Double = 980.00,
    val dailyOperatingExpenses: Double = 420.00,
    val dailyExpansionCost: Double = 250.00,
    val dailyLaborCost: Double = 170.00,
    val netDailyProfit: Double = 1630.00,
    val profitMarginPercent: Double = 47.2,
    val totalMonthlySales: Double = 42800.00,
    val totalMonthlyCost: Double = 22450.00,
    val monthlyNetProfit: Double = 20350.00,
    val pendingPayRequestsCount: Int = 3,
    val pendingPayAmount: Double = 410.00,
    val lateTasksCount: Int = 2,
    val activeStaffCount: Int = 4,
    val topSellingCategories: List<Pair<String, Double>> = listOf(
        "Commercial Assemblies" to 1650.00,
        "Custom Client Contracts" to 980.00,
        "Retail Component Packs" to 520.00,
        "Refurbishment Services" to 300.00
    )
)

@Composable
fun AdminDashboardScreen(
    currentUser: UserEntity,
    dailySummary: DailySummary,
    monthlySummary: MonthlySummary,
    userEarnings: UserEarningsSummary?,
    allTasks: List<WorkTaskEntity>,
    allPayRequests: List<PayRequestEntity>,
    onNavigate: (String) -> Unit,
    onOpenAddSale: () -> Unit,
    onOpenAddBuy: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onOpenAssignTask: () -> Unit,
    onOpenRequestPay: () -> Unit,
    onSwitchUser: ((UserEntity) -> Unit)? = null
) {
    // ------------------------------------------------------------------------
    // STRICT ROLE ACCESS CONTROL: ONLY ACCESSIBLE BY 'ADMIN' ROLE
    // ------------------------------------------------------------------------
    val isAdmin = currentUser.role.equals(UserRole.ADMIN.name, ignoreCase = true) ||
            currentUser.role.equals(UserRole.SUPER_ADMIN.name, ignoreCase = true)
    if (!isAdmin) {
        AdminAccessRestrictedScreen(
            currentUser = currentUser,
            onNavigateToAllowed = { onNavigate("WORK_TASKS") },
            onSwitchToAdmin = {
                // Return to allowed screen or trigger demo switch
                onNavigate("WORK_TASKS")
            }
        )
        return
    }

    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dummyMetrics = remember { AdminDummyMetrics() }

    // Toggle between Aggregated Dummy Data (initial preview) and Live Database Records
    var useDummyData by remember { mutableStateOf(true) }

    val activeDailySales = if (useDummyData) dummyMetrics.totalDailySales else dailySummary.totalSales
    val activeDailyCosts = if (useDummyData) dummyMetrics.totalDailyCosts else dailySummary.totalCost
    val activeDailyBuys = if (useDummyData) dummyMetrics.dailyBuysCost else dailySummary.totalBuys
    val activeDailyExpenses = if (useDummyData) dummyMetrics.dailyOperatingExpenses + dummyMetrics.dailyExpansionCost else dailySummary.totalExpenses
    val activeDailyLabor = if (useDummyData) dummyMetrics.dailyLaborCost else dailySummary.totalLaborCost
    val activeDailyProfit = if (useDummyData) dummyMetrics.netDailyProfit else dailySummary.netProfit

    val activeMonthlySales = if (useDummyData) dummyMetrics.totalMonthlySales else monthlySummary.totalSales
    val activeMonthlyCost = if (useDummyData) dummyMetrics.totalMonthlyCost else monthlySummary.totalCost
    val activeMonthlyProfit = if (useDummyData) dummyMetrics.monthlyNetProfit else monthlySummary.netProfit

    val pendingRequestsCount = if (useDummyData) dummyMetrics.pendingPayRequestsCount else allPayRequests.count { it.status == PayRequestStatus.PENDING.name }
    val lateTasksCount = if (useDummyData) dummyMetrics.lateTasksCount else allTasks.count { it.status == TaskStatus.LATE.name }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Admin Header with Security & Access Verification
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_header_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BrandBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Admin Executive Dashboard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Authenticated: ${currentUser.fullName} • Role: ${currentUser.role}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        StatusBadge(
                            text = "Admin Access Verified",
                            containerColor = BrandBlue,
                            contentColor = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BrandBlue.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Data Source Selector (Dummy Data vs Live Database)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Metrics Mode:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = useDummyData,
                                onClick = { useDummyData = true },
                                label = { Text("Dummy Metrics", fontSize = 11.sp) },
                                modifier = Modifier.testTag("mode_dummy_data")
                            )
                            FilterChip(
                                selected = !useDummyData,
                                onClick = { useDummyData = false },
                                label = { Text("Live Data", fontSize = 11.sp) },
                                modifier = Modifier.testTag("mode_live_data")
                            )
                        }
                    }
                }
            }
        }

        // 2. Urgent Attention Alerts (Pending Pay Requests / Late Tasks)
        if (pendingRequestsCount > 0 || lateTasksCount > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (pendingRequestsCount > 0) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("PAYMENTS") }
                                .testTag("admin_alert_pending_requests"),
                            colors = CardDefaults.cardColors(containerColor = AmberContainer)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("$pendingRequestsCount Pay Requests", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberWarning)
                                    Text("Requires Admin approval", fontSize = 11.sp, color = Slate700)
                                }
                            }
                        }
                    }

                    if (lateTasksCount > 0) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate("WORK_TASKS") }
                                .testTag("admin_alert_late_tasks"),
                            colors = CardDefaults.cardColors(containerColor = RoseContainer)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = RoseExpense, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("$lateTasksCount Late Tasks", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoseExpense)
                                    Text("Staff overdue work", fontSize = 11.sp, color = Slate700)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Primary Business Metrics (Total Daily Sales & Total Daily Costs)
        item {
            Text(
                text = if (useDummyData) "Aggregated Business Metrics (Sample)" else "Today's Live Business Performance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total Daily Sales",
                    value = currencyFormat.format(activeDailySales),
                    subtitle = if (useDummyData) "+${dummyMetrics.salesGrowthPercent}% vs yesterday" else "Recorded customer sales",
                    icon = Icons.Default.TrendingUp,
                    iconBgColor = EmeraldContainer,
                    iconTintColor = EmeraldGreen,
                    modifier = Modifier.weight(1f),
                    testTag = "admin_total_daily_sales"
                )
                MetricCard(
                    title = "Total Daily Costs",
                    value = currencyFormat.format(activeDailyCosts),
                    subtitle = "Buys + Overhead + Labor",
                    icon = Icons.Default.TrendingDown,
                    iconBgColor = RoseContainer,
                    iconTintColor = RoseExpense,
                    modifier = Modifier.weight(1f),
                    testTag = "admin_total_daily_costs"
                )
            }
        }

        // 4. Daily Profit Margin Banner
        item {
            val isProfit = activeDailyProfit >= 0
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_net_daily_profit_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isProfit) EmeraldContainer.copy(alpha = 0.8f) else RoseContainer.copy(alpha = 0.8f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Net Daily Profit (Sales - Costs)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate700
                        )
                        Text(
                            text = currencyFormat.format(activeDailyProfit),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isProfit) EmeraldGreen else RoseExpense
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(
                            text = if (isProfit) "Profitable" else "Loss",
                            containerColor = if (isProfit) EmeraldGreen else RoseExpense,
                            contentColor = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Margin: ${String.format(Locale.US, "%.1f", (activeDailyProfit / activeDailySales.coerceAtLeast(1.0)) * 100)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                    }
                }
            }
        }

        // 5. Daily Cost Breakdown Aggregation
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_cost_breakdown_card"),
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
                        Text(
                            text = "Daily Cost Breakdown",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Total: ${currencyFormat.format(activeDailyCosts)}",
                            fontWeight = FontWeight.Bold,
                            color = RoseExpense,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val totalCostsSafe = activeDailyCosts.coerceAtLeast(1.0)
                    val buysRatio = (activeDailyBuys / totalCostsSafe).toFloat().coerceIn(0f, 1f)
                    val expRatio = (activeDailyExpenses / totalCostsSafe).toFloat().coerceIn(0f, 1f)
                    val laborRatio = (activeDailyLabor / totalCostsSafe).toFloat().coerceIn(0f, 1f)

                    CostProgressBar("Daily Purchases (Raw materials/Inventory)", currencyFormat.format(activeDailyBuys), buysRatio, RoseExpense)
                    Spacer(modifier = Modifier.height(10.dp))
                    CostProgressBar("Daily Overhead & Expansion", currencyFormat.format(activeDailyExpenses), expRatio, AmberWarning)
                    Spacer(modifier = Modifier.height(10.dp))
                    CostProgressBar("Daily Staff Labor & Holiday Cost", currencyFormat.format(activeDailyLabor), laborRatio, BrandBlue)
                }
            }
        }

        // 6. Aggregated Category Sales Breakdown (Dummy Data Feature)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("admin_category_sales_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PieChart, contentDescription = null, tint = EmeraldGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Top Sales Segments (Aggregated)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    dummyMetrics.topSellingCategories.forEach { (cat, amount) ->
                        val ratio = (amount / dummyMetrics.totalDailySales).toFloat().coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(
                                text = currencyFormat.format(amount),
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen,
                                fontSize = 14.sp
                            )
                        }
                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = EmeraldGreen,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // 7. Monthly Aggregated Financial Overview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate("REPORTS") }
                    .testTag("admin_monthly_projection_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = BrandBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Monthly Aggregated Projection",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Monthly Sales", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormat.format(activeMonthlySales), fontWeight = FontWeight.Bold, color = EmeraldGreen, fontSize = 16.sp)
                        }
                        Column {
                            Text("Monthly Costs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormat.format(activeMonthlyCost), fontWeight = FontWeight.Bold, color = RoseExpense, fontSize = 16.sp)
                        }
                        Column {
                            Text("Projected Net", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(currencyFormat.format(activeMonthlyProfit), fontWeight = FontWeight.Bold, color = EmeraldGreen, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // 8. Admin Quick Actions
        item {
            Text("Admin Quick Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onOpenAddSale,
                    modifier = Modifier.weight(1f).testTag("admin_quick_sale"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sale", fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenAddBuy,
                    modifier = Modifier.weight(1f).testTag("admin_quick_buy"),
                    colors = ButtonDefaults.buttonColors(containerColor = RoseExpense),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Buy", fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenAddExpense,
                    modifier = Modifier.weight(1f).testTag("admin_quick_expense"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Expense", fontSize = 12.sp)
                }

                Button(
                    onClick = onOpenAssignTask,
                    modifier = Modifier.weight(1f).testTag("admin_quick_assign_task"),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Task", fontSize = 12.sp)
                }
            }
        }

        // 9. Business Modules Hub
        item {
            Text("Business Segments Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AdminSegmentRow("Daily Sales Management", "Record sales, transactions & receipts", Icons.Default.ShoppingCart, EmeraldContainer, EmeraldGreen) { onNavigate("SALES") }
                AdminSegmentRow("Daily Purchases & Buys", "Track inventory, supplier costs & buys", Icons.Default.ShoppingBag, RoseContainer, RoseExpense) { onNavigate("BUYS") }
                AdminSegmentRow("Expenses & Expansion", "Overheads, equipment & expansion capital", Icons.Default.AttachMoney, PurpleContainer, PurpleAccent) { onNavigate("EXPENSES") }
                AdminSegmentRow("Work & Staff Task Management", "Due work, late work, done work & holiday rates", Icons.Default.Schedule, AmberContainer, AmberWarning) { onNavigate("WORK_TASKS") }
                AdminSegmentRow("Payroll & Pay Requests", "Staff balances, review requests & payouts", Icons.Default.Payments, EmeraldContainer, EmeraldGreen) { onNavigate("PAYMENTS") }
                AdminSegmentRow("User & Role Access Management", "Add managers, staff & segment permissions", Icons.Default.Groups, MaterialTheme.colorScheme.primaryContainer, BrandBlue) { onNavigate("USERS") }
                AdminSegmentRow("Cost & Profit Analytics Reports", "Detailed daily vs monthly comparison", Icons.Default.Assessment, Slate200, Slate700) { onNavigate("REPORTS") }
            }
        }
    }
}

/**
 * Access Denied Guard Screen displayed whenever non-admin users attempt to access the Admin Dashboard.
 */
@Composable
fun AdminAccessRestrictedScreen(
    currentUser: UserEntity,
    onNavigateToAllowed: () -> Unit,
    onSwitchToAdmin: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize().testTag("admin_access_restricted_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(RoseContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Access Denied",
                    tint = RoseExpense,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Access Restricted",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The Admin Dashboard aggregates sensitive business financial metrics (daily sales, costs, payroll, and profit margins).",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Current User: ${currentUser.fullName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Role: ${currentUser.role}", fontSize = 13.sp, color = AmberWarning, fontWeight = FontWeight.SemiBold)
                    Text("Required Role: ADMIN", fontSize = 13.sp, color = RoseExpense, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onNavigateToAllowed,
                modifier = Modifier.fillMaxWidth().testTag("btn_go_to_allowed_segment"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Go to My Permitted Segment (Work & Tasks)")
            }
        }
    }
}

@Composable
private fun CostProgressBar(label: String, amount: String, ratio: Float, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Slate700)
            Text(amount, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun AdminSegmentRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTintColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
            }

            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}
