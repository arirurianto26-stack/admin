package com.example.ui.hrd

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApprovalRequest
import com.example.data.model.AttendanceLog
import com.example.data.model.Employee
import com.example.ui.components.AttendanceAnalyticsDashboard
import com.example.ui.components.MaintenanceBanner
import com.example.util.AttendanceExportHelper
import com.example.util.ExportResult
import com.example.viewmodel.HrdModule

@Composable
fun HrdScreen(
    employees: List<Employee>,
    attendanceLogs: List<AttendanceLog>,
    approvalRequests: List<ApprovalRequest>,
    isMaintenanceMode: Boolean,
    activeModule: HrdModule?,
    onOpenModule: (HrdModule) -> Unit,
    onCloseModule: () -> Unit,
    onAddEmployee: (String, String, String, String, String, Int, String, () -> Unit) -> Unit,
    onDeleteEmployee: (Long) -> Unit,
    onProcessApproval: (Long, String, String) -> Unit,
    onApproveResetDevice: (String, String) -> Unit,
    onDeleteAttendance: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
            .testTag("hrd_screen")
    ) {
        // 1. Deep Blue Curved Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF0369A1), Color(0xFF0284C7))
                    ),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Panel Human Capital Management,",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "RISMA DWI YUNITA",
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
                            .background(Color.White.copy(alpha = 0.25f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "HRD-MANAGER-01 • Head of HC",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "${employees.size} Karyawan Aktif",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Content Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Maintenance Banner (if enabled by IT)
            MaintenanceBanner(
                isVisible = isMaintenanceMode,
                title = "Pemberitahuan Sistem IT",
                subtitle = "Sistem berada dalam mode pemeliharaan (Maintenance Mode)."
            )

            // Dynamic Content: Menu Grid or Specific Module Box
            AnimatedContent(
                targetState = activeModule,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "hrdContent"
            ) { targetModule ->
                if (targetModule == null) {
                    // Home Menu Grid
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                HrdMenuItem(
                                    title = "Karyawan",
                                    icon = "👥",
                                    badgeCount = employees.size,
                                    tag = "menu_hrd_karyawan",
                                    onClick = { onOpenModule(HrdModule.KARYAWAN) }
                                )
                                HrdMenuItem(
                                    title = "Laporan",
                                    icon = "📈",
                                    badgeCount = attendanceLogs.size,
                                    tag = "menu_hrd_laporan",
                                    onClick = { onOpenModule(HrdModule.LAPORAN) }
                                )
                                HrdMenuItem(
                                    title = "Approval",
                                    icon = "📝",
                                    badgeCount = approvalRequests.count { it.status == "Pending" } + employees.count { it.statusRequestReset },
                                    tag = "menu_hrd_approval",
                                    onClick = { onOpenModule(HrdModule.APPROVAL) }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                HrdMenuItem(
                                    title = "Analitik",
                                    icon = "📊",
                                    badgeCount = null,
                                    tag = "menu_hrd_analitik",
                                    onClick = { onOpenModule(HrdModule.ANALYTICS) }
                                )
                                HrdMenuItem(
                                    title = "Hapus Absen",
                                    icon = "🗑️",
                                    badgeCount = null,
                                    tag = "menu_hrd_hapusabsen",
                                    onClick = { onOpenModule(HrdModule.HAPUS_ABSEN) }
                                )
                                HrdMenuItem(
                                    title = "Rekap Payroll",
                                    icon = "💰",
                                    badgeCount = null,
                                    tag = "menu_hrd_payroll",
                                    onClick = { onOpenModule(HrdModule.PAYROLL_SUMMARY) }
                                )
                            }
                        }
                    }

                    // Live Analytics & Trends Dashboard on HRD Overview
                    AttendanceAnalyticsDashboard(
                        employees = employees,
                        attendanceLogs = attendanceLogs,
                        approvalRequests = approvalRequests,
                        isDarkTheme = false
                    )
                } else {
                    // Active Module View
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
                                        HrdModule.KARYAWAN -> "Manajemen Karyawan (${employees.size})"
                                        HrdModule.LAPORAN -> "Rekapitulasi Kehadiran & Form"
                                        HrdModule.APPROVAL -> "Pusat Approval & Reset HP"
                                        HrdModule.ANALYTICS -> "Dashboard Analitik & Tren Presensi"
                                        HrdModule.HAPUS_ABSEN -> "Koreksi Log Absensi"
                                        HrdModule.PAYROLL_SUMMARY -> "Ringkasan Rekap Payroll"
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
                                HrdModule.KARYAWAN -> {
                                    HrdKaryawanModule(
                                        employees = employees,
                                        onAddEmployee = onAddEmployee,
                                        onDeleteEmployee = onDeleteEmployee
                                    )
                                }
                                HrdModule.LAPORAN -> {
                                    HrdLaporanModule(
                                        attendanceLogs = attendanceLogs,
                                        approvalRequests = approvalRequests
                                    )
                                }
                                HrdModule.APPROVAL -> {
                                    HrdApprovalModule(
                                        employees = employees,
                                        approvalRequests = approvalRequests,
                                        onProcessApproval = onProcessApproval,
                                        onApproveResetDevice = onApproveResetDevice
                                    )
                                }
                                HrdModule.ANALYTICS -> {
                                    AttendanceAnalyticsDashboard(
                                        employees = employees,
                                        attendanceLogs = attendanceLogs,
                                        approvalRequests = approvalRequests,
                                        isDarkTheme = false
                                    )
                                }
                                HrdModule.HAPUS_ABSEN -> {
                                    HrdHapusAbsenModule(
                                        attendanceLogs = attendanceLogs,
                                        onDeleteAttendance = onDeleteAttendance
                                    )
                                }
                                HrdModule.PAYROLL_SUMMARY -> {
                                    HrdPayrollModule(
                                        employees = employees,
                                        attendanceLogs = attendanceLogs,
                                        approvalRequests = approvalRequests
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
fun HrdMenuItem(
    title: String,
    icon: String,
    badgeCount: Int?,
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
            if (badgeCount != null && badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text("$badgeCount", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF475569)
        )
    }
}

@Composable
fun HrdKaryawanModule(
    employees: List<Employee>,
    onAddEmployee: (String, String, String, String, String, Int, String, () -> Unit) -> Unit,
    onDeleteEmployee: (Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showAddForm by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf("") }
    var inputNik by remember { mutableStateOf("") }
    var inputDevice by remember { mutableStateOf("") }
    var inputJabatan by remember { mutableStateOf("Staff Operasional") }
    var inputDepartemen by remember { mutableStateOf("Technology & HC") }
    var inputSisaCuti by remember { mutableStateOf("12") }
    var inputEmail by remember { mutableStateOf("") }

    val filteredEmployees = employees.filter {
        it.nama.contains(searchQuery, ignoreCase = true) ||
        it.nik.contains(searchQuery, ignoreCase = true) ||
        it.device.contains(searchQuery, ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari nama, NIK, atau HP...", fontSize = 11.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
        )

        Button(
            onClick = { showAddForm = !showAddForm },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_toggle_add_emp")
        ) {
            Text(if (showAddForm) "Tutup Form Pendaftaran" else "+ Tambah Karyawan Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        AnimatedVisibility(visible = showAddForm) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Form Registrasi Karyawan Lengkap", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))

                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        placeholder = { Text("Nama Lengkap", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_emp_name"),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )

                    OutlinedTextField(
                        value = inputNik,
                        onValueChange = { inputNik = it },
                        placeholder = { Text("Nomor NIK (Contoh: OTSM23E27A004)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_emp_nik"),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )

                    OutlinedTextField(
                        value = inputDevice,
                        onValueChange = { inputDevice = it },
                        placeholder = { Text("ID Perangkat HP (Contoh: HP-Rian-Samsung)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_emp_device"),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = inputJabatan,
                            onValueChange = { inputJabatan = it },
                            placeholder = { Text("Jabatan", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                        OutlinedTextField(
                            value = inputDepartemen,
                            onValueChange = { inputDepartemen = it },
                            placeholder = { Text("Departemen", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = inputSisaCuti,
                            onValueChange = { inputSisaCuti = it },
                            placeholder = { Text("Sisa Cuti", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            placeholder = { Text("Email", fontSize = 11.sp) },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val sisa = inputSisaCuti.toIntOrNull() ?: 12
                                onAddEmployee(inputName, inputNik, inputDevice, inputJabatan, inputDepartemen, sisa, inputEmail) {
                                    inputName = ""
                                    inputNik = ""
                                    inputDevice = ""
                                    inputEmail = ""
                                    showAddForm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_save_new_emp")
                        ) {
                            Text("Simpan Data", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showAddForm = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Employee List
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (filteredEmployees.isEmpty()) {
                Text("Tidak ada karyawan yang cocok dengan pencarian.", fontSize = 11.sp, color = Color.Gray)
            } else {
                filteredEmployees.forEach { emp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(emp.nama, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("${emp.nik} • ${emp.jabatan}", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("HP: ${emp.device} • Cuti: ${emp.sisaCuti} Hari", fontSize = 10.sp, color = Color(0xFF0284C7))
                            }

                            Button(
                                onClick = { onDeleteEmployee(emp.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Hapus", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrdLaporanModule(
    attendanceLogs: List<AttendanceLog>,
    approvalRequests: List<ApprovalRequest>
) {
    val context = LocalContext.current
    var filterType by remember { mutableStateOf("Semua") }
    var exportResult by remember { mutableStateOf<ExportResult?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var copyStatusMessage by remember { mutableStateOf<String?>(null) }

    val filterOptions = listOf("Semua", "Presensi", "Cuti & Sakit", "Lembur")

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Summary Metrics Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE0F2FE))
                    .padding(8.dp)
            ) {
                Column {
                    Text("Total Presensi", fontSize = 9.sp, color = Color(0xFF0369A1))
                    Text("${attendanceLogs.size} Log", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFDCFCE7))
                    .padding(8.dp)
            ) {
                Column {
                    Text("Check In", fontSize = 9.sp, color = Color(0xFF15803D))
                    Text("${attendanceLogs.count { it.type.contains("In", ignoreCase = true) }}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(8.dp)
            ) {
                Column {
                    Text("Check Out", fontSize = 9.sp, color = Color(0xFFB45309))
                    Text("${attendanceLogs.count { it.type.contains("Out", ignoreCase = true) }}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                }
            }
        }

        // Export and Download Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
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
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download & Ekspor Riwayat Absensi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                    Text(
                        text = "Format: CSV / Excel",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                }

                Text(
                    text = "Unduh seluruh riwayat absensi karyawan ke dalam format CSV yang kompatibel dengan Microsoft Excel dan Google Sheets.",
                    fontSize = 10.sp,
                    color = Color(0xFF475569),
                    lineHeight = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Primary Download Button
                    Button(
                        onClick = {
                            val csvData = AttendanceExportHelper.generateHrdCsv(attendanceLogs)
                            val res = AttendanceExportHelper.saveExportFile(
                                context = context,
                                prefix = "rekap_absensi_hrd",
                                extension = "csv",
                                content = csvData,
                                recordCount = attendanceLogs.size
                            )
                            exportResult = res
                            copyStatusMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_download_hrd_attendance")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unduh CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share Button
                    Button(
                        onClick = {
                            val csvData = AttendanceExportHelper.generateHrdCsv(attendanceLogs)
                            AttendanceExportHelper.shareExportContent(
                                context = context,
                                title = "Rekap Riwayat Absensi HRD",
                                content = csvData
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    // Copy Button
                    Button(
                        onClick = {
                            val csvData = AttendanceExportHelper.generateHrdCsv(attendanceLogs)
                            val copied = AttendanceExportHelper.copyToClipboard(context, csvData, "Rekap Absensi HRD")
                            if (copied) {
                                copyStatusMessage = "📋 Data CSV berhasil disalin ke clipboard!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF1E293B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    }
                }

                // Copy notification if any
                if (copyStatusMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(6.dp)
                    ) {
                        Text(copyStatusMessage ?: "", color = Color(0xFF0369A1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Download Success Card
                exportResult?.let { res ->
                    if (res.isSuccess) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "File Berhasil Diunduh & Tersimpan!",
                                        color = Color(0xFF15803D),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "📄 Nama: ${res.fileName}\n📊 Jumlah Data: ${res.recordCount} baris presensi\n💾 Ukuran: ${res.formattedSize}\n📁 Lokasi: ${res.fileAbsolutePath}",
                                    color = Color(0xFF166534),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { showPreviewDialog = !showPreviewDialog },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (showPreviewDialog) "Tutup Preview" else "Lihat Isi File", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Preview Expandable
                        if (showPreviewDialog) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F172A))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = res.content.lines().take(10).joinToString("\n") + if (res.content.lines().size > 10) "\n... (${res.content.lines().size - 10} baris lagi)" else "",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(8.dp)
                        ) {
                            Text(res.message, color = Color(0xFFB91C1C), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Filter bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filterOptions.forEach { opt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (filterType == opt) Color(0xFF0284C7) else Color(0xFFE2E8F0))
                        .clickable { filterType = opt }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = opt,
                        fontSize = 10.sp,
                        fontWeight = if (filterType == opt) FontWeight.Bold else FontWeight.Medium,
                        color = if (filterType == opt) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        // List
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (filterType == "Semua" || filterType == "Presensi") {
                attendanceLogs.forEach { log ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.type, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("${log.employeeNama} • ${log.timeString}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Hadir", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    }
                }
            }

            if (filterType == "Semua" || filterType == "Cuti & Sakit" || filterType == "Lembur") {
                val filteredReqs = approvalRequests.filter {
                    if (filterType == "Lembur") it.type.contains("Lembur", ignoreCase = true)
                    else if (filterType == "Cuti & Sakit") !it.type.contains("Lembur", ignoreCase = true)
                    else true
                }
                filteredReqs.forEach { req ->
                    val isApproved = req.status == "Approved"
                    val isRejected = req.status == "Rejected"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req.type, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("${req.employeeNama} • ${req.reason}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
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
                                    text = if (isApproved) "Disetujui" else if (isRejected) "Ditolak" else "Pending",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isApproved) Color(0xFF15803D) else if (isRejected) Color(0xFFB91C1C) else Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HrdApprovalModule(
    employees: List<Employee>,
    approvalRequests: List<ApprovalRequest>,
    onProcessApproval: (Long, String, String) -> Unit,
    onApproveResetDevice: (String, String) -> Unit
) {
    val pendingRequests = approvalRequests.filter { it.status == "Pending" }
    val resetRequests = employees.filter { it.statusRequestReset }

    if (pendingRequests.isEmpty() && resetRequests.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Tidak ada tiket pengajuan atau reset HP yang pending saat ini.", color = Color(0xFF94A3B8), fontSize = 11.sp)
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Pending Leave & Overtime requests
        pendingRequests.forEach { req ->
            var noteInput by remember { mutableStateOf("") }
            var showNoteField by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(req.type, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("Oleh: ${req.employeeNama} (${req.employeeNik})", fontSize = 10.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Medium)
                            Text("Uraian: ${req.reason}", fontSize = 10.sp, color = Color(0xFF64748B))
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { onProcessApproval(req.id, "Approved", noteInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Setujui", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onProcessApproval(req.id, "Rejected", noteInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Tolak", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Optional note field
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        placeholder = { Text("Catatan HRD (Opsional)...", fontSize = 10.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(6.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.sp)
                    )
                }
            }
        }

        // Pending Device Reset requests
        resetRequests.forEach { emp ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF3C7).copy(alpha = 0.4f))
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Permintaan Reset Device HP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                        Text("${emp.nama} (${emp.nik})", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("Perangkat lama: ${emp.device}", fontSize = 9.sp, color = Color.Gray)
                    }

                    Button(
                        onClick = { onApproveResetDevice(emp.nik, emp.nama) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Setujui Reset", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HrdHapusAbsenModule(
    attendanceLogs: List<AttendanceLog>,
    onDeleteAttendance: (Long) -> Unit
) {
    if (attendanceLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Tidak ada data absen untuk dihapus.", color = Color(0xFF94A3B8), fontSize = 11.sp)
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        attendanceLogs.forEach { log ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${log.employeeNama} (${log.type})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        Text(log.timeString, fontSize = 10.sp, color = Color(0xFF64748B))
                    }

                    Button(
                        onClick = { onDeleteAttendance(log.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Hapus", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun HrdPayrollModule(
    employees: List<Employee>,
    attendanceLogs: List<AttendanceLog>,
    approvalRequests: List<ApprovalRequest>
) {
    val totalEmployees = employees.size
    val totalApprovedLeaves = approvalRequests.count { it.status == "Approved" && !it.type.contains("Lembur") }
    val totalApprovedOvertimes = approvalRequests.count { it.status == "Approved" && it.type.contains("Lembur") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Summary Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Total SDM", fontSize = 10.sp, color = Color(0xFF1E40AF))
                    Text("$totalEmployees Orang", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E3A8A))
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Kehadiran Valid", fontSize = 10.sp, color = Color(0xFF065F46))
                    Text("${attendanceLogs.size} Log", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF047857))
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Lembur Disetujui", fontSize = 10.sp, color = Color(0xFF92400E))
                    Text("$totalApprovedOvertimes Tiket", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                }
            }
        }

        // Details breakdown
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Estimasi Beban Penggajian (Agustus 2026):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Total Gaji Pokok & Tunjangan", fontSize = 10.sp, color = Color(0xFF475569))
                    Text("Rp ${String.format("%,d", totalEmployees * 8300000).replace(',', '.')}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Upah Lembur Tervalidasi", fontSize = 10.sp, color = Color(0xFF475569))
                    Text("Rp ${String.format("%,d", totalApprovedOvertimes * 140000).replace(',', '.')}", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("• Cuti Tahunan Digunakan", fontSize = 10.sp, color = Color(0xFF475569))
                    Text("$totalApprovedLeaves Hari", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
