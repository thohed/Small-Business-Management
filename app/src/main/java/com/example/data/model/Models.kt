package com.example.data.model

enum class UserRole(val displayName: String, val level: Int) {
    SUPER_ADMIN("Super Admin", 100),
    ADMIN("Admin (Owner)", 80),
    MANAGER("Manager", 50),
    STAFF("Staff", 30),
    EMPLOYEE("Employee / User", 10);

    companion object {
        fun fromString(role: String?): UserRole {
            return entries.find { it.name.equals(role, ignoreCase = true) } ?: EMPLOYEE
        }
    }
}

enum class UserStatus(val label: String) {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    ON_LEAVE("On Leave")
}

enum class PaymentType(val label: String) {
    PER_WORK("Per-Work / Task"),
    HOURLY("Hourly Wage"),
    FIXED_SALARY("Fixed Salary")
}

enum class AppPermission(val code: String, val description: String) {
    VIEW_DASHBOARD("VIEW_DASHBOARD", "View Business Executive Dashboard"),
    MANAGE_USERS("MANAGE_USERS", "Create, edit, suspend & manage users"),
    MANAGE_ROLES("MANAGE_ROLES", "Configure roles & permissions"),
    MANAGE_SALES("MANAGE_SALES", "Record and manage daily sales"),
    MANAGE_BUYS("MANAGE_BUYS", "Record purchases and inventory buys"),
    MANAGE_EXPENSES("MANAGE_EXPENSES", "Record and manage business expenses & expansion"),
    MANAGE_WORK("MANAGE_WORK", "Create, assign, and update employee work/tasks"),
    MANAGE_PAYMENTS("MANAGE_PAYMENTS", "Review balances and disburse employee payments"),
    APPROVE_PAYMENTS("APPROVE_PAYMENTS", "Approve or reject employee pay requests"),
    VIEW_REPORTS("VIEW_REPORTS", "View and export financial & workforce reports"),
    MANAGE_HOLIDAYS("MANAGE_HOLIDAYS", "Set public holidays and holiday pay rules"),
    VIEW_AUDIT_LOGS("VIEW_AUDIT_LOGS", "Review security and activity audit logs"),
    MANAGE_SETTINGS("MANAGE_SETTINGS", "Configure business profile, currency & thresholds");

    companion object {
        fun allCodes(): String = entries.joinToString(",") { it.code }
        fun managerDefault(): String = listOf(
            VIEW_DASHBOARD, MANAGE_SALES, MANAGE_BUYS, MANAGE_EXPENSES,
            MANAGE_WORK, APPROVE_PAYMENTS, VIEW_REPORTS
        ).joinToString(",") { it.code }
        fun staffDefault(): String = listOf(
            MANAGE_WORK, VIEW_DASHBOARD
        ).joinToString(",") { it.code }
        fun employeeDefault(): String = "".trim()
    }
}

enum class BusinessSegment(val code: String, val label: String) {
    DASHBOARD("DASHBOARD", "Overview & Analytics"),
    SALES("SALES", "Daily Sales"),
    BUYS("BUYS", "Purchases / Buys"),
    EXPENSES("EXPENSES", "Expenses & Expansion"),
    WORK_TASKS("WORK_TASKS", "Work & Task Management"),
    PAYMENTS("PAYMENTS", "Pay Requests & Payroll"),
    HOLIDAYS("HOLIDAYS", "Holiday Management"),
    USERS("USERS", "User & Access Management"),
    REPORTS("REPORTS", "Cost & Profit Reports"),
    AUDIT_LOGS("AUDIT_LOGS", "Activity Audit Logs"),
    SETTINGS("SETTINGS", "Business Settings");

    companion object {
        fun fromCode(code: String): BusinessSegment? = entries.find { it.code.equals(code, ignoreCase = true) }
        fun allCodes(): String = entries.joinToString(",") { it.code }
    }
}

enum class TaskStatus(val label: String) {
    PENDING("Pending"),
    IN_PROGRESS("In Progress"),
    DUE("Due Work"),
    LATE("Late Work"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class TaskPriority(val label: String) {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High"),
    URGENT("Urgent")
}

enum class PayRequestStatus(val label: String) {
    PENDING("Pending"),
    APPROVED("Approved"),
    PAID("Paid"),
    REJECTED("Rejected")
}

enum class DateFilterRange(val label: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

enum class NotificationType(val label: String) {
    WORK_ASSIGNED("Work Assigned"),
    WORK_LATE("Work Overdue"),
    WORK_COMPLETED("Work Completed"),
    PAY_REQUEST_SUBMITTED("Pay Request Submitted"),
    PAY_REQUEST_APPROVED("Pay Request Approved"),
    PAY_REQUEST_PAID("Pay Request Paid"),
    PAY_REQUEST_REJECTED("Pay Request Rejected"),
    SYSTEM("System Announcement")
}

data class DailySummary(
    val dateMillis: Long,
    val totalSales: Double,
    val totalBuys: Double,
    val totalExpenses: Double,
    val totalExpansionExpenses: Double,
    val totalLaborCost: Double,
    val totalHolidayCost: Double,
    val totalCost: Double, // totalBuys + totalExpenses + totalLaborCost + totalHolidayCost
    val netProfit: Double // totalSales - totalCost
)

data class MonthlySummary(
    val yearMonth: String,
    val totalSales: Double,
    val totalBuys: Double,
    val totalExpenses: Double,
    val totalExpansionExpenses: Double,
    val totalLaborPaid: Double,
    val totalHolidayCost: Double,
    val totalCost: Double,
    val netProfit: Double
)

data class UserEarningsSummary(
    val userId: Long,
    val totalCompletedTasks: Int,
    val totalDueTasks: Int,
    val totalLateTasks: Int,
    val workBasedEarnings: Double,
    val holidayEarnings: Double,
    val bonusAmount: Double,
    val deductionAmount: Double,
    val grossApprovedEarnings: Double, // work + holiday + bonus - deduction
    val totalPaidOut: Double,
    val pendingRequestAmount: Double,
    val balanceDue: Double // grossApprovedEarnings - totalPaidOut
)
