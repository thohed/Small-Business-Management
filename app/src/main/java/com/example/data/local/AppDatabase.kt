package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AppPermission
import com.example.data.model.NotificationType
import com.example.data.model.PayRequestStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        SaleEntity::class,
        BuyEntity::class,
        ExpenseEntity::class,
        WorkTaskEntity::class,
        PayRequestEntity::class,
        HolidayEntity::class,
        AuditLogEntity::class,
        NotificationEntity::class,
        BusinessSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun workTaskDao(): WorkTaskDao
    abstract fun payRequestDao(): PayRequestDao
    abstract fun holidayDao(): HolidayDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun notificationDao(): NotificationDao
    abstract fun businessSettingsDao(): BusinessSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bizops_database"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabasePrepopulateCallback(scope))
                    .build()
                INSTANCE = instance

                // Eagerly ensure database has seeded accounts so login never fails
                scope.launch(Dispatchers.IO) {
                    ensureDatabaseSeeded(instance)
                }

                instance
            }
        }

        suspend fun ensureDatabaseSeeded(db: AppDatabase) {
            try {
                if (db.userDao().getUserCount() == 0) {
                    populateInitialData(db)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            val userDao = db.userDao()
            val transDao = db.transactionDao()
            val taskDao = db.workTaskDao()
            val payDao = db.payRequestDao()
            val holidayDao = db.holidayDao()
            val auditDao = db.auditLogDao()
            val notifDao = db.notificationDao()
            val settingsDao = db.businessSettingsDao()

            val now = System.currentTimeMillis()
            val dayMillis = 86400000L

            // 0. Default Business Settings
            settingsDao.insertOrUpdate(
                BusinessSettingsEntity(
                    id = 1,
                    businessName = "Apex Industrial & Tech Solutions",
                    currencySymbol = "$",
                    currencyCode = "USD",
                    taxRate = 5.0,
                    timeZone = "EST",
                    dateFormat = "MMM d, yyyy",
                    autoDetectLateWork = true,
                    allowEmployeePayRequests = true,
                    defaultHolidayMultiplier = 1.5
                )
            )

            // 1. Initial Users (1 Super Admin, 1 Admin, 2 Managers, 5 Employees)
            val users = listOf(
                // 1. Super Admin
                UserEntity(
                    id = 1,
                    username = "superadmin",
                    fullName = "Eleanor Vance",
                    role = UserRole.SUPER_ADMIN.name,
                    pin = "1234",
                    email = "eleanor.vance@apex.biz",
                    phone = "+1 (555) 019-2831",
                    address = "742 Evergreen Terrace, Suite 100",
                    employeeId = "EMP-001",
                    jobPosition = "Founder & Chief Executive",
                    department = "Executive Management",
                    paymentType = "FIXED_SALARY",
                    perWorkRate = 100.0,
                    holidayRateMultiplier = 2.0,
                    userStatus = UserStatus.ACTIVE.name,
                    allowedSegments = AppPermission.allCodes(),
                    createdAt = now - dayMillis * 90
                ),
                // 2. Admin
                UserEntity(
                    id = 2,
                    username = "admin",
                    fullName = "Arthur Pendelton",
                    role = UserRole.ADMIN.name,
                    pin = "1234",
                    email = "arthur.admin@apex.biz",
                    phone = "+1 (555) 019-3344",
                    address = "120 Market Street, Floor 4",
                    employeeId = "EMP-002",
                    jobPosition = "General Business Administrator",
                    department = "Operations & Finance",
                    paymentType = "FIXED_SALARY",
                    perWorkRate = 75.0,
                    holidayRateMultiplier = 1.75,
                    userStatus = UserStatus.ACTIVE.name,
                    allowedSegments = AppPermission.allCodes(),
                    createdAt = now - dayMillis * 60
                ),
                // 3. Manager 1
                UserEntity(
                    id = 3,
                    username = "marcus",
                    fullName = "Marcus Brody",
                    role = UserRole.MANAGER.name,
                    pin = "2345",
                    email = "marcus.brody@apex.biz",
                    phone = "+1 (555) 019-7412",
                    address = "45 Industrial Parkway, Bldg B",
                    employeeId = "MGR-001",
                    jobPosition = "Manufacturing & Assembly Lead",
                    department = "Manufacturing & Assembly",
                    paymentType = "PER_WORK",
                    perWorkRate = 50.0,
                    holidayRateMultiplier = 1.5,
                    userStatus = UserStatus.ACTIVE.name,
                    allowedSegments = AppPermission.managerDefault(),
                    createdAt = now - dayMillis * 45
                ),
                // 4. Manager 2
                UserEntity(
                    id = 4,
                    username = "elena",
                    fullName = "Elena Rostova",
                    role = UserRole.MANAGER.name,
                    pin = "2345",
                    email = "elena.rostova@apex.biz",
                    phone = "+1 (555) 019-8821",
                    address = "88 Harbor View Boulevard",
                    employeeId = "MGR-002",
                    jobPosition = "Logistics & Procurement Manager",
                    department = "Logistics & Inventory",
                    paymentType = "PER_WORK",
                    perWorkRate = 48.0,
                    holidayRateMultiplier = 1.5,
                    userStatus = UserStatus.ACTIVE.name,
                    allowedSegments = AppPermission.managerDefault(),
                    createdAt = now - dayMillis * 40
                ),
                // 5. Employee 1
                UserEntity(
                    id = 5,
                    username = "sophia",
                    fullName = "Sophia Chen",
                    role = UserRole.EMPLOYEE.name,
                    pin = "3456",
                    email = "sophia.chen@apex.biz",
                    phone = "+1 (555) 019-5582",
                    address = "210 Oak Street, Apt 3B",
                    employeeId = "EMP-101",
                    jobPosition = "Precision Assembly Specialist",
                    department = "Manufacturing & Assembly",
                    assignedManagerId = 3,
                    assignedManagerName = "Marcus Brody",
                    paymentType = "PER_WORK",
                    perWorkRate = 40.0,
                    holidayRateMultiplier = 1.5,
                    userStatus = UserStatus.ACTIVE.name,
                    createdAt = now - dayMillis * 30
                ),
                // 6. Employee 2
                UserEntity(
                    id = 6,
                    username = "david",
                    fullName = "David Miller",
                    role = UserRole.STAFF.name,
                    pin = "4567",
                    email = "david.miller@apex.biz",
                    phone = "+1 (555) 019-9943",
                    address = "55 Highland Road",
                    employeeId = "EMP-102",
                    jobPosition = "Inventory & Dispatch Associate",
                    department = "Logistics & Inventory",
                    assignedManagerId = 4,
                    assignedManagerName = "Elena Rostova",
                    paymentType = "PER_WORK",
                    perWorkRate = 35.0,
                    holidayRateMultiplier = 1.5,
                    userStatus = UserStatus.ACTIVE.name,
                    allowedSegments = "${AppPermission.MANAGE_SALES.code},${AppPermission.MANAGE_WORK.code}",
                    createdAt = now - dayMillis * 28
                ),
                // 7. Employee 3
                UserEntity(
                    id = 7,
                    username = "lucas",
                    fullName = "Lucas Hayes",
                    role = UserRole.EMPLOYEE.name,
                    pin = "5678",
                    email = "lucas.hayes@apex.biz",
                    phone = "+1 (555) 019-1123",
                    address = "904 Pine Avenue",
                    employeeId = "EMP-103",
                    jobPosition = "QA Test & Inspection Tech",
                    department = "Quality Assurance",
                    assignedManagerId = 3,
                    assignedManagerName = "Marcus Brody",
                    paymentType = "PER_WORK",
                    perWorkRate = 38.0,
                    holidayRateMultiplier = 1.5,
                    userStatus = UserStatus.ACTIVE.name,
                    createdAt = now - dayMillis * 20
                ),
                // 8. Employee 4
                UserEntity(
                    id = 8,
                    username = "aria",
                    fullName = "Aria Montgomery",
                    role = UserRole.EMPLOYEE.name,
                    pin = "6789",
                    email = "aria.montgomery@apex.biz",
                    phone = "+1 (555) 019-6677",
                    address = "12 Lakeview Drive",
                    employeeId = "EMP-104",
                    jobPosition = "Client Support & Order Specialist",
                    department = "Retail Sales & Support",
                    assignedManagerId = 2,
                    assignedManagerName = "Arthur Pendelton",
                    paymentType = "PER_WORK",
                    perWorkRate = 34.0,
                    holidayRateMultiplier = 1.5,
                    userStatus = UserStatus.ON_LEAVE.name,
                    createdAt = now - dayMillis * 15
                ),
                // 9. Employee 5
                UserEntity(
                    id = 9,
                    username = "jamal",
                    fullName = "Jamal Washington",
                    role = UserRole.EMPLOYEE.name,
                    pin = "7890",
                    email = "jamal.washington@apex.biz",
                    phone = "+1 (555) 019-4455",
                    address = "332 Cedar Court",
                    employeeId = "EMP-105",
                    jobPosition = "Equipment Calibration & Field Tech",
                    department = "Manufacturing & Assembly",
                    assignedManagerId = 3,
                    assignedManagerName = "Marcus Brody",
                    paymentType = "PER_WORK",
                    perWorkRate = 42.0,
                    holidayRateMultiplier = 1.5,
                    userStatus = UserStatus.ACTIVE.name,
                    createdAt = now - dayMillis * 12
                )
            )
            userDao.insertUsers(users)

            // 2. Daily Sales
            val sales = listOf(
                SaleEntity(
                    title = "Commercial Control System Batch #A14",
                    customerName = "Apex Commercial Ltd",
                    quantity = 2.0,
                    unitPrice = 1150.0,
                    amount = 2300.0,
                    paymentMethod = "Bank",
                    paymentStatus = "Paid",
                    category = "Wholesale",
                    department = "Sales",
                    notes = "Delivered to warehouse dock 3. Invoice #INV-2041",
                    dateMillis = now - 3600000L * 2,
                    recordedByUserId = 2,
                    recordedByName = "Arthur Pendelton"
                ),
                SaleEntity(
                    title = "Custom Maintenance & Calibration Service Contract",
                    customerName = "Metro Logistics Corp",
                    quantity = 1.0,
                    unitPrice = 850.0,
                    amount = 850.0,
                    paymentMethod = "Card",
                    paymentStatus = "Paid",
                    category = "Service Contract",
                    department = "Sales",
                    notes = "Quarterly preventative maintenance agreement",
                    dateMillis = now - 3600000L * 4,
                    recordedByUserId = 6,
                    recordedByName = "David Miller"
                ),
                SaleEntity(
                    title = "Walk-in Store Purchase: Precision Fastener Kits",
                    customerName = "Walk-in Customer",
                    quantity = 10.0,
                    unitPrice = 30.0,
                    amount = 300.0,
                    paymentMethod = "Cash",
                    paymentStatus = "Paid",
                    category = "Retail Sales",
                    department = "Retail Sales & Support",
                    notes = "Counter sale receipt #CS-8841",
                    dateMillis = now - 3600000L * 1,
                    recordedByUserId = 6,
                    recordedByName = "David Miller"
                ),
                SaleEntity(
                    title = "Industrial Power Distribution Pack",
                    customerName = "Greenline Fabrication",
                    quantity = 1.0,
                    unitPrice = 1850.0,
                    amount = 1850.0,
                    paymentMethod = "Mobile Payment",
                    paymentStatus = "Paid",
                    category = "Equipment",
                    department = "Sales",
                    notes = "Wire transfer receipt confirmed",
                    dateMillis = now - dayMillis * 2,
                    recordedByUserId = 2,
                    recordedByName = "Arthur Pendelton"
                )
            )
            transDao.insertSales(sales)

            // 3. Daily Buys (Purchases)
            val buys = listOf(
                BuyEntity(
                    title = "High-Grade Extruded Aluminum Stock (40 Units)",
                    supplierName = "Apex Metal Supply Co.",
                    quantity = 40.0,
                    unitPrice = 18.5,
                    amount = 740.0,
                    category = "Raw Materials",
                    paymentMethod = "Bank",
                    department = "Procurement",
                    notes = "Passed QA inspection at receiving bay",
                    dateMillis = now - 3600000L * 3,
                    recordedByUserId = 4,
                    recordedByName = "Elena Rostova"
                ),
                BuyEntity(
                    title = "Reinforced Shipping Cartons & Cushioning (500 sets)",
                    supplierName = "PackPro Packaging Solutions",
                    quantity = 500.0,
                    unitPrice = 0.48,
                    amount = 240.0,
                    category = "Packaging",
                    paymentMethod = "Card",
                    department = "Logistics & Inventory",
                    notes = "Replenished shipping bay stock",
                    dateMillis = now - 3600000L * 5,
                    recordedByUserId = 4,
                    recordedByName = "Elena Rostova"
                ),
                BuyEntity(
                    title = "Electronic Diagnostic Sensor Modules Batch 5",
                    supplierName = "MicroCircuit Global Ltd",
                    quantity = 50.0,
                    unitPrice = 28.0,
                    amount = 1400.0,
                    category = "Inventory",
                    paymentMethod = "Bank",
                    department = "Procurement",
                    notes = "Pre-paid inventory restock",
                    dateMillis = now - dayMillis * 4,
                    recordedByUserId = 2,
                    recordedByName = "Arthur Pendelton"
                )
            )
            transDao.insertBuys(buys)

            // 4. Daily Expenses
            val expenses = listOf(
                ExpenseEntity(
                    title = "Commercial High-Speed Fiber Internet & ERP Cloud",
                    category = "Utilities",
                    amount = 120.0,
                    isExpansion = false,
                    paymentMethod = "Bank",
                    approvalStatus = "Approved",
                    notes = "Monthly fiber connection and data hosting",
                    dateMillis = now - 3600000L * 2,
                    recordedByUserId = 2,
                    recordedByName = "Arthur Pendelton"
                ),
                ExpenseEntity(
                    title = "Automated CNC Precision Milling Head (Capital Expansion)",
                    category = "Equipment",
                    amount = 450.0,
                    isExpansion = true, // Capital expansion
                    paymentMethod = "Bank",
                    approvalStatus = "Approved",
                    notes = "Machinery capital upgrade to increase output capacity",
                    dateMillis = now - 3600000L * 6,
                    recordedByUserId = 1,
                    recordedByName = "Eleanor Vance"
                ),
                ExpenseEntity(
                    title = "Production Facility Lease & Showroom Rent",
                    category = "Rent",
                    amount = 1600.0,
                    isExpansion = false,
                    paymentMethod = "Bank",
                    approvalStatus = "Approved",
                    notes = "Monthly plant lease installment",
                    dateMillis = now - dayMillis * 6,
                    recordedByUserId = 1,
                    recordedByName = "Eleanor Vance"
                )
            )
            transDao.insertExpenses(expenses)

            // 5. Work Tasks (Due work, Late work, Done work, Holiday pay)
            val tasks = listOf(
                WorkTaskEntity(
                    title = "Precision Assembly Batch #A1 (3 units)",
                    description = "Assemble, wire, and inspect high-tolerance controller units.",
                    assignedUserId = 5,
                    assignedUserName = "Sophia Chen",
                    assignedManagerId = 3,
                    department = "Manufacturing & Assembly",
                    priority = TaskPriority.HIGH.name,
                    status = TaskStatus.COMPLETED.name,
                    workUnits = 3.0,
                    baseRate = 40.0,
                    isHoliday = false,
                    holidayMultiplier = 1.0,
                    earnedPay = 120.0, // 3 * 40
                    dueDateMillis = now - dayMillis,
                    completedDateMillis = now - 3600000L * 5,
                    createdByUserId = 3,
                    paymentStatus = "APPROVED",
                    notes = "100% QA pass rate"
                ),
                WorkTaskEntity(
                    title = "Holiday Weekend Overtime Calibration",
                    description = "Emergency holiday weekend calibration for medical client delivery.",
                    assignedUserId = 5,
                    assignedUserName = "Sophia Chen",
                    assignedManagerId = 3,
                    department = "Manufacturing & Assembly",
                    priority = TaskPriority.URGENT.name,
                    status = TaskStatus.COMPLETED.name,
                    workUnits = 2.0,
                    baseRate = 40.0,
                    isHoliday = true,
                    holidayMultiplier = 1.5,
                    earnedPay = 120.0, // 2 * 40 * 1.5
                    dueDateMillis = now - dayMillis * 2,
                    completedDateMillis = now - dayMillis * 2,
                    createdByUserId = 1,
                    paymentStatus = "PAID",
                    notes = "Holiday multiplier 1.5x verified"
                ),
                WorkTaskEntity(
                    title = "Unit #89 Sensor Refurbishment",
                    description = "Replace faulty thermocouple and flash firmware v4.2.",
                    assignedUserId = 5,
                    assignedUserName = "Sophia Chen",
                    assignedManagerId = 3,
                    department = "Manufacturing & Assembly",
                    priority = TaskPriority.MEDIUM.name,
                    status = TaskStatus.DUE.name,
                    workUnits = 1.0,
                    baseRate = 40.0,
                    isHoliday = false,
                    holidayMultiplier = 1.0,
                    earnedPay = 40.0,
                    dueDateMillis = now + 3600000L * 8, // Due today
                    createdByUserId = 3,
                    notes = "In progress at Bench 2"
                ),
                WorkTaskEntity(
                    title = "Quarterly Facility Emergency Safety Sensor Audit",
                    description = "Audit exhaust vent switches and emergency stop systems.",
                    assignedUserId = 7,
                    assignedUserName = "Lucas Hayes",
                    assignedManagerId = 3,
                    department = "Quality Assurance",
                    priority = TaskPriority.HIGH.name,
                    status = TaskStatus.LATE.name, // Overdue
                    workUnits = 1.0,
                    baseRate = 38.0,
                    isHoliday = false,
                    holidayMultiplier = 1.0,
                    earnedPay = 38.0,
                    dueDateMillis = now - dayMillis * 2, // 2 days past due
                    createdByUserId = 2,
                    notes = "Overdue: Requires immediate completion"
                ),
                WorkTaskEntity(
                    title = "Warehouse Pallet Sorting & RFID Barcoding",
                    description = "Scan incoming stock pallets and map to warehouse rack 14.",
                    assignedUserId = 6,
                    assignedUserName = "David Miller",
                    assignedManagerId = 4,
                    department = "Logistics & Inventory",
                    priority = TaskPriority.MEDIUM.name,
                    status = TaskStatus.COMPLETED.name,
                    workUnits = 3.0,
                    baseRate = 35.0,
                    isHoliday = false,
                    holidayMultiplier = 1.0,
                    earnedPay = 105.0,
                    dueDateMillis = now - dayMillis * 3,
                    completedDateMillis = now - dayMillis * 3,
                    createdByUserId = 4,
                    paymentStatus = "PAID",
                    notes = "Bins 10 through 22 completed"
                ),
                WorkTaskEntity(
                    title = "Retail Showcase Demo Bench Reconfiguration",
                    description = "Assemble new product demo displays for client showroom.",
                    assignedUserId = 6,
                    assignedUserName = "David Miller",
                    assignedManagerId = 4,
                    department = "Logistics & Inventory",
                    priority = TaskPriority.LOW.name,
                    status = TaskStatus.DUE.name,
                    workUnits = 2.0,
                    baseRate = 35.0,
                    isHoliday = false,
                    holidayMultiplier = 1.0,
                    earnedPay = 70.0,
                    dueDateMillis = now + dayMillis * 2,
                    createdByUserId = 4,
                    notes = "Materials staged at showroom"
                )
            )
            taskDao.insertTasks(tasks)

            // 6. Pay Requests
            val payRequests = listOf(
                PayRequestEntity(
                    userId = 5,
                    userName = "Sophia Chen",
                    amount = 120.0,
                    requestType = "Work Payout",
                    status = PayRequestStatus.PENDING.name,
                    reason = "Completed Controller Assembly Batch #A1",
                    relatedTaskId = 1,
                    requestDateMillis = now - 3600000L * 3
                ),
                PayRequestEntity(
                    userId = 7,
                    userName = "Lucas Hayes",
                    amount = 150.0,
                    requestType = "Work Payout",
                    status = PayRequestStatus.APPROVED.name,
                    reason = "Mid-month completed QA testing shifts",
                    requestDateMillis = now - dayMillis,
                    reviewedDateMillis = now - 3600000L * 4,
                    adminNote = "Approved for direct deposit"
                ),
                PayRequestEntity(
                    userId = 5,
                    userName = "Sophia Chen",
                    amount = 120.0,
                    requestType = "Holiday Bonus",
                    status = PayRequestStatus.PAID.name,
                    reason = "Holiday emergency overtime compensation",
                    requestDateMillis = now - dayMillis * 2,
                    reviewedDateMillis = now - dayMillis * 2,
                    adminNote = "Transferred via Direct Deposit #TRX-9941",
                    paymentReference = "TRX-9941"
                )
            )
            payDao.insertPayRequests(payRequests)

            // 7. Holidays
            val holidays = listOf(
                HolidayEntity(
                    name = "National Labor Day",
                    dateMillis = now - dayMillis * 10,
                    description = "Statutory paid business holiday with 1.5x bonus rate",
                    isPaid = true,
                    applicableDepartment = "All",
                    holidayRateMultiplier = 1.5,
                    flatPayBonus = 50.0
                ),
                HolidayEntity(
                    name = "Annual Founders Day",
                    dateMillis = now + dayMillis * 14,
                    description = "Company celebration holiday. Double pay for on-call staff.",
                    isPaid = true,
                    applicableDepartment = "All",
                    holidayRateMultiplier = 2.0,
                    flatPayBonus = 75.0
                )
            )
            holidayDao.insertHolidays(holidays)

            // 8. Audit Logs
            val logs = listOf(
                AuditLogEntity(
                    userId = 1,
                    userName = "Eleanor Vance",
                    userRole = "SUPER_ADMIN",
                    action = "USER_ROLE_UPDATED",
                    entityType = "User",
                    entityId = 6,
                    previousValue = "EMPLOYEE",
                    newValue = "STAFF",
                    details = "Granted David Miller sales recording permissions",
                    timestampMillis = now - dayMillis * 5
                ),
                AuditLogEntity(
                    userId = 2,
                    userName = "Arthur Pendelton",
                    userRole = "ADMIN",
                    action = "PAYMENT_REQUEST_APPROVED",
                    entityType = "PayRequest",
                    entityId = 2,
                    previousValue = "PENDING",
                    newValue = "APPROVED",
                    details = "Approved pay request of $150.00 for Lucas Hayes",
                    timestampMillis = now - 3600000L * 4
                ),
                AuditLogEntity(
                    userId = 3,
                    userName = "Marcus Brody",
                    userRole = "MANAGER",
                    action = "WORK_ASSIGNED",
                    entityType = "WorkTask",
                    entityId = 1,
                    previousValue = "NONE",
                    newValue = "ASSIGNED",
                    details = "Assigned Controller Assembly Batch #A1 to Sophia Chen",
                    timestampMillis = now - dayMillis * 2
                )
            )
            logs.forEach { auditDao.insertLog(it) }

            // 9. Notifications
            val notifs = listOf(
                NotificationEntity(
                    targetUserId = 1,
                    title = "Pending Pay Request",
                    message = "Sophia Chen submitted a payment request for $120.00",
                    type = NotificationType.PAY_REQUEST_SUBMITTED.name,
                    timestampMillis = now - 3600000L * 3
                ),
                NotificationEntity(
                    targetUserId = 7,
                    title = "Work Task Overdue",
                    message = "Safety Sensor Audit was due 2 days ago. Please update status.",
                    type = NotificationType.WORK_LATE.name,
                    timestampMillis = now - dayMillis
                ),
                NotificationEntity(
                    targetUserId = null,
                    title = "Company Announcement",
                    message = "Founders Day celebration scheduled for next month. 2x holiday pay active.",
                    type = NotificationType.SYSTEM.name,
                    timestampMillis = now - dayMillis * 2
                )
            )
            notifs.forEach { notifDao.insertNotification(it) }
        }
    }

    private class DatabasePrepopulateCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    ensureDatabaseSeeded(database)
                }
            }
        }
    }
}
