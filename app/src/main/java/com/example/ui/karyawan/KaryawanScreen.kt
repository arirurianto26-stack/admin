package com.example.ui.karyawan

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApprovalRequest
import com.example.data.model.AttendanceLog
import com.example.data.model.Employee
import com.example.ui.components.MaintenanceBanner
import com.example.viewmodel.GpsStatus
import com.example.viewmodel.KaryawanModule
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KaryawanScreen(
    currentEmployee: Employee?,
    allEmployees: List<Employee>,
    attendanceLogs: List<AttendanceLog>,
    approvalRequests: List<ApprovalRequest>,
    isMaintenanceMode: Boolean,
    announcement: String,
    officeName: String,
    officeRadius: String,
    activeModule: KaryawanModule?,
    gpsStatus: GpsStatus,
    gpsDetails: String,
    attendanceFeedback: String?,
    leaveFeedback: String?,
    overtimeFeedback: String?,
    slipFeedback: String?,
    onSwitchEmployee: (String) -> Unit,
    onOpenModule: (KaryawanModule) -> Unit,
    onCloseModule: () -> Unit,
    onSubmitLeave: (String, String) -> Unit,
    onRecordAttendance: (String) -> Unit,
    onSubmitOvertime: (String, String) -> Unit,
    onCheckSlip: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Realtime live clock
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy • HH:mm:ss 'WIB'", Locale("id", "ID"))
            currentTimeString = sdf.format(Date())
            delay(1000)
        }
    }

    // Filter personal history
    val myAttendanceLogs = attendanceLogs.filter { it.employeeNik == currentEmployee?.nik }
    val myApprovalRequests = approvalRequests.filter { it.employeeNik == currentEmployee?.nik }

    val now = System.currentTimeMillis()
    val todayCheckIn = myAttendanceLogs.firstOrNull { 
        it.type.contains("In", ignoreCase = true) && isSameDay(it.timestamp, now) 
    }
    val todayCheckOut = myAttendanceLogs.firstOrNull { 
        it.type.contains("Out", ignoreCase = true) && isSameDay(it.timestamp, now) 
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("karyawan_screen")
    ) {
        // 1. Blue Curved Header with User Switcher
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Top bar: Account Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Portal Karyawan",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Employee Account Switcher Dropdown
                    var showUserDropdown by remember { mutableStateOf(false) }
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .clickable { showUserDropdown = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ganti Akun ▾",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = showUserDropdown,
                            onExpandedChange = { showUserDropdown = !showUserDropdown }
                        ) {
                            ExposedDropdownMenu(
                                expanded = showUserDropdown,
                                onDismissRequest = { showUserDropdown = false }
                            ) {
                                allEmployees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(emp.nama, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("${emp.nik} • ${emp.jabatan}", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            onSwitchEmployee(emp.nik)
                                            showUserDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Profile Name and Designation
                Text(
                    text = currentEmployee?.nama ?: "Ari Rurianto",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${currentEmployee?.nik ?: "OTSM23E27A001"} • ${currentEmployee?.jabatan ?: "Staff IT"}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = currentEmployee?.departemen ?: "Technology & HC",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Running Live Clock
                if (currentTimeString.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "⏱ $currentTimeString",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Content Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2. Broadcast Announcement Banner
            if (announcement.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = announcement,
                            fontSize = 11.sp,
                            color = Color(0xFF1E3A8A),
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. Quick Stats / KPI Cards (3 Cards Row)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Kuota Cuti
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Sisa Cuti", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${currentEmployee?.sisaCuti ?: 12} Hari",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                // Presensi Bulan Ini
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Kehadiran", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${myAttendanceLogs.size + 18} Hari",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                // Lembur Bulan Ini
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Jam Lembur", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(
                            text = "4.0 Jam",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706)
                        )
                    }
                }
            }

            // Maintenance Banner (if enabled)
            MaintenanceBanner(isVisible = isMaintenanceMode)

            // 6. Dynamic Content (Menu Grid or Active Module View)
            AnimatedContent(
                targetState = activeModule,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "karyawanContent"
            ) { targetModule ->
                if (targetModule == null) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Quick Today Attendance Status Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenModule(KaryawanModule.ATTENDANCE) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📍", fontSize = 16.sp)
                                    }
                                    Column {
                                        Text(
                                            text = "Status Presensi Hari Ini",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D)
                                        )
                                        val statusMasuk = if (todayCheckIn != null) {
                                            "In: ${todayCheckIn.timeString.substringAfter("- ").trim()}"
                                        } else "In: Belum ⏳"
                                        val statusKeluar = if (todayCheckOut != null) {
                                            "Out: ${todayCheckOut.timeString.substringAfter("- ").trim()}"
                                        } else "Out: Belum ⏳"

                                        Text(
                                            text = "$statusMasuk • $statusKeluar",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF16A34A))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (todayCheckIn != null && todayCheckOut != null) "Selesai ✅" else "Buka Absen ➔",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Home Menu Grid (5 Menu: Leave, Attendance, Overtime, Slip, History)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    KaryawanMenuItem(
                                        title = "Leave & FDA",
                                        icon = "✈️",
                                        tag = "menu_leave",
                                        onClick = { onOpenModule(KaryawanModule.LEAVE) }
                                    )
                                    KaryawanMenuItem(
                                        title = "Absensi GPS",
                                        icon = "📍",
                                        tag = "menu_attendance",
                                        onClick = { onOpenModule(KaryawanModule.ATTENDANCE) }
                                    )
                                    KaryawanMenuItem(
                                        title = "Lembur",
                                        icon = "⏰",
                                        tag = "menu_overtime",
                                        onClick = { onOpenModule(KaryawanModule.OVERTIME) }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    KaryawanMenuItem(
                                        title = "Slip Gaji",
                                        icon = "💰",
                                        tag = "menu_slip",
                                        onClick = { onOpenModule(KaryawanModule.SLIP) }
                                    )
                                    KaryawanMenuItem(
                                        title = "Riwayat Saya",
                                        icon = "📑",
                                        tag = "menu_history",
                                        onClick = { onOpenModule(KaryawanModule.HISTORY) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Active Module Screen
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Header bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (targetModule) {
                                        KaryawanModule.LEAVE -> "Formulir Leave, FDA & Sakit"
                                        KaryawanModule.ATTENDANCE -> "Absensi GPS Geofencing"
                                        KaryawanModule.OVERTIME -> "Pengajuan Lembur (Overtime)"
                                        KaryawanModule.SLIP -> "Rincian Slip Gaji Digital"
                                        KaryawanModule.HISTORY -> "Riwayat Pengajuan & Presensi"
                                    },
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = onCloseModule,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 10.dp,
                                        vertical = 4.dp
                                    )
                                ) {
                                    Text("Kembali", color = Color(0xFF334155), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            when (targetModule) {
                                KaryawanModule.LEAVE -> {
                                    LeaveModuleContent(
                                        sisaCuti = currentEmployee?.sisaCuti ?: 12,
                                        feedback = leaveFeedback,
                                        onSubmit = onSubmitLeave
                                    )
                                }
                                KaryawanModule.ATTENDANCE -> {
                                    AttendanceModuleContent(
                                        officeName = officeName,
                                        officeRadius = officeRadius,
                                        gpsStatus = gpsStatus,
                                        gpsDetails = gpsDetails,
                                        feedback = attendanceFeedback,
                                        todayCheckIn = todayCheckIn,
                                        todayCheckOut = todayCheckOut,
                                        onRecord = onRecordAttendance
                                    )
                                }
                                KaryawanModule.OVERTIME -> {
                                    OvertimeModuleContent(
                                        feedback = overtimeFeedback,
                                        onSubmit = onSubmitOvertime
                                    )
                                }
                                KaryawanModule.SLIP -> {
                                    SlipModuleContent(
                                        employeeName = currentEmployee?.nama ?: "Ari Rurianto",
                                        employeeEmail = currentEmployee?.email ?: "karyawan@company.com",
                                        employeeNik = currentEmployee?.nik ?: "OTSM23E27A001",
                                        employeeJabatan = currentEmployee?.jabatan ?: "Staff IT",
                                        feedback = slipFeedback,
                                        onCheck = onCheckSlip
                                    )
                                }
                                KaryawanModule.HISTORY -> {
                                    HistoryModuleContent(
                                        attendanceLogs = myAttendanceLogs,
                                        approvalRequests = myApprovalRequests
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun KaryawanMenuItem(
    title: String,
    icon: String,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(6.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F2FE)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 20.sp)
        }
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaveModuleContent(
    sisaCuti: Int,
    feedback: String?,
    onSubmit: (String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf("Cuti Tahunan") }
    var durationDays by remember { mutableStateOf("1") }
    var reason by remember { mutableStateOf("") }
    var expandedTypeMenu by remember { mutableStateOf(false) }

    val types = listOf(
        "Cuti Tahunan",
        "FDA (Formulir Dinas Luar Kantor)",
        "Izin Sakit (Surat Dokter)",
        "Izin Khusus / Keperluan Mendesak"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Jenis Pengajuan:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
            Text("Sisa Cuti Anda: $sisaCuti Hari", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
        }

        ExposedDropdownMenuBox(
            expanded = expandedTypeMenu,
            onExpandedChange = { expandedTypeMenu = !expandedTypeMenu },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTypeMenu) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
            )
            ExposedDropdownMenu(
                expanded = expandedTypeMenu,
                onDismissRequest = { expandedTypeMenu = false }
            ) {
                types.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type, fontSize = 11.sp) },
                        onClick = {
                            selectedType = type
                            expandedTypeMenu = false
                        }
                    )
                }
            }
        }

        Text("Durasi Hari:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        OutlinedTextField(
            value = durationDays,
            onValueChange = { durationDays = it },
            placeholder = { Text("Jumlah hari...", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
        )

        Text("Keterangan & Alasan Pengajuan:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            placeholder = { Text("Contoh: Keperluan keluarga mendesak...", fontSize = 11.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_leave_reason"),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
        )

        Button(
            onClick = {
                val fullReason = "$reason (Durasi: $durationDays Hari)"
                onSubmit(selectedType, fullReason)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("btn_submit_leave")
        ) {
            Text("Kirim Pengajuan ke HRD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (feedback != null) {
            Text(
                text = feedback,
                color = if (feedback.contains("✅")) Color(0xFF047857) else Color(0xFFEF4444),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AttendanceModuleContent(
    officeName: String,
    officeRadius: String,
    gpsStatus: GpsStatus,
    gpsDetails: String,
    feedback: String?,
    todayCheckIn: AttendanceLog?,
    todayCheckOut: AttendanceLog?,
    onRecord: (String) -> Unit
) {
    val isBothCompleted = todayCheckIn != null && todayCheckOut != null
    val hasCheckedIn = todayCheckIn != null
    val hasCheckedOut = todayCheckOut != null

    val sdfDate = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID")) }
    val todayFormatted = remember { sdfDate.format(Date()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Office Geofence Target Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("🏢 Titik Geofencing Kantor:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text(officeName, fontSize = 11.sp, color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
                Text("Radius Validasi Maksimal: $officeRadius Meter", fontSize = 10.sp, color = Color(0xFF64748B))
            }
        }

        // Today Attendance Status & Anti Double Absen Tracker Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Status Presensi Hari Ini",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = todayFormatted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                // 2 Status Cards (Check In & Check Out)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Check In Status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (hasCheckedIn) Color(0xFFDCFCE7) else Color(0xFFF1F5F9))
                            .border(1.dp, if (hasCheckedIn) Color(0xFF86EFAC) else Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Check In (Masuk)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasCheckedIn) Color(0xFF15803D) else Color(0xFF64748B)
                            )
                            if (hasCheckedIn) {
                                val timeIn = todayCheckIn?.timeString?.substringAfter("- ")?.trim() ?: "Tercatat"
                                Text(
                                    text = "✅ Sudah ($timeIn)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            } else {
                                Text(
                                    text = "⏳ Belum Absen",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }

                    // Check Out Status
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (hasCheckedOut) Color(0xFFDCFCE7) 
                                else if (hasCheckedIn) Color(0xFFE0F2FE) 
                                else Color(0xFFF1F5F9)
                            )
                            .border(
                                1.dp, 
                                if (hasCheckedOut) Color(0xFF86EFAC) 
                                else if (hasCheckedIn) Color(0xFFBAE6FD) 
                                else Color(0xFFCBD5E1), 
                                RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Check Out (Pulang)",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (hasCheckedOut) Color(0xFF15803D) else if (hasCheckedIn) Color(0xFF0369A1) else Color(0xFF64748B)
                            )
                            if (hasCheckedOut) {
                                val timeOut = todayCheckOut?.timeString?.substringAfter("- ")?.trim() ?: "Tercatat"
                                Text(
                                    text = "✅ Sudah ($timeOut)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            } else if (hasCheckedIn) {
                                Text(
                                    text = "🔓 Siap Absen",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0284C7)
                                )
                            } else {
                                Text(
                                    text = "🔒 Perlu Check In",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }

                // Anti Double Absen Policy Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Kebijakan Sistem: Maksimal 1x Check In & 1x Check Out per hari.",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Completion Banner if both done
        if (isBothCompleted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFDCFCE7))
                    .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Presensi Hari Ini Lengkap! 🎉",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                        val inTime = todayCheckIn?.timeString?.substringAfter("- ")?.trim() ?: "-"
                        val outTime = todayCheckOut?.timeString?.substringAfter("- ")?.trim() ?: "-"
                        Text(
                            text = "Anda telah menyelesaikan Check In ($inTime) dan Check Out ($outTime). Sistem memproteksi dari absensi ganda.",
                            fontSize = 9.sp,
                            color = Color(0xFF166534),
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }

        // GPS Geolocation Status
        when (gpsStatus) {
            GpsStatus.SCANNING -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFD97706)
                    )
                    Text(
                        text = "🔄 Memvalidasi radius satelit GPS...",
                        color = Color(0xFFD97706),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            GpsStatus.SUCCESS -> {
                Text(
                    text = "✅ Lokasi Valid! Anda berada dalam radius presensi kantor.",
                    color = Color(0xFF047857),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            GpsStatus.IDLE -> {
                Text(
                    text = "📍 Menyiapkan GPS Satelit...",
                    color = Color(0xFF64748B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Text(
            text = gpsDetails,
            fontSize = 10.sp,
            color = Color(0xFF64748B)
        )

        // Gate Time status button
        val isLocked = gpsStatus != GpsStatus.SUCCESS
        Button(
            onClick = {},
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = if (isLocked) Color(0xFF94A3B8) else Color(0xFF22C55E),
                disabledContentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isLocked) "Gate Time: Belum Terbuka" else "Gate Time: Terbuka (Shift Pagi)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (gpsStatus == GpsStatus.SUCCESS) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Check In Action Button
                val canCheckIn = !hasCheckedIn
                Button(
                    onClick = { onRecord("Check In") },
                    enabled = canCheckIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        disabledContainerColor = Color(0xFFE2E8F0),
                        disabledContentColor = Color(0xFF64748B)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_check_in")
                ) {
                    if (hasCheckedIn) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sudah Check In", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("📥 Check In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Check Out Action Button
                val canCheckOut = hasCheckedIn && !hasCheckedOut
                Button(
                    onClick = { onRecord("Check Out") },
                    enabled = canCheckOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF43F5E),
                        disabledContainerColor = Color(0xFFE2E8F0),
                        disabledContentColor = Color(0xFF94A3B8)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_check_out")
                ) {
                    if (hasCheckedOut) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sudah Check Out", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    } else if (!hasCheckedIn) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Check Out (Kunci)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("📤 Check Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feedback Notice
        if (feedback != null) {
            val isSuccess = feedback.contains("✅")
            val isWarning = feedback.contains("⚠️")
            val bgColor = if (isSuccess) Color(0xFFDCFCE7) else if (isWarning) Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
            val textColor = if (isSuccess) Color(0xFF15803D) else if (isWarning) Color(0xFFB45309) else Color(0xFFB91C1C)
            val borderColor = if (isSuccess) Color(0xFF86EFAC) else if (isWarning) Color(0xFFFCD34D) else Color(0xFFFCA5A5)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = feedback,
                    color = textColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun OvertimeModuleContent(
    feedback: String?,
    onSubmit: (String, String) -> Unit
) {
    var durationHours by remember { mutableStateOf("2") }
    var taskDescription by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Jumlah Jam Lembur:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        OutlinedTextField(
            value = durationHours,
            onValueChange = { durationHours = it },
            placeholder = { Text("Contoh: 2", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
        )

        // Estimasi insentif
        val hours = durationHours.toIntOrNull() ?: 0
        val estimasi = hours * 35000
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFFEF3C7))
                .padding(8.dp)
        ) {
            Text(
                text = "💰 Estimasi Insentif Lembur: Rp ${String.format("%,d", estimasi).replace(',', '.')}",
                color = Color(0xFFB45309),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text("Uraian Pekerjaan / Target Lembur:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        OutlinedTextField(
            value = taskDescription,
            onValueChange = { taskDescription = it },
            placeholder = { Text("Jelaskan pekerjaan yang diselesaikan...", fontSize = 11.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_overtime_desc"),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
        )

        Button(
            onClick = { onSubmit(durationHours, taskDescription) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .testTag("btn_submit_overtime")
        ) {
            Text("Ajukan Lembur ke HRD", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        if (feedback != null) {
            Text(
                text = feedback,
                color = if (feedback.contains("✅")) Color(0xFF047857) else Color(0xFFEF4444),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlipModuleContent(
    employeeName: String,
    employeeEmail: String,
    employeeNik: String,
    employeeJabatan: String,
    feedback: String?,
    onCheck: (String) -> Unit
) {
    var email by remember { mutableStateOf(employeeEmail) }
    var selectedMonth by remember { mutableStateOf("Agustus 2026") }
    var expandedMonth by remember { mutableStateOf(false) }

    val months = listOf("Agustus 2026", "Juli 2026", "Juni 2026", "Mei 2026")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Pilih Periode Penggajian:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))

        ExposedDropdownMenuBox(
            expanded = expandedMonth,
            onExpandedChange = { expandedMonth = !expandedMonth },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedMonth,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
            )
            ExposedDropdownMenu(
                expanded = expandedMonth,
                onDismissRequest = { expandedMonth = false }
            ) {
                months.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m, fontSize = 11.sp) },
                        onClick = {
                            selectedMonth = m
                            expandedMonth = false
                        }
                    )
                }
            }
        }

        // Rincian Breakdown Slip Gaji
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(employeeName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(selectedMonth, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                }
                Text("NIK: $employeeNik • $employeeJabatan", fontSize = 10.sp, color = Color(0xFF64748B))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                Text("Penerimaan (Earnings):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Gaji Pokok", fontSize = 10.sp, color = Color(0xFF475569))
                    Text("Rp 6.800.000", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Tunjangan Jabatan & Kehadiran", fontSize = 10.sp, color = Color(0xFF475569))
                    Text("Rp 1.500.000", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Upah Lembur (Overtime)", fontSize = 10.sp, color = Color(0xFF475569))
                    Text("Rp 140.000", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }

                Text("Potongan (Deductions):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• BPJS Ketenagakerjaan & Kesehatan", fontSize = 10.sp, color = Color(0xFFDC2626))
                    Text("-Rp 320.000", fontSize = 10.sp, color = Color(0xFFDC2626))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• PPh 21 Terutang", fontSize = 10.sp, color = Color(0xFFDC2626))
                    Text("-Rp 100.000", fontSize = 10.sp, color = Color(0xFFDC2626))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFE2E8F0))
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Take Home Pay (Bersih):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("Rp 8.020.000", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF047857))
                }
            }
        }

        Text("Kirim Dokumen Resmi ke Email:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("karyawan@company.com", fontSize = 11.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
        )

        Button(
            onClick = { onCheck(email) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
                .testTag("btn_check_slip")
        ) {
            Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Kirim PDF ke Email & Unduh", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        if (feedback != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(8.dp)
            ) {
                Text(
                    text = feedback,
                    color = Color(0xFF15803D),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun HistoryModuleContent(
    attendanceLogs: List<AttendanceLog>,
    approvalRequests: List<ApprovalRequest>
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Pengajuan Saya, 1: Presensi Saya

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE2E8F0))
                .padding(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedTab == 0) Color.White else Color.Transparent)
                    .clickable { selectedTab = 0 }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Status Pengajuan (${approvalRequests.size})",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == 0) Color(0xFF0F172A) else Color(0xFF64748B)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (selectedTab == 1) Color.White else Color.Transparent)
                    .clickable { selectedTab = 1 }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Log Presensi (${attendanceLogs.size})",
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == 1) Color(0xFF0F172A) else Color(0xFF64748B)
                )
            }
        }

        if (selectedTab == 0) {
            if (approvalRequests.isEmpty()) {
                Text(
                    text = "Belum ada pengajuan cuti, FDA, atau lembur.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                approvalRequests.forEach { req ->
                    val isApproved = req.status == "Approved"
                    val isRejected = req.status == "Rejected"
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(req.type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (isApproved) Color(0xFFDCFCE7)
                                            else if (isRejected) Color(0xFFFEE2E2)
                                            else Color(0xFFFEF3C7)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isApproved) "Disetujui" else if (isRejected) "Ditolak" else "Menunggu",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isApproved) Color(0xFF15803D) else if (isRejected) Color(0xFFB91C1C) else Color(0xFFB45309)
                                    )
                                }
                            }
                            Text("Alasan: ${req.reason}", fontSize = 10.sp, color = Color(0xFF475569))
                            Text("Tanggal: ${req.timeString}", fontSize = 9.sp, color = Color(0xFF94A3B8))
                            if (req.reviewerNote.isNotBlank()) {
                                Text("Catatan HRD: ${req.reviewerNote}", fontSize = 9.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        } else {
            if (attendanceLogs.isEmpty()) {
                Text(
                    text = "Belum ada catatan log presensi hari ini.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                attendanceLogs.forEach { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.type, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text(log.timeString, fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Terekam GPS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    }
                }
            }
        }
    }
}
