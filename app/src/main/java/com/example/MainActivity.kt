package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.AddBuyDialog
import com.example.ui.components.AddExpenseDialog
import com.example.ui.components.AddHolidayDialog
import com.example.ui.components.AddSaleDialog
import com.example.ui.components.AddUserDialog
import com.example.ui.components.AssignTaskDialog
import com.example.ui.components.BizTopBar
import com.example.ui.components.RequestPayDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BuysScreen
import com.example.ui.screens.EmployeeDashboardScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.HolidayManagementScreen
import com.example.ui.screens.ManagerDashboardScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UserManagementScreen
import com.example.ui.screens.WorkManagementScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.BizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BizOpsApp()
            }
        }
    }
}

@Composable
fun BizOpsApp(
    authViewModel: AuthViewModel = viewModel(),
    bizViewModel: BizViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val authError by authViewModel.authError.collectAsStateWithLifecycle()
    val authLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val allUsers by authViewModel.allUsers.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf("DASHBOARD") }

    // Sync auth session to business operations
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            bizViewModel.switchUser(user)
            currentScreen = "DASHBOARD"
        }
    }

    val allSales by bizViewModel.allSales.collectAsStateWithLifecycle()
    val allBuys by bizViewModel.allBuys.collectAsStateWithLifecycle()
    val allExpenses by bizViewModel.allExpenses.collectAsStateWithLifecycle()
    val allTasks by bizViewModel.allTasks.collectAsStateWithLifecycle()
    val allPayRequests by bizViewModel.allPayRequests.collectAsStateWithLifecycle()
    val allHolidays by bizViewModel.allHolidays.collectAsStateWithLifecycle()
    val recentAuditLogs by bizViewModel.recentAuditLogs.collectAsStateWithLifecycle()
    val allNotifications by bizViewModel.allNotifications.collectAsStateWithLifecycle()
    val businessSettings by bizViewModel.businessSettings.collectAsStateWithLifecycle()

    val dailySummary by bizViewModel.dailySummary.collectAsStateWithLifecycle()
    val monthlySummary by bizViewModel.monthlySummary.collectAsStateWithLifecycle()
    val userEarnings by bizViewModel.currentUserEarnings.collectAsStateWithLifecycle()
    val exportMessage by bizViewModel.exportMessage.collectAsStateWithLifecycle()

    val unreadNotifCount = allNotifications.count { !it.isRead }

    // Dialog states
    var showAddSaleDialog by remember { mutableStateOf(false) }
    var showAddBuyDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAssignTaskDialog by remember { mutableStateOf(false) }
    var showRequestPayDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var showAddHolidayDialog by remember { mutableStateOf(false) }

    // Dialogs
    if (showAddSaleDialog) {
        AddSaleDialog(
            onDismiss = { showAddSaleDialog = false },
            onConfirm = { title, customer, qty, price, method, category, dept, notes ->
                bizViewModel.addSale(title, customer, qty, price, method, category, dept, notes)
            }
        )
    }

    if (showAddBuyDialog) {
        AddBuyDialog(
            onDismiss = { showAddBuyDialog = false },
            onConfirm = { title, supplier, qty, price, category, dept, method, notes ->
                bizViewModel.addBuy(title, supplier, qty, price, category, dept, method, notes)
            }
        )
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { title, category, amount, isExpansion, method, notes ->
                bizViewModel.addExpense(title, category, amount, isExpansion, method, notes)
            }
        )
    }

    if (showAssignTaskDialog) {
        AssignTaskDialog(
            staffList = allUsers.filter { it.role != UserRole.SUPER_ADMIN.name },
            onDismiss = { showAssignTaskDialog = false },
            onConfirm = { title, desc, assignedId, dept, priority, units, rate, isHoliday, mult, due, notes ->
                bizViewModel.assignTask(title, desc, assignedId, dept, priority, units, rate, isHoliday, mult, due, notes)
            }
        )
    }

    if (showRequestPayDialog) {
        val maxBalance = userEarnings?.balanceDue ?: 0.0
        RequestPayDialog(
            maxBalanceDue = maxBalance,
            onDismiss = { showRequestPayDialog = false },
            onConfirm = { amount, type, reason ->
                bizViewModel.submitPayRequest(amount, type, reason)
            }
        )
    }

    if (showAddUserDialog) {
        AddUserDialog(
            managers = allUsers.filter { it.role == UserRole.MANAGER.name || it.role == UserRole.ADMIN.name },
            onDismiss = { showAddUserDialog = false },
            onConfirm = { username, fullName, role, pin, email, phone, address, empId, position, dept, payType, rate, holMult, mgrId, perms, notes ->
                bizViewModel.addUser(username, fullName, role, pin, email, phone, address, empId, position, dept, payType, rate, holMult, mgrId, perms, notes)
            }
        )
    }

    if (showAddHolidayDialog) {
        AddHolidayDialog(
            onDismiss = { showAddHolidayDialog = false },
            onConfirm = { name, date, desc, isPaid, dept, mult, bonus ->
                bizViewModel.addHoliday(name, date, desc, isPaid, dept, mult, bonus)
            }
        )
    }

    // Main App Scaffold
    if (currentUser == null || currentScreen == "LOGIN") {
        AuthScreen(
            users = allUsers,
            errorMessage = authError,
            isLoading = authLoading,
            onLogin = { username, pin ->
                authViewModel.signIn(username, pin) { successUser ->
                    bizViewModel.switchUser(successUser)
                    currentScreen = "DASHBOARD"
                }
            },
            onQuickSwitch = { user ->
                authViewModel.signInWithUser(user)
                bizViewModel.switchUser(user)
                currentScreen = "DASHBOARD"
            }
        )
    } else {
        val user = currentUser!!
        val isManagerOrAdmin = bizViewModel.isManagerOrAdmin()
        val isAdmin = bizViewModel.isAdmin()

        val screenTitle = when (currentScreen) {
            "SALES" -> "Daily Sales"
            "BUYS" -> "Daily Purchases"
            "EXPENSES" -> "Expenses & Expansion"
            "WORK_TASKS" -> "Work & Tasks"
            "PAYMENTS" -> "Payroll & Requests"
            "HOLIDAYS" -> "Holidays"
            "AUDIT_LOGS" -> "Audit Logs"
            "NOTIFICATIONS" -> "Notifications"
            "SETTINGS" -> "Settings"
            "USERS" -> "Team & Roles"
            "REPORTS" -> "Cost Analytics"
            else -> "Dashboard"
        }

        Scaffold(
            topBar = {
                BizTopBar(
                    title = screenTitle,
                    currentUser = user,
                    allUsers = allUsers,
                    unreadNotifCount = unreadNotifCount,
                    onNavigate = { currentScreen = it },
                    onSwitchUser = { newUser ->
                        authViewModel.signInWithUser(newUser)
                    },
                    onLogout = {
                        authViewModel.signOut()
                        currentScreen = "LOGIN"
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "SALES" -> SalesScreen(
                        sales = allSales,
                        canManage = isManagerOrAdmin,
                        onBack = { currentScreen = "DASHBOARD" },
                        onOpenAddSale = { showAddSaleDialog = true },
                        onDeleteSale = { bizViewModel.deleteSale(it) }
                    )
                    "BUYS" -> BuysScreen(
                        buys = allBuys,
                        canManage = isManagerOrAdmin,
                        onBack = { currentScreen = "DASHBOARD" },
                        onOpenAddBuy = { showAddBuyDialog = true },
                        onDeleteBuy = { bizViewModel.deleteBuy(it) }
                    )
                    "EXPENSES" -> ExpensesScreen(
                        expenses = allExpenses,
                        canManage = isManagerOrAdmin,
                        onBack = { currentScreen = "DASHBOARD" },
                        onOpenAddExpense = { showAddExpenseDialog = true },
                        onDeleteExpense = { bizViewModel.deleteExpense(it) }
                    )
                    "WORK_TASKS" -> WorkManagementScreen(
                        currentUser = user,
                        tasks = allTasks,
                        allUsers = allUsers,
                        onBack = { currentScreen = "DASHBOARD" },
                        onOpenAssignTask = { showAssignTaskDialog = true },
                        onMarkTaskDone = { bizViewModel.markTaskDone(it) },
                        onDeleteTask = { bizViewModel.deleteTask(it) }
                    )
                    "PAYMENTS" -> PaymentsScreen(
                        currentUser = user,
                        payRequests = allPayRequests,
                        userEarnings = userEarnings,
                        onBack = { currentScreen = "DASHBOARD" },
                        onOpenRequestPay = { showRequestPayDialog = true },
                        onResolvePayRequest = { id, status, note, ref ->
                            bizViewModel.resolvePayRequest(id, status, note, ref)
                        }
                    )
                    "HOLIDAYS" -> HolidayManagementScreen(
                        currentUser = user,
                        holidays = allHolidays,
                        canManage = isManagerOrAdmin,
                        onBack = { currentScreen = "DASHBOARD" },
                        onOpenAddHoliday = { showAddHolidayDialog = true },
                        onDeleteHoliday = { bizViewModel.deleteHoliday(it) }
                    )
                    "AUDIT_LOGS" -> AuditLogsScreen(
                        auditLogs = recentAuditLogs,
                        onBack = { currentScreen = "DASHBOARD" }
                    )
                    "NOTIFICATIONS" -> NotificationsScreen(
                        notifications = allNotifications,
                        onBack = { currentScreen = "DASHBOARD" },
                        onMarkRead = { bizViewModel.markNotificationRead(it) },
                        onMarkAllRead = { bizViewModel.markAllNotificationsRead() }
                    )
                    "SETTINGS" -> SettingsScreen(
                        currentUser = user,
                        currentSettings = businessSettings,
                        onBack = { currentScreen = "DASHBOARD" },
                        onSaveSettings = { bizViewModel.updateBusinessSettings(it) }
                    )
                    "USERS" -> UserManagementScreen(
                        currentUser = user,
                        users = allUsers,
                        onBack = { currentScreen = "DASHBOARD" },
                        onOpenAddUser = { showAddUserDialog = true },
                        onDeleteUser = { bizViewModel.deleteUser(it) }
                    )
                    "REPORTS" -> ReportsScreen(
                        dailySummary = dailySummary,
                        monthlySummary = monthlySummary,
                        allTasks = allTasks,
                        allUsers = allUsers,
                        exportMessage = exportMessage,
                        onExportCsv = { bizViewModel.exportFinancialReportCsv() },
                        onBack = { currentScreen = "DASHBOARD" }
                    )
                    else -> {
                        // Role-specific Dashboards:
                        when {
                            user.role == UserRole.SUPER_ADMIN.name || user.role == UserRole.ADMIN.name -> {
                                AdminDashboardScreen(
                                    currentUser = user,
                                    dailySummary = dailySummary,
                                    monthlySummary = monthlySummary,
                                    userEarnings = userEarnings,
                                    allTasks = allTasks,
                                    allPayRequests = allPayRequests,
                                    onNavigate = { currentScreen = it },
                                    onOpenAddSale = { showAddSaleDialog = true },
                                    onOpenAddBuy = { showAddBuyDialog = true },
                                    onOpenAddExpense = { showAddExpenseDialog = true },
                                    onOpenAssignTask = { showAssignTaskDialog = true },
                                    onOpenRequestPay = { showRequestPayDialog = true },
                                    onSwitchUser = { authViewModel.signInWithUser(it) }
                                )
                            }
                            user.role == UserRole.MANAGER.name -> {
                                ManagerDashboardScreen(
                                    currentUser = user,
                                    allUsers = allUsers,
                                    allTasks = allTasks,
                                    allPayRequests = allPayRequests,
                                    onNavigate = { currentScreen = it },
                                    onOpenAssignTask = { showAssignTaskDialog = true }
                                )
                            }
                            else -> {
                                EmployeeDashboardScreen(
                                    currentUser = user,
                                    userEarnings = userEarnings,
                                    allTasks = allTasks,
                                    allPayRequests = allPayRequests,
                                    onOpenRequestPay = { showRequestPayDialog = true },
                                    onMarkTaskDone = { bizViewModel.markTaskDone(it) },
                                    onNavigate = { currentScreen = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
