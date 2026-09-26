package com.example.data.repository

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
import com.example.data.model.NotificationType
import com.example.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

class BizRepository(private val database: AppDatabase) {

    private val userDao = database.userDao()
    private val transDao = database.transactionDao()
    private val taskDao = database.workTaskDao()
    private val payDao = database.payRequestDao()
    private val holidayDao = database.holidayDao()
    private val auditDao = database.auditLogDao()
    private val notifDao = database.notificationDao()
    private val settingsDao = database.businessSettingsDao()

    // --- Users ---
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()
    val activeUsers: Flow<List<UserEntity>> = userDao.getActiveUsers()

    fun getUserById(id: Long): Flow<UserEntity?> = userDao.getUserById(id)
    suspend fun getUserByIdOnce(id: Long): UserEntity? = userDao.getUserByIdOnce(id)

    suspend fun authenticate(identifier: String, pin: String): UserEntity? {
        val trimmedId = identifier.trim()
        val trimmedPin = pin.trim()
        if (trimmedId.isEmpty() || trimmedPin.isEmpty()) return null

        try {
            if (userDao.getUserCount() == 0) {
                AppDatabase.populateInitialData(database)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return userDao.authenticate(trimmedId, trimmedPin)
    }

    suspend fun insertUser(user: UserEntity): Long {
        val id = userDao.insertUser(user)
        logAudit(
            userId = user.id,
            userName = user.fullName,
            role = user.role,
            action = "USER_CREATED",
            entityType = "User",
            entityId = id,
            details = "Added new user @${user.username} with role ${user.role} in department ${user.department}"
        )
        return id
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
        logAudit(
            userId = user.id,
            userName = user.fullName,
            role = user.role,
            action = "USER_UPDATED",
            entityType = "User",
            entityId = user.id,
            details = "Updated user profile & permissions for @${user.username}"
        )
    }

    suspend fun updateUserStatus(id: Long, newStatus: String, actor: UserEntity) {
        val prev = userDao.getUserByIdOnce(id)
        userDao.updateUserStatus(id, newStatus)
        logAudit(
            userId = actor.id,
            userName = actor.fullName,
            role = actor.role,
            action = "USER_STATUS_CHANGED",
            entityType = "User",
            entityId = id,
            previousValue = prev?.userStatus ?: "UNKNOWN",
            newValue = newStatus,
            details = "Changed user status from ${prev?.userStatus} to $newStatus"
        )
    }

    suspend fun resetUserPin(id: Long, newPin: String, actor: UserEntity) {
        userDao.resetUserPin(id, newPin)
        logAudit(
            userId = actor.id,
            userName = actor.fullName,
            role = actor.role,
            action = "USER_PIN_RESET",
            entityType = "User",
            entityId = id,
            details = "Security PIN reset for user #$id by ${actor.fullName}"
        )
    }

    suspend fun deleteUser(id: Long) = userDao.deleteUser(id)

    // --- Transactions (Sales, Buys, Expenses) ---
    val allSales: Flow<List<SaleEntity>> = transDao.getAllSales()
    val allBuys: Flow<List<BuyEntity>> = transDao.getAllBuys()
    val allExpenses: Flow<List<ExpenseEntity>> = transDao.getAllExpenses()

    suspend fun insertSale(sale: SaleEntity): Long {
        val id = transDao.insertSale(sale)
        logAudit(
            userId = sale.recordedByUserId,
            userName = sale.recordedByName,
            role = "USER",
            action = "SALE_RECORDED",
            entityType = "Sale",
            entityId = id,
            details = "Recorded sale: ${sale.title} for $${sale.amount} (${sale.paymentMethod})"
        )
        return id
    }
    suspend fun deleteSale(id: Long) = transDao.deleteSale(id)

    suspend fun insertBuy(buy: BuyEntity): Long {
        val id = transDao.insertBuy(buy)
        logAudit(
            userId = buy.recordedByUserId,
            userName = buy.recordedByName,
            role = "USER",
            action = "BUY_RECORDED",
            entityType = "Buy",
            entityId = id,
            details = "Recorded purchase: ${buy.title} from ${buy.supplierName} for $${buy.amount}"
        )
        return id
    }
    suspend fun deleteBuy(id: Long) = transDao.deleteBuy(id)

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        val id = transDao.insertExpense(expense)
        logAudit(
            userId = expense.recordedByUserId,
            userName = expense.recordedByName,
            role = "USER",
            action = "EXPENSE_RECORDED",
            entityType = "Expense",
            entityId = id,
            details = "Recorded expense: ${expense.title} ($${expense.amount}) - ${if (expense.isExpansion) "Expansion" else "Overhead"}"
        )
        return id
    }
    suspend fun deleteExpense(id: Long) = transDao.deleteExpense(id)

    // --- Work Tasks ---
    val allTasks: Flow<List<WorkTaskEntity>> = taskDao.getAllTasks()
    fun getTasksForUser(userId: Long): Flow<List<WorkTaskEntity>> = taskDao.getTasksForUser(userId)

    suspend fun insertTask(task: WorkTaskEntity): Long {
        val id = taskDao.insertTask(task)
        notifDao.insertNotification(
            NotificationEntity(
                targetUserId = task.assignedUserId,
                title = "New Work Assigned",
                message = "You have been assigned: ${task.title} (Pay: $${task.earnedPay})",
                type = NotificationType.WORK_ASSIGNED.name
            )
        )
        return id
    }

    suspend fun updateTask(task: WorkTaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(id: Long) = taskDao.deleteTask(id)

    suspend fun markTaskCompleted(taskId: Long, actor: UserEntity, notes: String = "") {
        val existing = taskDao.getTaskById(taskId) ?: return
        val updated = existing.copy(
            status = TaskStatus.COMPLETED.name,
            completedDateMillis = System.currentTimeMillis(),
            notes = if (notes.isNotBlank()) notes else existing.notes
        )
        taskDao.updateTask(updated)
        logAudit(
            userId = actor.id,
            userName = actor.fullName,
            role = actor.role,
            action = "WORK_COMPLETED",
            entityType = "WorkTask",
            entityId = taskId,
            details = "Task '${existing.title}' completed by ${actor.fullName}. Earned pay: $${existing.earnedPay}"
        )
        // Notify managers/admins
        notifDao.insertNotification(
            NotificationEntity(
                targetUserId = null,
                title = "Work Completed",
                message = "${actor.fullName} completed: ${existing.title}",
                type = NotificationType.WORK_COMPLETED.name
            )
        )
    }

    // --- Pay Requests ---
    val allPayRequests: Flow<List<PayRequestEntity>> = payDao.getAllPayRequests()
    fun getPayRequestsForUser(userId: Long): Flow<List<PayRequestEntity>> = payDao.getPayRequestsForUser(userId)

    suspend fun insertPayRequest(request: PayRequestEntity): Long {
        val id = payDao.insertPayRequest(request)
        notifDao.insertNotification(
            NotificationEntity(
                targetUserId = null,
                title = "New Pay Request",
                message = "${request.userName} requested $${request.amount} for ${request.reason}",
                type = NotificationType.PAY_REQUEST_SUBMITTED.name
            )
        )
        return id
    }

    suspend fun updatePayRequestStatus(
        requestId: Long,
        status: String,
        actor: UserEntity,
        adminNote: String = "",
        paymentRef: String = ""
    ) {
        val existing = payDao.getPayRequestById(requestId) ?: return
        val updated = existing.copy(
            status = status,
            reviewedDateMillis = System.currentTimeMillis(),
            adminNote = adminNote,
            paymentReference = paymentRef
        )
        payDao.updatePayRequest(updated)

        logAudit(
            userId = actor.id,
            userName = actor.fullName,
            role = actor.role,
            action = "PAY_REQUEST_${status.uppercase()}",
            entityType = "PayRequest",
            entityId = requestId,
            previousValue = existing.status,
            newValue = status,
            details = "${actor.fullName} marked pay request of $${existing.amount} for ${existing.userName} as $status. Ref: $paymentRef"
        )

        val notifType = when (status) {
            "PAID" -> NotificationType.PAY_REQUEST_PAID.name
            "APPROVED" -> NotificationType.PAY_REQUEST_APPROVED.name
            else -> NotificationType.PAY_REQUEST_REJECTED.name
        }
        notifDao.insertNotification(
            NotificationEntity(
                targetUserId = existing.userId,
                title = "Pay Request $status",
                message = "Your pay request for $${existing.amount} was marked $status by ${actor.fullName}.",
                type = notifType
            )
        )
    }

    suspend fun deletePayRequest(id: Long) = payDao.deletePayRequest(id)

    // --- Holidays ---
    val allHolidays: Flow<List<HolidayEntity>> = holidayDao.getAllHolidays()
    suspend fun insertHoliday(holiday: HolidayEntity): Long = holidayDao.insertHoliday(holiday)
    suspend fun updateHoliday(holiday: HolidayEntity) = holidayDao.updateHoliday(holiday)
    suspend fun deleteHoliday(id: Long) = holidayDao.deleteHoliday(id)

    // --- Audit Logs ---
    val recentAuditLogs: Flow<List<AuditLogEntity>> = auditDao.getRecentAuditLogs()
    suspend fun logAudit(
        userId: Long,
        userName: String,
        role: String,
        action: String,
        entityType: String,
        entityId: Long = 0,
        previousValue: String = "",
        newValue: String = "",
        details: String = ""
    ) {
        try {
            auditDao.insertLog(
                AuditLogEntity(
                    userId = userId,
                    userName = userName,
                    userRole = role,
                    action = action,
                    entityType = entityType,
                    entityId = entityId,
                    previousValue = previousValue,
                    newValue = newValue,
                    details = details
                )
            )
        } catch (_: Exception) {}
    }

    // --- Notifications ---
    val allNotifications: Flow<List<NotificationEntity>> = notifDao.getAllNotifications()
    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>> = notifDao.getNotificationsForUser(userId)
    suspend fun markNotificationRead(id: Long) = notifDao.markAsRead(id)
    suspend fun markAllNotificationsRead(userId: Long) = notifDao.markAllAsRead(userId)

    // --- Business Settings ---
    val businessSettings: Flow<BusinessSettingsEntity?> = settingsDao.getSettings()
    suspend fun updateSettings(settings: BusinessSettingsEntity, actor: UserEntity) {
        settingsDao.insertOrUpdate(settings)
        logAudit(
            userId = actor.id,
            userName = actor.fullName,
            role = actor.role,
            action = "SETTINGS_UPDATED",
            entityType = "BusinessSettings",
            details = "Updated business settings (Currency: ${settings.currencyCode}, Tax: ${settings.taxRate}%) by ${actor.fullName}"
        )
    }
}
