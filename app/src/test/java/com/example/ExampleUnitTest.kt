package com.example

import com.example.data.local.UserEntity
import com.example.data.model.AppPermission
import com.example.data.model.TaskStatus
import com.example.data.model.UserRole
import com.example.data.model.UserStatus
import com.example.ui.screens.AdminDummyMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testDailyCostAndProfitCalculation() {
    val dailyBuys = 640.0 + 185.0 // 825.0
    val dailyExpenses = 95.0 + 350.0 // 445.0
    val dailyLabor = 114.0 // 114.0
    val totalDailyCost = dailyBuys + dailyExpenses + dailyLabor
    assertEquals(1384.0, totalDailyCost, 0.001)

    val dailySales = 1450.0 + 820.0 + 310.0 // 2580.0
    val netDailyProfit = dailySales - totalDailyCost
    assertEquals(1196.0, netDailyProfit, 0.001)
  }

  @Test
  fun testHolidayBonusCalculation() {
    val workUnits = 2.0
    val baseRate = 38.0
    val holidayMultiplier = 1.5
    val holidayPay = workUnits * baseRate * holidayMultiplier
    assertEquals(114.0, holidayPay, 0.001)
    val standardPay = workUnits * baseRate
    assertEquals(76.0, standardPay, 0.001)
    val holidayBonus = holidayPay - standardPay
    assertEquals(38.0, holidayBonus, 0.001)
  }

  @Test
  fun testEmployeeOutstandingBalanceCalculation() {
    val workEarnings = 350.0
    val holidayEarnings = 75.0
    val bonuses = 50.0
    val deductions = 25.0
    val grossApproved = workEarnings + holidayEarnings + bonuses - deductions
    assertEquals(450.0, grossApproved, 0.001)

    val totalPaidOut = 200.0
    val outstandingBalance = grossApproved - totalPaidOut
    assertEquals(250.0, outstandingBalance, 0.001)
  }

  @Test
  fun testLateWorkDetectionLogic() {
    val now = 1000000L
    val pastDue = 900000L
    val futureDue = 1100000L

    // If currentTime > dueDate and status != COMPLETED, it is LATE
    val isOverduePast = pastDue < now
    assertTrue(isOverduePast)

    val isOverdueFuture = futureDue < now
    assertFalse(isOverdueFuture)
  }

  @Test
  fun testRoleBasedHierarchyAndPermissions() {
    val superAdmin = UserRole.SUPER_ADMIN
    val admin = UserRole.ADMIN
    val manager = UserRole.MANAGER
    val staff = UserRole.STAFF
    val employee = UserRole.EMPLOYEE

    assertTrue(superAdmin.level > admin.level)
    assertTrue(admin.level > manager.level)
    assertTrue(manager.level > staff.level)
    assertTrue(staff.level > employee.level)

    val allPerms = AppPermission.allCodes()
    assertTrue(allPerms.contains("MANAGE_USERS"))
    assertTrue(allPerms.contains("MANAGE_SETTINGS"))
    assertTrue(allPerms.contains("VIEW_AUDIT_LOGS"))
  }

  @Test
  fun testAdminDummyMetricsAggregation() {
    val dummy = AdminDummyMetrics()
    assertEquals(3450.00, dummy.totalDailySales, 0.001)
    assertEquals(1820.00, dummy.totalDailyCosts, 0.001)
    assertEquals(1630.00, dummy.netDailyProfit, 0.001)
    assertEquals(4, dummy.topSellingCategories.size)
  }
}
