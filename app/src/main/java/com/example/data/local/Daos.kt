package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY role ASC, fullName ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userStatus = 'ACTIVE' ORDER BY fullName ASC")
    fun getActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserByIdOnce(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("""
        SELECT * FROM users 
        WHERE (
            LOWER(TRIM(username)) = LOWER(TRIM(:identifier)) 
            OR LOWER(TRIM(email)) = LOWER(TRIM(:identifier))
            OR LOWER(TRIM(employeeId)) = LOWER(TRIM(:identifier))
            OR LOWER(TRIM(fullName)) = LOWER(TRIM(:identifier))
        ) 
        AND TRIM(pin) = TRIM(:pin) 
        LIMIT 1
    """)
    suspend fun authenticate(identifier: String, pin: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET userStatus = :newStatus WHERE id = :id")
    suspend fun updateUserStatus(id: Long, newStatus: String)

    @Query("UPDATE users SET pin = :newPin WHERE id = :id")
    suspend fun resetUserPin(id: Long, newPin: String)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Long)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}

@Dao
interface TransactionDao {
    // --- Sales ---
    @Query("SELECT * FROM sales ORDER BY dateMillis DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE dateMillis >= :startTime AND dateMillis <= :endTime ORDER BY dateMillis DESC")
    fun getSalesInRange(startTime: Long, endTime: Long): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleEntity>)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSale(id: Long)

    // --- Buys (Purchases) ---
    @Query("SELECT * FROM buys ORDER BY dateMillis DESC")
    fun getAllBuys(): Flow<List<BuyEntity>>

    @Query("SELECT * FROM buys WHERE dateMillis >= :startTime AND dateMillis <= :endTime ORDER BY dateMillis DESC")
    fun getBuysInRange(startTime: Long, endTime: Long): Flow<List<BuyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuy(buy: BuyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuys(buys: List<BuyEntity>)

    @Query("DELETE FROM buys WHERE id = :id")
    suspend fun deleteBuy(id: Long)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY dateMillis DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE dateMillis >= :startTime AND dateMillis <= :endTime ORDER BY dateMillis DESC")
    fun getExpensesInRange(startTime: Long, endTime: Long): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpense(id: Long)
}

@Dao
interface WorkTaskDao {
    @Query("SELECT * FROM work_tasks ORDER BY dueDateMillis ASC")
    fun getAllTasks(): Flow<List<WorkTaskEntity>>

    @Query("SELECT * FROM work_tasks WHERE assignedUserId = :userId ORDER BY dueDateMillis ASC")
    fun getTasksForUser(userId: Long): Flow<List<WorkTaskEntity>>

    @Query("SELECT * FROM work_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): WorkTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: WorkTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<WorkTaskEntity>)

    @Update
    suspend fun updateTask(task: WorkTaskEntity)

    @Query("DELETE FROM work_tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)
}

@Dao
interface PayRequestDao {
    @Query("SELECT * FROM pay_requests ORDER BY requestDateMillis DESC")
    fun getAllPayRequests(): Flow<List<PayRequestEntity>>

    @Query("SELECT * FROM pay_requests WHERE userId = :userId ORDER BY requestDateMillis DESC")
    fun getPayRequestsForUser(userId: Long): Flow<List<PayRequestEntity>>

    @Query("SELECT * FROM pay_requests WHERE id = :id LIMIT 1")
    suspend fun getPayRequestById(id: Long): PayRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayRequest(request: PayRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayRequests(requests: List<PayRequestEntity>)

    @Update
    suspend fun updatePayRequest(request: PayRequestEntity)

    @Query("DELETE FROM pay_requests WHERE id = :id")
    suspend fun deletePayRequest(id: Long)
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY dateMillis ASC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolidays(holidays: List<HolidayEntity>)

    @Update
    suspend fun updateHoliday(holiday: HolidayEntity)

    @Query("DELETE FROM holidays WHERE id = :id")
    suspend fun deleteHoliday(id: Long)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestampMillis DESC LIMIT 200")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestampMillis DESC LIMIT 100")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE targetUserId IS NULL OR targetUserId = :userId ORDER BY timestampMillis DESC LIMIT 50")
    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1 WHERE targetUserId IS NULL OR targetUserId = :userId")
    suspend fun markAllAsRead(userId: Long)
}

@Dao
interface BusinessSettingsDao {
    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<BusinessSettingsEntity?>

    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsOnce(): BusinessSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: BusinessSettingsEntity)
}
