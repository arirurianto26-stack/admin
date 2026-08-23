package com.example.ui.it

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.util.AttendanceExportHelper
import com.example.util.ExportResult
import com.example.viewmodel.LoginStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItControlScreen(
    isMaintenanceMode: Boolean,
    currentAnnouncement: String,
    currentOfficeName: String,
    currentOfficeRadius: String,
    totalEmployees: Int,
    totalAttendance: Int,
    totalApprovals: Int,
    attendanceLogs: List<AttendanceLog> = emptyList(),
    approvalRequests: List<ApprovalRequest> = emptyList(),
    currentEmployee: Employee?,
    allEmployees: List<Employee>,
    selectedDevice: String,
    loginStatus: LoginStatus,
    onToggleMaintenance: (Boolean) -> Unit,
    onUpdateAnnouncement: (String) -> Unit,
    onUpdateOfficeSettings: (String, String) -> Unit,
    onResetDemoData: () -> Unit,
    onSelectDevice: (String) -> Unit,
    onAttemptLogin: () -> Unit,
    onSwitchEmployee: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var announcementInput by remember(currentAnnouncement) { mutableStateOf(currentAnnouncement) }
    var officeNameInput by remember(currentOfficeName) { mutableStateOf(currentOfficeName) }
    var officeRadiusInput by remember(currentOfficeRadius) { mutableStateOf(currentOfficeRadius) }

    // IT Export states
    var itExportFormat by remember { mutableStateOf("CSV") } // "CSV" or "JSON"
    var itEmployeeFilter by remember { mutableStateOf("SEMUA") }
    var itExportResult by remember { mutableStateOf<ExportResult?>(null) }
    var showItPreview by remember { mutableStateOf(false) }
    var itCopyMessage by remember { mutableStateOf<String?>(null) }
    var expandedExportEmpMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF1E293B))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
            .padding(20.dp)
            .testTag("it_control_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("💻", fontSize = 20.sp)
            Text(
                text = "Panel Kontrol & Konfigurasi IT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF8FAFC)
            )
        }

        // 1. Maintenance Toggle Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "System Maintenance Mode",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                    Text(
                        text = "Aktifkan untuk menampilkan peringatan pemeliharaan pada aplikasi Karyawan & HRD.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusColor by animateColorAsState(
                        targetValue = if (isMaintenanceMode) Color(0xFFEF4444) else Color(0xFF94A3B8),
                        label = "switchColor"
                    )

                    Text(
                        text = if (isMaintenanceMode) "ON" else "OFF",
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Switch(
                        checked = isMaintenanceMode,
                        onCheckedChange = onToggleMaintenance,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFEF4444),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("toggle_maintenance")
                    )
                }
            }
        }

        // 2. Broadcast Announcement Editor Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📢 Broadcast Pengumuman Perusahaan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = "Pesan ini akan tampil di bagian atas dashboard seluruh Karyawan.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                OutlinedTextField(
                    value = announcementInput,
                    onValueChange = { announcementInput = it },
                    placeholder = { Text("Tulis pengumuman...", color = Color.Gray, fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                Button(
                    onClick = { onUpdateAnnouncement(announcementInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Perbarui Pengumuman", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 3. Office & Geofence GPS Settings Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📍 Konfigurasi Geofencing Kantor",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )

                Text("Nama Titik Kantor:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                OutlinedTextField(
                    value = officeNameInput,
                    onValueChange = { officeNameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                Text("Maksimal Radius Presensi (Meter):", fontSize = 10.sp, color = Color(0xFF94A3B8))
                OutlinedTextField(
                    value = officeRadiusInput,
                    onValueChange = { officeRadiusInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    )
                )

                Button(
                    onClick = { onUpdateOfficeSettings(officeNameInput, officeRadiusInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Simpan Pengaturan Geofence", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 4. IT Device Binding & Hardware Verification Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🔒 Pengujian & Simulasi Device Binding",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = "Fitur ini hanya dapat diakses oleh IT Administrator untuk mensimulasikan otentikasi hardware ID karyawan.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                // Pilih Karyawan Uji
                var expandedEmpMenu by remember { mutableStateOf(false) }
                Text("Pilih Karyawan Target Pengujian:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                ExposedDropdownMenuBox(
                    expanded = expandedEmpMenu,
                    onExpandedChange = { expandedEmpMenu = !expandedEmpMenu },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = "${currentEmployee?.nama ?: ""} (${currentEmployee?.nik ?: ""}) - Device: ${currentEmployee?.device ?: ""}",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEmpMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEmpMenu,
                        onDismissRequest = { expandedEmpMenu = false }
                    ) {
                        allEmployees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text("${emp.nama} (${emp.nik}) - [${emp.device}]", fontSize = 11.sp) },
                                onClick = {
                                    onSwitchEmployee(emp.nik)
                                    expandedEmpMenu = false
                                }
                            )
                        }
                    }
                }

                // Pilih Simulasi Perangkat
                var expandedDevMenu by remember { mutableStateOf(false) }
                val currentDev = currentEmployee?.device ?: "HP-Ari-Primary"
                val deviceOptions = listOf(
                    currentDev to "HP Resmi Terdaftar ($currentDev)",
                    "HP-Lain-Tidak-Terdaftar" to "HP Lain / Perangkat Baru (Tidak Terdaftar)"
                )

                Text("Simulasi Perangkat yang Digunakan:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedDevMenu,
                        onExpandedChange = { expandedDevMenu = !expandedDevMenu },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = if (selectedDevice == currentDev) "HP Terdaftar ($currentDev)" else "HP Lain / Perangkat Baru",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDevMenu) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDevMenu,
                            onDismissRequest = { expandedDevMenu = false }
                        ) {
                            deviceOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label, fontSize = 11.sp) },
                                    onClick = {
                                        onSelectDevice(key)
                                        expandedDevMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onAttemptLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_it_verify_device")
                    ) {
                        Text("Uji Verifikasi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Status Hasil Verifikasi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (loginStatus.isSuccess) Color(0xFF064E3B) else Color(0xFF7F1D1D))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Hasil Uji: ${loginStatus.message}",
                        color = if (loginStatus.isSuccess) Color(0xFF34D399) else Color(0xFFFCA5A5),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // 5. IT Attendance Audit Log & Download Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📥 Download Riwayat Presensi & Log Audit IT",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Text(
                    text = "Ekspor data riwayat absensi teknis, koordinat satelit GPS, ID log, stempel waktu milidetik, serta verifikasi device binding.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )

                // Format Selector (CSV vs JSON)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CSV", "JSON").forEach { fmt ->
                        val isSelected = itExportFormat == fmt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B))
                                .border(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                .clickable { itExportFormat = fmt }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (fmt == "CSV") "📊 Format CSV (Standar)" else "{ } Format JSON (Raw API)",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Filter Karyawan Dropdown
                Text("Filter Data Target:", fontSize = 10.sp, color = Color(0xFF94A3B8))
                ExposedDropdownMenuBox(
                    expanded = expandedExportEmpMenu,
                    onExpandedChange = { expandedExportEmpMenu = !expandedExportEmpMenu },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filterLabel = if (itEmployeeFilter == "SEMUA") "Semua Karyawan (${attendanceLogs.size} Log Total)" 
                        else allEmployees.firstOrNull { it.nik == itEmployeeFilter }?.let { "${it.nama} (${it.nik})" } ?: itEmployeeFilter

                    OutlinedTextField(
                        value = filterLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedExportEmpMenu) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155)
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expandedExportEmpMenu,
                        onDismissRequest = { expandedExportEmpMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Karyawan (${attendanceLogs.size} Log)", fontSize = 11.sp) },
                            onClick = {
                                itEmployeeFilter = "SEMUA"
                                expandedExportEmpMenu = false
                            }
                        )
                        allEmployees.forEach { emp ->
                            val empCount = attendanceLogs.count { it.employeeNik == emp.nik }
                            DropdownMenuItem(
                                text = { Text("${emp.nama} (${emp.nik}) - $empCount Log", fontSize = 11.sp) },
                                onClick = {
                                    itEmployeeFilter = emp.nik
                                    expandedExportEmpMenu = false
                                }
                            )
                        }
                    }
                }

                // Action Buttons
                val filteredLogs = if (itEmployeeFilter == "SEMUA") attendanceLogs else attendanceLogs.filter { it.employeeNik == itEmployeeFilter }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Download Button
                    Button(
                        onClick = {
                            val content = if (itExportFormat == "CSV") {
                                AttendanceExportHelper.generateItCsv(filteredLogs)
                            } else {
                                AttendanceExportHelper.generateItJson(filteredLogs)
                            }
                            val res = AttendanceExportHelper.saveExportFile(
                                context = context,
                                prefix = "audit_absensi_it",
                                extension = if (itExportFormat == "CSV") "csv" else "json",
                                content = content,
                                recordCount = filteredLogs.size
                            )
                            itExportResult = res
                            itCopyMessage = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_it_download_attendance")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Unduh $itExportFormat", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share Button
                    Button(
                        onClick = {
                            val content = if (itExportFormat == "CSV") {
                                AttendanceExportHelper.generateItCsv(filteredLogs)
                            } else {
                                AttendanceExportHelper.generateItJson(filteredLogs)
                            }
                            AttendanceExportHelper.shareExportContent(
                                context = context,
                                title = "Audit Log Absensi IT ($itExportFormat)",
                                content = content
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
                            val content = if (itExportFormat == "CSV") {
                                AttendanceExportHelper.generateItCsv(filteredLogs)
                            } else {
                                AttendanceExportHelper.generateItJson(filteredLogs)
                            }
                            val copied = AttendanceExportHelper.copyToClipboard(context, content, "Audit Log Absensi IT")
                            if (copied) {
                                itCopyMessage = "📋 Data Log IT berhasil disalin ke clipboard!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF38BDF8))
                    }
                }

                // Copy Notice
                if (itCopyMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0C4A6E))
                            .padding(8.dp)
                    ) {
                        Text(itCopyMessage ?: "", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Download Success Details
                itExportResult?.let { res ->
                    if (res.isSuccess) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF064E3B))
                                .border(1.dp, Color(0xFF059669), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Log Audit IT Berhasil Diunduh!",
                                        color = Color(0xFF34D399),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "📄 File: ${res.fileName}\n📊 Records: ${res.recordCount} baris log presensi\n💾 Size: ${res.formattedSize}\n📁 Path: ${res.fileAbsolutePath}",
                                    color = Color(0xFFA7F3D0),
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { showItPreview = !showItPreview },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(if (showItPreview) "Tutup Log Preview" else "Lihat Data Log", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Code / Log Preview
                        if (showItPreview) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF020617))
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = res.content.lines().take(12).joinToString("\n") + if (res.content.lines().size > 12) "\n... (${res.content.lines().size - 12} baris log lainnya)" else "",
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
                                .background(Color(0xFF7F1D1D))
                                .padding(8.dp)
                        ) {
                            Text(res.message, color = Color(0xFFFCA5A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 6. IT & System Attendance Analytics Dashboard
        AttendanceAnalyticsDashboard(
            employees = allEmployees,
            attendanceLogs = attendanceLogs,
            approvalRequests = approvalRequests,
            isDarkTheme = true
        )

        // 7. Database Stats & Reset Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "📊 Status Database & Layanan",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Karyawan Terdaftar:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("$totalEmployees Orang", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Log Presensi Tercatat:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("$totalAttendance Baris", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tiket Approval / Pengajuan:", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text("$totalApprovals Tiket", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFF334155))
                )

                Button(
                    onClick = onResetDemoData,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🔄 Reset Database ke Data Awal Demo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 5. Documentation Guide Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Panduan Fitur & Integrasi Smart HC:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF1F5F9)
                )
                Text(
                    text = "1. Gunakan 'Ganti Akun' di Portal Karyawan untuk mencoba login sebagai karyawan lain atau karyawan baru yang didaftarkan.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
                Text(
                    text = "2. Device Binding memastikan setiap karyawan hanya dapat absen dari HP terdaftar. Jika menggunakan HP lain, sistem akan meminta Reset Device ke HRD.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
                Text(
                    text = "3. Menu HRD kini dilengkapi Rekapitulasi Kehadiran, Form Approval dengan Catatan Reviewer, dan Ringkasan Payroll.",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 15.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}
