package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val fullName: String,
    val role: String, // "SUPER_ADMIN", "ADMIN", "MANAGER", "STAFF", "EMPLOYEE"
    val pin: String, // 4 to 6 digit security PIN
    val email: String,
    val phone: String = "",
    val address: String = "",
    val employeeId: String = "",
    val jobPosition: String = "",
    val department: String = "Operations",
    val joiningDateMillis: Long = System.currentTimeMillis(),
    val paymentType: String = "PER_WORK", // PER_WORK, HOURLY, FIXED_SALARY
    val perWorkRate: Double = 35.0,
    val holidayRateMultiplier: Double = 1.5,
    val bonusAmount: Double = 0.0,
    val deductionAmount: Double = 0.0,
    val assignedManagerId: Long? = null,
    val assignedManagerName: String = "",
    val userStatus: String = "ACTIVE", // ACTIVE, INACTIVE, SUSPENDED, ON_LEAVE
    val allowedSegments: String = "",
    val notes: String = "",
    val profilePhotoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sales",
    indices = [Index(value = ["dateMillis"])]
)
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val customerName: String,
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val amount: Double, // quantity * unitPrice
    val paymentMethod: String = "Cash", // Cash, Bank, Mobile Payment, Card, Other
    val paymentStatus: String = "Paid",
    val category: String = "General Sales",
    val department: String = "Sales",
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val recordedByUserId: Long,
    val recordedByName: String
)

@Entity(
    tableName = "buys",
    indices = [Index(value = ["dateMillis"])]
)
data class BuyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val supplierName: String,
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val amount: Double,
    val category: String = "Inventory",
    val paymentMethod: String = "Bank",
    val paymentStatus: String = "Paid",
    val department: String = "Procurement",
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val recordedByUserId: Long,
    val recordedByName: String
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["dateMillis"])]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // Employee payment, Transportation, Food, Office, Equipment, Utilities, Rent, Marketing, Maintenance, Other
    val amount: Double,
    val isExpansion: Boolean = false,
    val paymentMethod: String = "Bank",
    val approvalStatus: String = "Approved",
    val receiptAttachment: String = "",
    val notes: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val recordedByUserId: Long,
    val recordedByName: String
)

@Entity(
    tableName = "work_tasks",
    indices = [Index(value = ["assignedUserId"]), Index(value = ["dueDateMillis"])]
)
data class WorkTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val assignedUserId: Long,
    val assignedUserName: String,
    val assignedManagerId: Long? = null,
    val department: String = "Operations",
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val status: String, // PENDING, IN_PROGRESS, DUE, LATE, COMPLETED, CANCELLED
    val workUnits: Double = 1.0,
    val baseRate: Double = 35.0,
    val isHoliday: Boolean = false,
    val holidayMultiplier: Double = 1.5,
    val earnedPay: Double,
    val startDateMillis: Long = System.currentTimeMillis(),
    val dueDateMillis: Long,
    val completedDateMillis: Long? = null,
    val createdByUserId: Long,
    val paymentStatus: String = "PENDING", // PENDING, APPROVED, PAID, REJECTED
    val notes: String = ""
)

@Entity(
    tableName = "pay_requests",
    indices = [Index(value = ["userId"]), Index(value = ["status"])]
)
data class PayRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val amount: Double,
    val requestType: String = "Work Payout",
    val status: String = "PENDING", // PENDING, APPROVED, PAID, REJECTED
    val reason: String = "",
    val relatedTaskId: Long? = null,
    val requestDateMillis: Long = System.currentTimeMillis(),
    val reviewedDateMillis: Long? = null,
    val adminNote: String = "",
    val paymentReference: String = ""
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateMillis: Long,
    val description: String = "",
    val isPaid: Boolean = true,
    val applicableDepartment: String = "All",
    val holidayRateMultiplier: Double = 1.5,
    val flatPayBonus: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs", indices = [Index(value = ["timestampMillis"])])
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userRole: String,
    val action: String,
    val entityType: String,
    val entityId: Long = 0,
    val previousValue: String = "",
    val newValue: String = "",
    val details: String = "",
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetUserId: Long? = null, // null for broadcast to admins & managers
    val title: String,
    val message: String,
    val type: String = "SYSTEM",
    val isRead: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_settings")
data class BusinessSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "BizOps Enterprise",
    val currencySymbol: String = "$",
    val currencyCode: String = "USD",
    val taxRate: Double = 0.0,
    val timeZone: String = "UTC",
    val dateFormat: String = "MMM d, yyyy",
    val autoDetectLateWork: Boolean = true,
    val allowEmployeePayRequests: Boolean = true,
    val defaultHolidayMultiplier: Double = 1.5
)
