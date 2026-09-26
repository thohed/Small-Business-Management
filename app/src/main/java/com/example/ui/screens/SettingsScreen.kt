package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BusinessSettingsEntity
import com.example.data.local.UserEntity
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.EmeraldGreen

@Composable
fun SettingsScreen(
    currentUser: UserEntity,
    currentSettings: BusinessSettingsEntity?,
    onBack: () -> Unit,
    onSaveSettings: (BusinessSettingsEntity) -> Unit
) {
    BackHandler { onBack() }

    var businessName by remember(currentSettings) { mutableStateOf(currentSettings?.businessName ?: "Apex Industrial & Tech") }
    var currencySymbol by remember(currentSettings) { mutableStateOf(currentSettings?.currencySymbol ?: "$") }
    var currencyCode by remember(currentSettings) { mutableStateOf(currentSettings?.currencyCode ?: "USD") }
    var taxRateText by remember(currentSettings) { mutableStateOf(currentSettings?.taxRate?.toString() ?: "5.0") }
    var timeZone by remember(currentSettings) { mutableStateOf(currentSettings?.timeZone ?: "EST") }
    var autoDetectLateWork by remember(currentSettings) { mutableStateOf(currentSettings?.autoDetectLateWork ?: true) }
    var allowPayRequests by remember(currentSettings) { mutableStateOf(currentSettings?.allowEmployeePayRequests ?: true) }
    var defaultHolidayMultiplierText by remember(currentSettings) { mutableStateOf(currentSettings?.defaultHolidayMultiplier?.toString() ?: "1.5") }
    var saveSuccess by remember { mutableStateOf(false) }

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("settings_screen_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Business Configuration & Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Currency, thresholds, automation & operational rules",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // General Business Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Business, contentDescription = null, tint = BrandBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Company & Financial Preferences", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business / Enterprise Name *") },
                            modifier = Modifier.fillMaxWidth().testTag("settings_business_name_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = currencySymbol,
                                onValueChange = { currencySymbol = it },
                                label = { Text("Currency Symbol") },
                                modifier = Modifier.weight(1f).testTag("settings_currency_symbol_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = currencyCode,
                                onValueChange = { currencyCode = it.uppercase() },
                                label = { Text("Currency Code") },
                                modifier = Modifier.weight(1f).testTag("settings_currency_code_input"),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = taxRateText,
                                onValueChange = { taxRateText = it },
                                label = { Text("Default Tax Rate (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = timeZone,
                                onValueChange = { timeZone = it },
                                label = { Text("Time Zone") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Operational Rules & Automation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Workforce & Payment Automation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Automated Late Work Detection", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Flag uncompleted tasks past due date as LATE", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = autoDetectLateWork,
                                onCheckedChange = { autoDetectLateWork = it },
                                modifier = Modifier.testTag("switch_auto_detect_late")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Allow Employee Pay Requests", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text("Staff can submit digital requests against earned balance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(
                                checked = allowPayRequests,
                                onCheckedChange = { allowPayRequests = it },
                                modifier = Modifier.testTag("switch_allow_pay_requests")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = defaultHolidayMultiplierText,
                            onValueChange = { defaultHolidayMultiplierText = it },
                            label = { Text("Default Holiday Wage Multiplier (e.g. 1.5x)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Save Action
            item {
                if (saveSuccess) {
                    Text(
                        text = "✓ Settings successfully updated and recorded in audit log.",
                        color = EmeraldGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        val tax = taxRateText.toDoubleOrNull() ?: 0.0
                        val holMult = defaultHolidayMultiplierText.toDoubleOrNull() ?: 1.5
                        val updated = BusinessSettingsEntity(
                            id = 1,
                            businessName = businessName.trim().ifEmpty { "BizOps Enterprise" },
                            currencySymbol = currencySymbol.trim().ifEmpty { "$" },
                            currencyCode = currencyCode.trim().ifEmpty { "USD" },
                            taxRate = tax,
                            timeZone = timeZone.trim().ifEmpty { "UTC" },
                            dateFormat = "MMM d, yyyy",
                            autoDetectLateWork = autoDetectLateWork,
                            allowEmployeePayRequests = allowPayRequests,
                            defaultHolidayMultiplier = holMult
                        )
                        onSaveSettings(updated)
                        saveSuccess = true
                    },
                    modifier = Modifier.fillMaxWidth().testTag("btn_save_settings"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Business Settings", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
