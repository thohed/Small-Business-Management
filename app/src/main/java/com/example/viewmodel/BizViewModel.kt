package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AuditLogEntity
import com.example.data.local.BusinessSettingsEntity
import com.example.data.local.BuyEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.HolidayEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.PayRequestEntity
import com.example.data.local.SaleEntity
import com.example.data.local.UserEntity
import com.example.data.local.WorkTaskEntity
import com.example.data.model.AppPermission
import com.example.data.model.BusinessSegment
import com.example.data.model.DailySummary
import com.example.data.model.DateFilterRange
import com.example.data.model.MonthlySummary
import com.example.data.model.PayRequestStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.UserEarningsSummary
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.data.repository.BizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BizViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BizRepository

    // Auth Session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow("DASHBOARD")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateFilterRange.TODAY)
    val dateFilter: StateFlow<DateFilterRange> = _dateFilter.asStateFlow()

    // Export Feedback
    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = BizRepository(db)

        // Default login as Super Admin Eleanor Vance
        viewModelScope.launch {
            repository.allUsers.collect { users ->
                if (_currentUser.value == null && users.isNotEmpty()) {
                    val superAdmin = users.find { it.role == UserRole.SUPER_ADMIN.name }
                        ?: users.find { it.role == UserRole.ADMIN.name }
                        ?: users.first()
                    _currentUser.value = superAdmin
                }
            }
        }
    }

    // Exposed Reactive StateFlows
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSales: StateFlow<List<SaleEntity>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBuys: StateFlow<List<BuyEntity>> = repository.allBuys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<WorkTaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPayRequests: StateFlow<List<PayRequestEntity>> = repository.allPayRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHolidays: StateFlow<List<HolidayEntity>> = repository.allHolidays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAuditLogs: StateFlow<List<AuditLogEntity>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessSettings: StateFlow<BusinessSettingsEntity?> = repository.businessSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Daily Summary Calculation
    val dailySummary: StateFlow<DailySummary> = combine(
        allSales,
        allBuys,
        allExpenses,
        allTasks,
        allHolidays
    ) { sales, buys, expenses, tasks, holidays ->
        val (startOfDay, endOfDay) = getTodayRange()

        val todaySales = sales.filter { it.dateMillis in startOfDay..endOfDay }.sumOf { it.amount }
        val todayBuys = buys.filter { it.dateMillis in startOfDay..endOfDay }.sumOf { it.amount }

        val todayExpensesList = expenses.filter { it.dateMillis in startOfDay..endOfDay }
        val todayExpenses = todayExpensesList.sumOf { it.amount }
        val todayExpansion = todayExpensesList.filter { it.isExpansion }.sumOf { it.amount }

        // Labor cost for work completed today
        val todayLaborCost = tasks.filter {
            it.status == TaskStatus.COMPLETED.name &&
            (it.completedDateMillis ?: 0L) in startOfDay..endOfDay
        }.sumOf { it.earnedPay }

        // Holiday compensation cost
        val todayHolidayCost = holidays.filter { it.dateMillis in startOfDay..endOfDay && it.isPaid }.sumOf { it.flatPayBonus }

        val totalCost = todayBuys + todayExpenses + todayLaborCost + todayHolidayCost
        val netProfit = todaySales - totalCost

        DailySummary(
            dateMillis = startOfDay,
            totalSales = todaySales,
            totalBuys = todayBuys,
            totalExpenses = todayExpenses,
            totalExpansionExpenses = todayExpansion,
            totalLaborCost = todayLaborCost,
            totalHolidayCost = todayHolidayCost,
            totalCost = totalCost,
            netProfit = netProfit
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DailySummary(0L, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    // Monthly Summary Calculation
    val monthlySummary: StateFlow<MonthlySummary> = combine(
        allSales,
        allBuys,
        allExpenses,
        allPayRequests,
        allHolidays
    ) { sales, buys, expenses, payRequests, holidays ->
        val (startOfMonth, endOfMonth) = getMonthRange()
        val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(startOfMonth))

        val monthSales = sales.filter { it.dateMillis in startOfMonth..endOfMonth }.sumOf { it.amount }
        val monthBuys = buys.filter { it.dateMillis in startOfMonth..endOfMonth }.sumOf { it.amount }

        val monthExpensesList = expenses.filter { it.dateMillis in startOfMonth..endOfMonth }
        val monthExpenses = monthExpensesList.sumOf { it.amount }
        val monthExpansion = monthExpensesList.filter { it.isExpansion }.sumOf { it.amount }

        val monthLaborPaid = payRequests.filter {
            it.status == PayRequestStatus.PAID.name &&
            (it.reviewedDateMillis ?: it.requestDateMillis) in startOfMonth..endOfMonth
        }.sumOf { it.amount }

        val monthHolidayCost = holidays.filter { it.dateMillis in startOfMonth..endOfMonth && it.isPaid }.sumOf { it.flatPayBonus }

        val totalCost = monthBuys + monthExpenses + monthLaborPaid + monthHolidayCost
        val netProfit = monthSales - totalCost

        MonthlySummary(
            yearMonth = monthLabel,
            totalSales = monthSales,
            totalBuys = monthBuys,
            totalExpenses = monthExpenses,
            totalExpansionExpenses = monthExpansion,
            totalLaborPaid = monthLaborPaid,
            totalHolidayCost = monthHolidayCost,
            totalCost = totalCost,
            netProfit = netProfit
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MonthlySummary("", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )

    // Current User Personal Earnings & Work Status
    val currentUserEarnings: StateFlow<UserEarningsSummary?> = combine(
        _currentUser,
        allTasks,
        allPayRequests
    ) { user, tasks, payRequests ->
        if (user == null) return@combine null

        val userTasks = tasks.filter { it.assignedUserId == user.id }
        val now = System.currentTimeMillis()

        val completedTasks = userTasks.filter { it.status == TaskStatus.COMPLETED.name }
        val dueTasks = userTasks.filter {
            (it.status == TaskStatus.DUE.name || it.status == TaskStatus.IN_PROGRESS.name || it.status == TaskStatus.PENDING.name) &&
            it.dueDateMillis >= now
        }
        val lateTasks = userTasks.filter {
            it.status == TaskStatus.LATE.name ||
            (it.status != TaskStatus.COMPLETED.name && it.status != TaskStatus.CANCELLED.name && it.dueDateMillis < now)
        }

        val workBasedEarnings = completedTasks.sumOf { it.earnedPay }
        val holidayBonus = completedTasks.filter { it.isHoliday }.sumOf {
            it.earnedPay - (it.workUnits * it.baseRate)
        }

        val userPayRequests = payRequests.filter { it.userId == user.id }
        val totalPaidOut = userPayRequests.filter { it.status == PayRequestStatus.PAID.name }.sumOf { it.amount }
        val pendingRequests = userPayRequests.filter { it.status == PayRequestStatus.PENDING.name }.sumOf { it.amount }

        val grossApproved = workBasedEarnings + holidayBonus + user.bonusAmount - user.deductionAmount
        val balanceDue = (grossApproved - totalPaidOut).coerceAtLeast(0.0)

        UserEarningsSummary(
            userId = user.id,
            totalCompletedTasks = completedTasks.size,
            totalDueTasks = dueTasks.size,
            totalLateTasks = lateTasks.size,
            workBasedEarnings = workBasedEarnings,
            holidayEarnings = holidayBonus,
            bonusAmount = user.bonusAmount,
            deductionAmount = user.deductionAmount,
            grossApprovedEarnings = grossApproved,
            totalPaidOut = totalPaidOut,
            pendingRequestAmount = pendingRequests,
            balanceDue = balanceDue
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Navigation and Session
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
    }

    fun setDateFilter(range: DateFilterRange) {
        _dateFilter.value = range
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }

    // Role & Permission Checks
    fun isSuperAdmin(): Boolean = _currentUser.value?.role == UserRole.SUPER_ADMIN.name
    fun isAdmin(): Boolean = _currentUser.value?.let { it.role == UserRole.SUPER_ADMIN.name || it.role == UserRole.ADMIN.name } ?: false
    fun isManager(): Boolean = _currentUser.value?.role == UserRole.MANAGER.name
    fun isStaff(): Boolean = _currentUser.value?.role == UserRole.STAFF.name
    fun isEmployee(): Boolean = _currentUser.value?.role == UserRole.EMPLOYEE.name
    fun isManagerOrAdmin(): Boolean = isAdmin() || isManager()

    fun hasPermission(permission: AppPermission): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role == UserRole.SUPER_ADMIN.name || user.role == UserRole.ADMIN.name) return true
        val allowed = user.allowedSegments.split(",").map { it.trim().uppercase() }
        return allowed.contains(permission.code)
    }

    // Sales Actions
    fun addSale(
        title: String,
        customerName: String,
        quantity: Double,
        unitPrice: Double,
        paymentMethod: String,
        category: String,
        department: String,
        notes: String
    ) {
        val user = _currentUser.value ?: return
        val totalAmount = quantity * unitPrice
        viewModelScope.launch {
            repository.insertSale(
                SaleEntity(
                    title = title.trim(),
                    customerName = customerName.trim().ifEmpty { "Walk-in Customer" },
                    quantity = quantity,
                    unitPrice = unitPrice,
                    amount = totalAmount,
                    paymentMethod = paymentMethod,
                    category = category.trim().ifEmpty { "General Sales" },
                    department = department.trim().ifEmpty { "Sales" },
                    notes = notes.trim(),
                    dateMillis = System.currentTimeMillis(),
                    recordedByUserId = user.id,
                    recordedByName = user.fullName
                )
            )
        }
    }

    fun deleteSale(id: Long) {
        val user = _currentUser.value ?: return
        if (!isManagerOrAdmin()) return
        viewModelScope.launch {
            repository.deleteSale(id)
            repository.logAudit(user.id, user.fullName, user.role, "SALE_DELETED", "Sale", id, details = "Deleted sale #$id")
        }
    }

    // Buys (Purchases) Actions
    fun addBuy(
        title: String,
        supplierName: String,
        quantity: Double,
        unitPrice: Double,
        category: String,
        department: String,
        paymentMethod: String,
        notes: String
    ) {
        val user = _currentUser.value ?: return
        val totalAmount = quantity * unitPrice
        viewModelScope.launch {
            repository.insertBuy(
                BuyEntity(
                    title = title.trim(),
                    supplierName = supplierName.trim().ifEmpty { "General Supplier" },
                    quantity = quantity,
                    unitPrice = unitPrice,
                    amount = totalAmount,
                    category = category.trim().ifEmpty { "Inventory" },
                    department = department.trim().ifEmpty { "Procurement" },
                    paymentMethod = paymentMethod,
                    notes = notes.trim(),
                    dateMillis = System.currentTimeMillis(),
                    recordedByUserId = user.id,
                    recordedByName = user.fullName
                )
            )
        }
    }

    fun deleteBuy(id: Long) {
        val user = _currentUser.value ?: return
        if (!isManagerOrAdmin()) return
        viewModelScope.launch {
            repository.deleteBuy(id)
            repository.logAudit(user.id, user.fullName, user.role, "BUY_DELETED", "Buy", id, details = "Deleted purchase record #$id")
        }
    }

    // Expenses Actions
    fun addExpense(
        title: String,
        category: String,
        amount: Double,
        isExpansion: Boolean,
        paymentMethod: String,
        notes: String
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.insertExpense(
                ExpenseEntity(
                    title = title.trim(),
                    category = category.trim(),
                    amount = amount,
                    isExpansion = isExpansion,
                    paymentMethod = paymentMethod,
                    notes = notes.trim(),
                    dateMillis = System.currentTimeMillis(),
                    recordedByUserId = user.id,
                    recordedByName = user.fullName
                )
            )
        }
    }

    fun deleteExpense(id: Long) {
        val user = _currentUser.value ?: return
        if (!isManagerOrAdmin()) return
        viewModelScope.launch {
            repository.deleteExpense(id)
            repository.logAudit(user.id, user.fullName, user.role, "EXPENSE_DELETED", "Expense", id, details = "Deleted expense record #$id")
        }
    }

    // Work Tasks Actions
    fun assignTask(
        title: String,
        description: String,
        assignedUserId: Long,
        department: String,
        priority: String,
        workUnits: Double,
        baseRate: Double,
        isHoliday: Boolean,
        holidayMultiplier: Double,
        dueDateMillis: Long,
        notes: String
    ) {
        val creator = _currentUser.value ?: return
        val assignedUser = allUsers.value.find { it.id == assignedUserId }
        val userName = assignedUser?.fullName ?: "Staff #$assignedUserId"

        val multiplier = if (isHoliday) holidayMultiplier else 1.0
        val earnedPay = workUnits * baseRate * multiplier

        val task = WorkTaskEntity(
            title = title.trim(),
            description = description.trim(),
            assignedUserId = assignedUserId,
            assignedUserName = userName,
            assignedManagerId = creator.id,
            department = department,
            priority = priority,
            status = TaskStatus.DUE.name,
            workUnits = workUnits,
            baseRate = baseRate,
            isHoliday = isHoliday,
            holidayMultiplier = holidayMultiplier,
            earnedPay = earnedPay,
            dueDateMillis = dueDateMillis,
            createdByUserId = creator.id,
            notes = notes.trim()
        )

        viewModelScope.launch {
            val id = repository.insertTask(task)
            repository.logAudit(
                creator.id, creator.fullName, creator.role, "WORK_ASSIGNED", "WorkTask", id,
                details = "Assigned '${task.title}' to $userName ($${task.earnedPay})"
            )
        }
    }

    fun markTaskDone(taskId: Long, notes: String = "") {
        val actor = _currentUser.value ?: return
        viewModelScope.launch {
            repository.markTaskCompleted(taskId, actor, notes)
        }
    }

    fun deleteTask(id: Long) {
        val user = _currentUser.value ?: return
        if (!isManagerOrAdmin()) return
        viewModelScope.launch {
            repository.deleteTask(id)
            repository.logAudit(user.id, user.fullName, user.role, "WORK_DELETED", "WorkTask", id, details = "Deleted task #$id")
        }
    }

    // Pay Requests Actions
    fun submitPayRequest(
        amount: Double,
        requestType: String,
        reason: String,
        relatedTaskId: Long? = null
    ) {
        val user = _currentUser.value ?: return
        val request = PayRequestEntity(
            userId = user.id,
            userName = user.fullName,
            amount = amount,
            requestType = requestType,
            status = PayRequestStatus.PENDING.name,
            reason = reason.trim(),
            relatedTaskId = relatedTaskId,
            requestDateMillis = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.insertPayRequest(request)
        }
    }

    fun resolvePayRequest(
        requestId: Long,
        newStatus: PayRequestStatus,
        adminNote: String = "",
        paymentRef: String = ""
    ) {
        val actor = _currentUser.value ?: return
        if (!isManagerOrAdmin()) return
        viewModelScope.launch {
            repository.updatePayRequestStatus(requestId, newStatus.name, actor, adminNote, paymentRef)
        }
    }

    // User & Team Management Actions
    fun addUser(
        username: String,
        fullName: String,
        role: UserRole,
        pin: String,
        email: String,
        phone: String,
        address: String,
        employeeId: String,
        jobPosition: String,
        department: String,
        paymentType: String,
        perWorkRate: Double,
        holidayRateMultiplier: Double,
        assignedManagerId: Long?,
        allowedPermissions: List<AppPermission>,
        notes: String
    ) {
        val actor = _currentUser.value ?: return
        if (!isAdmin()) return

        val managerName = allUsers.value.find { it.id == assignedManagerId }?.fullName ?: ""
        val permissionsStr = allowedPermissions.joinToString(",") { it.code }

        val newUser = UserEntity(
            username = username.trim().lowercase(),
            fullName = fullName.trim(),
            role = role.name,
            pin = pin.trim().ifEmpty { "1234" },
            email = email.trim(),
            phone = phone.trim(),
            address = address.trim(),
            employeeId = employeeId.trim().ifEmpty { "EMP-${(100..999).random()}" },
            jobPosition = jobPosition.trim(),
            department = department.trim(),
            paymentType = paymentType,
            perWorkRate = perWorkRate,
            holidayRateMultiplier = holidayRateMultiplier,
            assignedManagerId = assignedManagerId,
            assignedManagerName = managerName,
            userStatus = UserStatus.ACTIVE.name,
            allowedSegments = permissionsStr,
            notes = notes.trim()
        )

        viewModelScope.launch {
            repository.insertUser(newUser)
        }
    }

    fun updateUserStatus(userId: Long, newStatus: UserStatus) {
        val actor = _currentUser.value ?: return
        if (!isAdmin()) return
        viewModelScope.launch {
            repository.updateUserStatus(userId, newStatus.name, actor)
        }
    }

    fun resetUserPin(userId: Long, newPin: String) {
        val actor = _currentUser.value ?: return
        if (!isAdmin()) return
        viewModelScope.launch {
            repository.resetUserPin(userId, newPin, actor)
        }
    }

    fun addBonusOrDeduction(userId: Long, bonus: Double, deduction: Double) {
        val actor = _currentUser.value ?: return
        if (!isAdmin()) return
        viewModelScope.launch {
            val user = repository.getUserByIdOnce(userId) ?: return@launch
            val updated = user.copy(
                bonusAmount = user.bonusAmount + bonus,
                deductionAmount = user.deductionAmount + deduction
            )
            repository.updateUser(updated)
            repository.logAudit(
                actor.id, actor.fullName, actor.role, "FINANCIAL_ADJUSTMENT", "User", userId,
                details = "Adjusted ${user.fullName}: +$bonus bonus, -$deduction deduction by ${actor.fullName}"
            )
        }
    }

    fun deleteUser(id: Long) {
        val actor = _currentUser.value ?: return
        if (!isSuperAdmin()) return
        viewModelScope.launch {
            repository.deleteUser(id)
            repository.logAudit(actor.id, actor.fullName, actor.role, "USER_DELETED", "User", id, details = "Deleted user #$id")
        }
    }

    // Holiday Management Actions
    fun addHoliday(
        name: String,
        dateMillis: Long,
        description: String,
        isPaid: Boolean,
        department: String,
        holidayRateMultiplier: Double,
        flatPayBonus: Double
    ) {
        val actor = _currentUser.value ?: return
        if (!isManagerOrAdmin()) return

        val holiday = HolidayEntity(
            name = name.trim(),
            dateMillis = dateMillis,
            description = description.trim(),
            isPaid = isPaid,
            applicableDepartment = department,
            holidayRateMultiplier = holidayRateMultiplier,
            flatPayBonus = flatPayBonus
        )
        viewModelScope.launch {
            val id = repository.insertHoliday(holiday)
            repository.logAudit(
                actor.id, actor.fullName, actor.role, "HOLIDAY_CREATED", "Holiday", id,
                details = "Created holiday '${holiday.name}' (Paid: $isPaid, Bonus: $flatPayBonus)"
            )
        }
    }

    fun deleteHoliday(id: Long) {
        val actor = _currentUser.value ?: return
        if (!isManagerOrAdmin()) return
        viewModelScope.launch {
            repository.deleteHoliday(id)
            repository.logAudit(actor.id, actor.fullName, actor.role, "HOLIDAY_DELETED", "Holiday", id, details = "Deleted holiday #$id")
        }
    }

    // Business Settings Actions
    fun updateBusinessSettings(settings: BusinessSettingsEntity) {
        val actor = _currentUser.value ?: return
        if (!isAdmin()) return
        viewModelScope.launch {
            repository.updateSettings(settings, actor)
        }
    }

    // Notifications Actions
    fun markNotificationRead(id: Long) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        val user = _currentUser.value ?: return
        viewModelScope.launch { repository.markAllNotificationsRead(user.id) }
    }

    // Export Reports to CSV String
    fun exportFinancialReportCsv(): String {
        val sales = allSales.value
        val buys = allBuys.value
        val expenses = allExpenses.value
        val sb = StringBuilder()
        sb.append("Type,Title,Category/Supplier,Amount,PaymentMethod,Date\n")
        sales.forEach { s ->
            sb.append("SALE,\"${s.title}\",\"${s.category}\",${s.amount},\"${s.paymentMethod}\",\"${Date(s.dateMillis)}\"\n")
        }
        buys.forEach { b ->
            sb.append("BUY,\"${b.title}\",\"${b.supplierName}\",${b.amount},\"${b.paymentMethod}\",\"${Date(b.dateMillis)}\"\n")
        }
        expenses.forEach { e ->
            sb.append("EXPENSE,\"${e.title}\",\"${e.category}\",${e.amount},\"${e.paymentMethod}\",\"${Date(e.dateMillis)}\"\n")
        }
        _exportMessage.value = "Exported ${sales.size + buys.size + expenses.size} financial records to CSV format."
        return sb.toString()
    }

    // Helpers
    private fun getTodayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    private fun getMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}
