package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PayRequestEntity
import com.example.data.local.UserEntity
import com.example.data.model.AppPermission
import com.example.data.model.PayRequestStatus
import com.example.data.model.TaskPriority
import com.example.data.model.UserRole
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.RoseExpense
import java.util.Locale

// 1. ADD SALE DIALOG
@Composable
fun AddSaleDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        customer: String,
        quantity: Double,
        unitPrice: Double,
        paymentMethod: String,
        category: String,
        department: String,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var customer by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1.0") }
    var unitPriceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Wholesale") }
    var department by remember { mutableStateOf("Sales") }
    var paymentMethod by remember { mutableStateOf("Bank") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val qty = quantityText.toDoubleOrNull() ?: 1.0
    val price = unitPriceText.toDoubleOrNull() ?: 0.0
    val totalCalculated = qty * price

    val paymentMethods = listOf("Cash", "Bank", "Mobile Payment", "Card", "Other")
    val categories = listOf("Wholesale", "Retail Sales", "Service Contract", "Custom Order")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Daily Sale", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product / Service Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("sale_title_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("Customer / Client Name") },
                    placeholder = { Text("e.g. Metro Logistics") },
                    modifier = Modifier.fillMaxWidth().testTag("sale_customer_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("sale_qty_input")
                    )
                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text("Unit Price ($) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("sale_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Calculated Total (Qty × Price):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "$${String.format(Locale.US, "%.2f", totalCalculated)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = EmeraldGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    paymentMethods.take(3).forEach { m ->
                        FilterChip(
                            selected = paymentMethod == m,
                            onClick = { paymentMethod = m },
                            label = { Text(m, fontSize = 10.sp) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    paymentMethods.drop(3).forEach { m ->
                        FilterChip(
                            selected = paymentMethod == m,
                            onClick = { paymentMethod = m },
                            label = { Text(m, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Invoice Ref (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = RoseExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Please enter product/service name"
                    } else if (price <= 0 || qty <= 0) {
                        error = "Please enter valid quantity and unit price"
                    } else {
                        onConfirm(title, customer, qty, price, paymentMethod, category, department, notes)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_sale_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Save Sale")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 2. ADD BUY (PURCHASE) DIALOG
@Composable
fun AddBuyDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        supplier: String,
        quantity: Double,
        unitPrice: Double,
        category: String,
        department: String,
        paymentMethod: String,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var supplier by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1.0") }
    var unitPriceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Raw Materials") }
    var department by remember { mutableStateOf("Procurement") }
    var paymentMethod by remember { mutableStateOf("Bank") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val qty = quantityText.toDoubleOrNull() ?: 1.0
    val price = unitPriceText.toDoubleOrNull() ?: 0.0
    val totalCalculated = qty * price

    val categories = listOf("Raw Materials", "Inventory", "Packaging", "Tools", "Office Supplies")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = RoseExpense)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Daily Buy (Purchase)", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Item / Material Purchased *") },
                    modifier = Modifier.fillMaxWidth().testTag("buy_title_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Supplier Name") },
                    placeholder = { Text("e.g. Apex Metal Supply") },
                    modifier = Modifier.fillMaxWidth().testTag("buy_supplier_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("buy_qty_input")
                    )
                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it },
                        label = { Text("Unit Cost ($) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("buy_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Purchase Cost:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "$${String.format(Locale.US, "%.2f", totalCalculated)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = RoseExpense
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Batch # (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = RoseExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Please enter purchased item title"
                    } else if (price <= 0 || qty <= 0) {
                        error = "Please enter valid quantity and unit cost"
                    } else {
                        onConfirm(title, supplier, qty, price, category, department, paymentMethod, notes)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_buy_button"),
                colors = ButtonDefaults.buttonColors(containerColor = RoseExpense)
            ) {
                Text("Save Buy")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 3. ADD EXPENSE DIALOG
@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double, isExpansion: Boolean, paymentMethod: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Utilities") }
    var amountText by remember { mutableStateOf("") }
    var isExpansion by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("Bank") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Employee payment", "Transportation", "Food", "Office", "Equipment", "Utilities", "Rent", "Marketing", "Maintenance", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Business Expense", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title *") },
                    placeholder = { Text("e.g. Electric bill, CNC upgrade") },
                    modifier = Modifier.fillMaxWidth().testTag("expense_title_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                    singleLine = true,
                    leadingIcon = { Text("$", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                )
                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExpansion) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Capital Expansion Cost", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Long-term asset / capacity expansion", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = isExpansion,
                            onCheckedChange = { isExpansion = it },
                            modifier = Modifier.testTag("expansion_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Category", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.drop(3).take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 10.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Memo (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = RoseExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (title.isBlank()) {
                        error = "Please enter expense title"
                    } else if (amount == null || amount <= 0) {
                        error = "Please enter a valid expense amount"
                    } else {
                        onConfirm(title, category, amount, isExpansion, paymentMethod, notes)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_expense_button"),
                colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 4. ASSIGN TASK DIALOG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignTaskDialog(
    staffList: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf(staffList.firstOrNull()) }
    var department by remember { mutableStateOf(selectedUser?.department ?: "Manufacturing & Assembly") }
    var priority by remember { mutableStateOf(TaskPriority.MEDIUM.name) }
    var unitsText by remember { mutableStateOf("1.0") }
    var baseRateText by remember { mutableStateOf(selectedUser?.perWorkRate?.toString() ?: "40.0") }
    var isHoliday by remember { mutableStateOf(false) }
    var holidayMultiplierText by remember { mutableStateOf(selectedUser?.holidayRateMultiplier?.toString() ?: "1.5") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var userExpanded by remember { mutableStateOf(false) }

    val units = unitsText.toDoubleOrNull() ?: 1.0
    val rate = baseRateText.toDoubleOrNull() ?: 0.0
    val mult = if (isHoliday) (holidayMultiplierText.toDoubleOrNull() ?: 1.5) else 1.0
    val projectedPay = units * rate * mult

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Assign Work / Task", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g. Precision Controller Assembly Batch") },
                    modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = userExpanded,
                    onExpandedChange = { userExpanded = !userExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedUser?.let { "${it.fullName} (${it.role})" } ?: "Select Employee",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned Employee *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor().testTag("assign_user_dropdown")
                    )
                    ExposedDropdownMenu(
                        expanded = userExpanded,
                        onDismissRequest = { userExpanded = false }
                    ) {
                        staffList.forEach { user ->
                            DropdownMenuItem(
                                text = { Text("${user.fullName} (${user.jobPosition}) - Rate: $${user.perWorkRate}") },
                                onClick = {
                                    selectedUser = user
                                    department = user.department
                                    baseRateText = user.perWorkRate.toString()
                                    holidayMultiplierText = user.holidayRateMultiplier.toString()
                                    userExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = unitsText,
                        onValueChange = { unitsText = it },
                        label = { Text("Work Units *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("task_units_input")
                    )
                    OutlinedTextField(
                        value = baseRateText,
                        onValueChange = { baseRateText = it },
                        label = { Text("Rate / Unit ($) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("task_rate_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isHoliday) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Holiday Overtime Task", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Applies holiday overtime multiplier", style = MaterialTheme.typography.bodySmall)
                            }
                            Switch(
                                checked = isHoliday,
                                onCheckedChange = { isHoliday = it },
                                modifier = Modifier.testTag("holiday_work_switch")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Calculated Work Pay:", fontWeight = FontWeight.Bold)
                        Text(
                            "$${String.format(Locale.US, "%.2f", projectedPay)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Task Description / Specifications") },
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = RoseExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val assigned = selectedUser
                    val unitsVal = unitsText.toDoubleOrNull()
                    val rateVal = baseRateText.toDoubleOrNull()
                    val multVal = holidayMultiplierText.toDoubleOrNull() ?: 1.0

                    if (title.isBlank()) {
                        error = "Please enter task title"
                    } else if (assigned == null) {
                        error = "Please select an employee"
                    } else if (unitsVal == null || unitsVal <= 0 || rateVal == null || rateVal < 0) {
                        error = "Please enter valid units and rate"
                    } else {
                        val due = System.currentTimeMillis() + 86400000L // 24h
                        onConfirm(title, description, assigned.id, department, priority, unitsVal, rateVal, isHoliday, multVal, due, notes)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_task_button")
            ) {
                Text("Assign Work")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 5. REQUEST PAY DIALOG
@Composable
fun RequestPayDialog(
    maxBalanceDue: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, requestType: String, reason: String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (maxBalanceDue > 0) String.format(Locale.US, "%.2f", maxBalanceDue) else "") }
    var requestType by remember { mutableStateOf("Work Payout") }
    var reason by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val types = listOf("Work Payout", "Salary Advance", "Holiday Bonus", "Expense Reimbursement")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachMoney, contentDescription = null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Pay Request", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Available Earned Balance:", style = MaterialTheme.typography.labelSmall)
                        Text(
                            "$${String.format(Locale.US, "%.2f", maxBalanceDue)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = EmeraldGreen
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Requested Amount ($) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("pay_request_amount_input"),
                    leadingIcon = { Text("$", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) }
                )

                Spacer(modifier = Modifier.height(10.dp))
                Text("Request Type", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    types.take(2).forEach { t ->
                        FilterChip(
                            selected = requestType == t,
                            onClick = { requestType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason / Note for Admin *") },
                    modifier = Modifier.fillMaxWidth().testTag("pay_request_reason_input")
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = RoseExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        error = "Please enter valid payout amount"
                    } else if (reason.isBlank()) {
                        error = "Please provide reason/note"
                    } else {
                        onConfirm(amount, requestType, reason)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_pay_request_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 6. RESOLVE PAY REQUEST DIALOG
@Composable
fun ResolvePayRequestDialog(
    request: PayRequestEntity,
    onDismiss: () -> Unit,
    onResolve: (status: PayRequestStatus, note: String, paymentRef: String) -> Unit
) {
    var adminNote by remember { mutableStateOf(request.adminNote) }
    var paymentRef by remember { mutableStateOf(request.paymentReference.ifEmpty { "TXN-${System.currentTimeMillis() % 100000}" }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Review Pay Request #${request.id}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text("Employee: ${request.userName}", fontWeight = FontWeight.Bold)
                Text("Amount: $${String.format(Locale.US, "%.2f", request.amount)}", fontSize = 20.sp, color = EmeraldGreen, fontWeight = FontWeight.ExtraBold)
                Text("Type: ${request.requestType}", style = MaterialTheme.typography.bodySmall)
                Text("Reason: ${request.reason}", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = paymentRef,
                    onValueChange = { paymentRef = it },
                    label = { Text("Payment Reference / Check #") },
                    modifier = Modifier.fillMaxWidth().testTag("payment_ref_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = adminNote,
                    onValueChange = { adminNote = it },
                    label = { Text("Admin Note / Transaction Memo") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_note_input")
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onResolve(PayRequestStatus.PAID, adminNote, paymentRef)
                        onDismiss()
                    },
                    modifier = Modifier.testTag("mark_paid_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Disburse Payment")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onResolve(PayRequestStatus.REJECTED, adminNote, paymentRef)
                        onDismiss()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseExpense)
                ) {
                    Text("Reject")
                }
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}

// 7. ADD USER DIALOG
@Composable
fun AddUserDialog(
    managers: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirm: (
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
    ) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.EMPLOYEE) }
    var pin by remember { mutableStateOf("1234") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("EMP-${(100..999).random()}") }
    var jobPosition by remember { mutableStateOf("Assembly Technician") }
    var department by remember { mutableStateOf("Manufacturing & Assembly") }
    var paymentType by remember { mutableStateOf("PER_WORK") }
    var perWorkRateText by remember { mutableStateOf("38.0") }
    var holidayMultiplierText by remember { mutableStateOf("1.5") }
    var selectedManagerId by remember { mutableStateOf<Long?>(managers.firstOrNull()?.id) }
    val selectedPermissions = remember { mutableStateListOf<AppPermission>() }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = BrandBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Team Member & Assign Role", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth().testTag("user_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username *") },
                        modifier = Modifier.weight(1f).testTag("user_username_input"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6) pin = it },
                        label = { Text("Access PIN *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.weight(1f).testTag("user_pin_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Assigned Role", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    UserRole.entries.forEach { r ->
                        FilterChip(
                            selected = role == r,
                            onClick = {
                                role = r
                                selectedPermissions.clear()
                                if (r == UserRole.SUPER_ADMIN || r == UserRole.ADMIN) {
                                    selectedPermissions.addAll(AppPermission.entries)
                                } else if (r == UserRole.MANAGER) {
                                    selectedPermissions.addAll(listOf(
                                        AppPermission.VIEW_DASHBOARD, AppPermission.MANAGE_SALES, AppPermission.MANAGE_BUYS,
                                        AppPermission.MANAGE_EXPENSES, AppPermission.MANAGE_WORK, AppPermission.APPROVE_PAYMENTS, AppPermission.VIEW_REPORTS
                                    ))
                                }
                            },
                            label = { Text(r.name, fontSize = 9.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        label = { Text("Employee ID") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = jobPosition,
                        onValueChange = { jobPosition = it },
                        label = { Text("Job Position") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = department,
                    onValueChange = { department = it },
                    label = { Text("Department") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = perWorkRateText,
                        onValueChange = { perWorkRateText = it },
                        label = { Text("Per-Work Pay ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f).testTag("user_rate_input")
                    )
                    OutlinedTextField(
                        value = holidayMultiplierText,
                        onValueChange = { holidayMultiplierText = it },
                        label = { Text("Holiday Rate (x)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = RoseExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = perWorkRateText.toDoubleOrNull() ?: 35.0
                    val holMult = holidayMultiplierText.toDoubleOrNull() ?: 1.5
                    if (fullName.isBlank() || username.isBlank() || pin.length < 4) {
                        error = "Please enter full name, username, and at least 4-digit PIN"
                    } else {
                        onConfirm(
                            username, fullName, role, pin, email.ifEmpty { "$username@bizops.local" },
                            phone, address, employeeId, jobPosition, department, paymentType,
                            rate, holMult, selectedManagerId, selectedPermissions.toList(), notes
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_user_button")
            ) {
                Text("Create User")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// 8. ADD HOLIDAY DIALOG
@Composable
fun AddHolidayDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, dateMillis: Long, desc: String, isPaid: Boolean, dept: String, multiplier: Double, flatBonus: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var isPaid by remember { mutableStateOf(true) }
    var dept by remember { mutableStateOf("All") }
    var multiplierText by remember { mutableStateOf("1.5") }
    var flatBonusText by remember { mutableStateOf("50.0") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Celebration, contentDescription = null, tint = PurpleAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Statutory / Company Holiday", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Holiday Name *") },
                    placeholder = { Text("e.g. National Day") },
                    modifier = Modifier.fillMaxWidth().testTag("holiday_name_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Policy Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paid Holiday Policy", fontWeight = FontWeight.Bold)
                    Switch(checked = isPaid, onCheckedChange = { isPaid = it })
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = multiplierText,
                        onValueChange = { multiplierText = it },
                        label = { Text("Wage Multiplier (x)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = flatBonusText,
                        onValueChange = { flatBonusText = it },
                        label = { Text("Flat Bonus ($)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = RoseExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mult = multiplierText.toDoubleOrNull() ?: 1.5
                    val bonus = flatBonusText.toDoubleOrNull() ?: 0.0
                    if (name.isBlank()) {
                        error = "Please enter holiday name"
                    } else {
                        onConfirm(name, System.currentTimeMillis() + 86400000L * 7, desc, isPaid, dept, mult, bonus)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("submit_holiday_button"),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
            ) {
                Text("Save Holiday")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
