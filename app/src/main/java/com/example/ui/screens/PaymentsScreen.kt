package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PayRequestEntity
import com.example.data.local.UserEntity
import com.example.data.model.PayRequestStatus
import com.example.data.model.UserEarningsSummary
import com.example.data.model.UserRole
import com.example.ui.components.ResolvePayRequestDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.RoseContainer
import com.example.ui.theme.RoseExpense
import com.example.ui.theme.Slate700
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PaymentsScreen(
    currentUser: UserEntity,
    payRequests: List<PayRequestEntity>,
    userEarnings: UserEarningsSummary?,
    onBack: () -> Unit,
    onOpenRequestPay: () -> Unit,
    onResolvePayRequest: (requestId: Long, status: PayRequestStatus, note: String, paymentRef: String) -> Unit
) {
    BackHandler { onBack() }

    val isAdminOrManager = currentUser.role == UserRole.ADMIN.name || currentUser.role == UserRole.MANAGER.name
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
    val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

    var selectedRequestToReview by remember { mutableStateOf<PayRequestEntity?>(null) }

    // Display list: Staff sees only their requests; Admin/Manager sees all
    val displayedRequests = if (isAdminOrManager) {
        payRequests
    } else {
        payRequests.filter { it.userId == currentUser.id }
    }

    selectedRequestToReview?.let { req ->
        ResolvePayRequestDialog(
            request = req,
            onDismiss = { selectedRequestToReview = null },
            onResolve = { status, note, ref ->
                onResolvePayRequest(req.id, status, note, ref)
                selectedRequestToReview = null
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenRequestPay,
                containerColor = EmeraldGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_request_pay")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Request Pay")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("payments_back_button")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isAdminOrManager) "Payroll & Pay Requests" else "My Compensation & Payouts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Work earnings, balance due & payout requests",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Staff Earnings Summary Banner
            userEarnings?.let { earnings ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Available Balance Due", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                                Text(
                                    text = currencyFormat.format(earnings.balanceDue),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreen
                                )
                            }
                            Button(
                                onClick = onOpenRequestPay,
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_request_payout_banner")
                            ) {
                                Text("Request Payout")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                text = "Total Earned: ${currencyFormat.format(earnings.grossApprovedEarnings)}",
                                fontSize = 12.sp,
                                color = Slate700,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Paid to Date: ${currencyFormat.format(earnings.totalPaidOut)}",
                                fontSize = 12.sp,
                                color = Slate700,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Payout Requests (${displayedRequests.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isAdminOrManager) {
                    val pendingCount = displayedRequests.count { it.status == PayRequestStatus.PENDING.name }
                    if (pendingCount > 0) {
                        StatusBadge(
                            text = "$pendingCount Pending Review",
                            containerColor = AmberContainer,
                            contentColor = AmberWarning
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Requests List
            if (displayedRequests.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pay requests found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(displayedRequests, key = { it.id }) { request ->
                        val isPending = request.status == PayRequestStatus.PENDING.name

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pay_request_card_${request.id}")
                                .then(
                                    if (isAdminOrManager && isPending) {
                                        Modifier.clickable { selectedRequestToReview = request }
                                    } else Modifier
                                ),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = request.userName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = request.requestType,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = currencyFormat.format(request.amount),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = EmeraldGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        StatusBadge(
                                            text = request.status,
                                            containerColor = when (request.status) {
                                                PayRequestStatus.PAID.name -> EmeraldContainer
                                                PayRequestStatus.APPROVED.name -> BrandBlue.copy(alpha = 0.15f)
                                                PayRequestStatus.REJECTED.name -> RoseContainer
                                                else -> AmberContainer
                                            },
                                            contentColor = when (request.status) {
                                                PayRequestStatus.PAID.name -> EmeraldGreen
                                                PayRequestStatus.APPROVED.name -> BrandBlue
                                                PayRequestStatus.REJECTED.name -> RoseExpense
                                                else -> AmberWarning
                                            }
                                        )
                                    }
                                }

                                if (request.reason.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Reason: ${request.reason}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                if (request.adminNote.isNotEmpty() || request.paymentReference.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            if (request.paymentReference.isNotEmpty()) {
                                                Text(
                                                    text = "Payment Ref: ${request.paymentReference}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            if (request.adminNote.isNotEmpty()) {
                                                Text(
                                                    text = "Memo: ${request.adminNote}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Slate700
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Requested: ${dateFormat.format(Date(request.requestDateMillis))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (isAdminOrManager && isPending) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = { selectedRequestToReview = request },
                                        modifier = Modifier.fillMaxWidth().testTag("review_request_${request.id}"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Review & Approve / Pay")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
