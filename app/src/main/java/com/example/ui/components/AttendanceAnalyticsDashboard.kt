package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApprovalRequest
import com.example.data.model.AttendanceLog
import com.example.data.model.Employee
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class DailyAttendanceMetric(
    val dayKey: String,          // e.g. "2026-08-22"
    val dateLabel: String,       // e.g. "22 Agu"
    val dayName: String,         // e.g. "Sab"
    val timestamp: Long,
    val totalCheckIn: Int,
    val onTimeCheckIn: Int,
    val lateCheckIn: Int,
    val totalCheckOut: Int,
    val attendanceRate: Float    // 0f to 1f
)

data class EmployeeTodayStatus(
    val employee: Employee,
    val hasCheckedIn: Boolean,
    val checkInTime: String?,
    val isLate: Boolean,
    val hasCheckedOut: Boolean,
    val checkOutTime: String?,
    val isOnLeave: Boolean,
    val leaveType: String?
)

@Composable
fun AttendanceAnalyticsDashboard(
    employees: List<Employee>,
    attendanceLogs: List<AttendanceLog>,
    approvalRequests: List<ApprovalRequest>,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Styling colors based on theme
    val cardBg = if (isDarkTheme) Color(0xFF0F172A) else Color.White
    val cardBorder = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textPrimary = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val accentPrimary = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)

    // Timeframe selector state
    var selectedDaysRange by remember { mutableIntStateOf(7) } // 7, 14, or 30 days
    var selectedChartType by remember { mutableStateOf("BAR") } // "BAR" or "LINE"
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var selectedDepartmentFilter by remember { mutableStateOf("SEMUA") }

    // Aggregate Data Calculation
    val calendar = Calendar.getInstance()
    val todayStartMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val departments = remember(employees) {
        listOf("SEMUA") + employees.map { it.departemen.ifBlank { "General" } }.distinct()
    }

    val filteredEmployees = remember(employees, selectedDepartmentFilter) {
        if (selectedDepartmentFilter == "SEMUA") employees
        else employees.filter { it.departemen.equals(selectedDepartmentFilter, ignoreCase = true) }
    }

    val filteredLogs = remember(attendanceLogs, filteredEmployees) {
        val nset = filteredEmployees.map { it.nik }.toSet()
        attendanceLogs.filter { it.employeeNik in nset }
    }

    // Daily Trend Metrics over selected days range
    val dailyMetrics = remember(filteredLogs, filteredEmployees, selectedDaysRange) {
        val result = mutableListOf<DailyAttendanceMetric>()
        val cal = Calendar.getInstance()
        val sdfDateLabel = SimpleDateFormat("dd MMM", Locale("id", "ID"))
        val sdfDayName = SimpleDateFormat("EEE", Locale("id", "ID"))
        val sdfDayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        for (i in (selectedDaysRange - 1) downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis() - (i * 86400000L)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startDay = targetCal.timeInMillis
            val endDay = startDay + 86400000L

            val dayLogs = filteredLogs.filter { it.timestamp in startDay until endDay }
            val checkIns = dayLogs.filter { it.type.contains("In", ignoreCase = true) }
            val checkOuts = dayLogs.filter { it.type.contains("Out", ignoreCase = true) }

            var onTimeCount = 0
            var lateCount = 0

            for (log in checkIns) {
                val logCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                val hour = logCal.get(Calendar.HOUR_OF_DAY)
                val minute = logCal.get(Calendar.MINUTE)
                if (hour < 8 || (hour == 8 && minute <= 30)) {
                    onTimeCount++
                } else {
                    lateCount++
                }
            }

            val totalHeadcount = max(filteredEmployees.size, 1)
            val rate = (checkIns.size.toFloat() / totalHeadcount).coerceIn(0f, 1f)

            result.add(
                DailyAttendanceMetric(
                    dayKey = sdfDayKey.format(targetCal.time),
                    dateLabel = sdfDateLabel.format(targetCal.time),
                    dayName = sdfDayName.format(targetCal.time),
                    timestamp = startDay,
                    totalCheckIn = checkIns.size,
                    onTimeCheckIn = onTimeCount,
                    lateCheckIn = lateCount,
                    totalCheckOut = checkOuts.size,
                    attendanceRate = rate
                )
            )
        }
        result
    }

    // Today status breakdown for all employees
    val todayStatuses = remember(filteredEmployees, attendanceLogs, approvalRequests) {
        val now = System.currentTimeMillis()
        val calNow = Calendar.getInstance().apply { timeInMillis = now }

        filteredEmployees.map { emp ->
            val empLogs = attendanceLogs.filter { it.employeeNik == emp.nik }
            val todayIn = empLogs.firstOrNull { log ->
                val lCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                log.type.contains("In", ignoreCase = true) &&
                        lCal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                        lCal.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
            }
            val todayOut = empLogs.firstOrNull { log ->
                val lCal = Calendar.getInstance().apply { timeInMillis = log.timestamp }
                log.type.contains("Out", ignoreCase = true) &&
                        lCal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) &&
                        lCal.get(Calendar.DAY_OF_YEAR) == calNow.get(Calendar.DAY_OF_YEAR)
            }

            val todayLeave = approvalRequests.firstOrNull { req ->
                req.employeeNik == emp.nik && req.status == "Approved"
            }

            val isLate = if (todayIn != null) {
                val tCal = Calendar.getInstance().apply { timeInMillis = todayIn.timestamp }
                val h = tCal.get(Calendar.HOUR_OF_DAY)
                val m = tCal.get(Calendar.MINUTE)
                h > 8 || (h == 8 && m > 30)
            } else false

            EmployeeTodayStatus(
                employee = emp,
                hasCheckedIn = todayIn != null,
                checkInTime = todayIn?.timeString?.substringAfter("- ")?.trim(),
                isLate = isLate,
                hasCheckedOut = todayOut != null,
                checkOutTime = todayOut?.timeString?.substringAfter("- ")?.trim(),
                isOnLeave = todayLeave != null,
                leaveType = todayLeave?.type
            )
        }
    }

    val totalHeadcount = filteredEmployees.size
    val todayPresentCount = todayStatuses.count { it.hasCheckedIn }
    val todayOnTimeCount = todayStatuses.count { it.hasCheckedIn && !it.isLate }
    val todayLateCount = todayStatuses.count { it.hasCheckedIn && it.isLate }
    val todayLeaveCount = todayStatuses.count { it.isOnLeave }
    val todayPendingCount = totalHeadcount - todayPresentCount - todayLeaveCount

    val presentPercentage = if (totalHeadcount > 0) ((todayPresentCount.toFloat() / totalHeadcount) * 100).toInt() else 0
    val onTimePercentage = if (todayPresentCount > 0) ((todayOnTimeCount.toFloat() / todayPresentCount) * 100).toInt() else 100

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_analytics_dashboard"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header & Filter Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = accentPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Attendance Analytics & Trends",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Text(
                                text = "Visualisasi Presensi Harian & Status Karyawan",
                                fontSize = 10.sp,
                                color = textSecondary
                            )
                        }
                    }

                    // Department Filter Pills / Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Headcount: $totalHeadcount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentPrimary
                        )
                    }
                }

                // Range Selector (7 Hari, 14 Hari, 30 Hari) & Chart Style Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Range Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(7 to "7 Hari", 14 to "14 Hari", 30 to "30 Hari").forEach { (days, label) ->
                            val isSelected = selectedDaysRange == days
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) accentPrimary
                                        else if (isDarkTheme) Color(0xFF1E293B)
                                        else Color(0xFFF1F5F9)
                                    )
                                    .clickable {
                                        selectedDaysRange = days
                                        selectedDayIndex = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else textSecondary
                                )
                            }
                        }
                    }

                    // Chart Mode Toggle (Bar vs Area)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (selectedChartType == "BAR") accentPrimary
                                    else if (isDarkTheme) Color(0xFF1E293B)
                                    else Color(0xFFF1F5F9)
                                )
                                .clickable { selectedChartType = "BAR" }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "Bar Chart",
                                    tint = if (selectedChartType == "BAR") Color.White else textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Batang",
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedChartType == "BAR") FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedChartType == "BAR") Color.White else textSecondary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (selectedChartType == "LINE") accentPrimary
                                    else if (isDarkTheme) Color(0xFF1E293B)
                                    else Color(0xFFF1F5F9)
                                )
                                .clickable { selectedChartType = "LINE" }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShowChart,
                                    contentDescription = "Line Chart",
                                    tint = if (selectedChartType == "LINE") Color.White else textSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Kurva",
                                    fontSize = 10.sp,
                                    fontWeight = if (selectedChartType == "LINE") FontWeight.Bold else FontWeight.Medium,
                                    color = if (selectedChartType == "LINE") Color.White else textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4 KPI Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Attendance Rate Today
            KpiMetricCard(
                title = "Kehadiran Hari Ini",
                value = "$presentPercentage%",
                subtitle = "$todayPresentCount dari $totalHeadcount karyawan",
                icon = "👥",
                accentColor = Color(0xFF10B981),
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )

            // 2. On Time Rate
            KpiMetricCard(
                title = "Ketepatan Waktu",
                value = "$onTimePercentage%",
                subtitle = "$todayOnTimeCount tepat • $todayLateCount telat",
                icon = "⏱️",
                accentColor = if (onTimePercentage >= 80) Color(0xFF0284C7) else Color(0xFFF59E0B),
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 3. Cuti / Izin
            KpiMetricCard(
                title = "Cuti & Sakit",
                value = "$todayLeaveCount",
                subtitle = "Pengajuan disetujui",
                icon = "🏖️",
                accentColor = Color(0xFF8B5CF6),
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )

            // 4. Belum Absen
            KpiMetricCard(
                title = "Belum Absen",
                value = "${max(todayPendingCount, 0)}",
                subtitle = "Menunggu Check In",
                icon = "⏳",
                accentColor = Color(0xFFF43F5E),
                isDarkTheme = isDarkTheme,
                modifier = Modifier.weight(1f)
            )
        }

        // Daily Attendance Trends Interactive Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tren Presensi Harian ($selectedDaysRange Hari)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        Text(
                            text = "Ketuk batang / grafik untuk melihat rincian detail",
                            fontSize = 9.sp,
                            color = textSecondary
                        )
                    }

                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(label = "Tepat", color = Color(0xFF10B981), isDarkTheme = isDarkTheme)
                        LegendItem(label = "Telat", color = Color(0xFFF59E0B), isDarkTheme = isDarkTheme)
                        LegendItem(label = "Out", color = Color(0xFF38BDF8), isDarkTheme = isDarkTheme)
                    }
                }

                // Interactive Chart Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDarkTheme) Color(0xFF090E17) else Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    if (dailyMetrics.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada data presensi untuk periode ini", fontSize = 11.sp, color = textSecondary)
                        }
                    } else {
                        DailyAttendanceCanvasChart(
                            metrics = dailyMetrics,
                            chartType = selectedChartType,
                            selectedIndex = selectedDayIndex,
                            maxExpected = max(totalHeadcount, 4),
                            isDarkTheme = isDarkTheme,
                            onSelectIndex = { index ->
                                selectedDayIndex = if (selectedDayIndex == index) null else index
                            }
                        )
                    }
                }

                // Selected Day Detailed Tooltip Banner
                AnimatedVisibility(
                    visible = selectedDayIndex != null && selectedDayIndex in dailyMetrics.indices,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    val metric = dailyMetrics.getOrNull(selectedDayIndex ?: 0)
                    if (metric != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE0F2FE))
                                .border(1.dp, if (isDarkTheme) Color(0xFF38BDF8).copy(alpha = 0.5f) else Color(0xFFBAE6FD), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "📅 ${metric.dayName}, ${metric.dateLabel}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0369A1)
                                    )
                                    Text(
                                        text = "Total Check In: ${metric.totalCheckIn} (${metric.onTimeCheckIn} Tepat Waktu, ${metric.lateCheckIn} Terlambat)",
                                        fontSize = 10.sp,
                                        color = textPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Total Check Out: ${metric.totalCheckOut} • Tingkat Kehadiran: ${(metric.attendanceRate * 100).toInt()}%",
                                        fontSize = 9.sp,
                                        color = textSecondary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (metric.totalCheckIn > 0) Color(0xFF10B981) else Color(0xFF64748B))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${(metric.attendanceRate * 100).toInt()}% Hadir",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Overall Employee Status Breakdown (Donut Chart & Distribution)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Status Kehadiran Karyawan Hari Ini",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AttendanceDonutChart(
                            onTime = todayOnTimeCount,
                            late = todayLateCount,
                            leave = todayLeaveCount,
                            pending = max(todayPendingCount, 0),
                            isDarkTheme = isDarkTheme
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$presentPercentage%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = textPrimary
                            )
                            Text(
                                text = "Hadir",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondary
                            )
                        }
                    }

                    // Status Breakdown List
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusProgressRow(
                            label = "Tepat Waktu",
                            count = todayOnTimeCount,
                            total = totalHeadcount,
                            color = Color(0xFF10B981),
                            isDarkTheme = isDarkTheme
                        )
                        StatusProgressRow(
                            label = "Terlambat",
                            count = todayLateCount,
                            total = totalHeadcount,
                            color = Color(0xFFF59E0B),
                            isDarkTheme = isDarkTheme
                        )
                        StatusProgressRow(
                            label = "Cuti & Sakit",
                            count = todayLeaveCount,
                            total = totalHeadcount,
                            color = Color(0xFF8B5CF6),
                            isDarkTheme = isDarkTheme
                        )
                        StatusProgressRow(
                            label = "Belum Absen",
                            count = max(todayPendingCount, 0),
                            total = totalHeadcount,
                            color = Color(0xFF94A3B8),
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }
        }

        // Live Employee Attendance Roster Table / Cards
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rincian Kehadiran Per Karyawan",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "${todayStatuses.size} Karyawan",
                        fontSize = 10.sp,
                        color = textSecondary
                    )
                }

                todayStatuses.forEach { status ->
                    EmployeeStatusItemRow(status = status, isDarkTheme = isDarkTheme)
                }
            }
        }

        // Peak Check-In Rush Hours Distribution (Audit Telemetry for IT & HRD)
        Card(
            colors = CardDefaults.cardColors(containerColor = cardBg),
            border = BorderStroke(1.dp, cardBorder),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = accentPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Distribusi Jam Sibuk Presensi (Peak Hours)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                    }
                }

                PeakHourHistogram(
                    attendanceLogs = filteredLogs,
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: String,
    accentColor: Color,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) Color(0xFF0F172A) else Color.White
        ),
        border = BorderStroke(1.dp, if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
                Text(text = icon, fontSize = 12.sp)
            }
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun LegendItem(
    label: String,
    color: Color,
    isDarkTheme: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
        )
    }
}

@Composable
fun StatusProgressRow(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    isDarkTheme: Boolean
) {
    val progress = if (total > 0) (count.toFloat() / total).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF334155)
                )
            }
            Text(
                text = "$count (${(progress * 100).toInt()}%)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF1F5F9)
        )
    }
}

@Composable
fun EmployeeStatusItemRow(
    status: EmployeeTodayStatus,
    isDarkTheme: Boolean
) {
    val emp = status.employee
    val bg = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val borderCol = if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Avatar with initials
                val initials = emp.nama.split(" ").take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (status.hasCheckedIn) Color(0xFF10B981) else Color(0xFF64748B)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = emp.nama,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) Color(0xFFF8FAFC) else Color(0xFF0F172A)
                    )
                    Text(
                        text = "${emp.nik} • ${emp.departemen.ifBlank { "Operations" }}",
                        fontSize = 9.sp,
                        color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }

            // Status Badge
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when {
                    status.isOnLeave -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEDE9FE))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "🏖️ ${status.leaveType ?: "Cuti"}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6D28D9)
                            )
                        }
                    }
                    status.hasCheckedIn -> {
                        val badgeBg = if (status.isLate) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                        val badgeText = if (status.isLate) Color(0xFFB45309) else Color(0xFF15803D)
                        val statusLabel = if (status.isLate) "⚠️ Terlambat" else "✅ Tepat Waktu"
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeText
                            )
                        }
                        Text(
                            text = "In: ${status.checkInTime ?: "-"}${if (status.hasCheckedOut) " • Out: ${status.checkOutTime}" else ""}",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                        )
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFF1F5F9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⏳ Belum Absen",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyAttendanceCanvasChart(
    metrics: List<DailyAttendanceMetric>,
    chartType: String,
    selectedIndex: Int?,
    maxExpected: Int,
    isDarkTheme: Boolean,
    onSelectIndex: (Int) -> Unit
) {
    val gridColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val textLabelColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
    val highlightColor = if (isDarkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)

    val maxVal = max(metrics.maxOfOrNull { max(it.totalCheckIn, it.totalCheckOut) } ?: 1, maxExpected).toFloat()

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(metrics) {
                detectTapGestures { offset ->
                    val count = metrics.size
                    if (count > 0) {
                        val colWidth = size.width / count
                        val tappedIdx = (offset.x / colWidth).toInt().coerceIn(0, count - 1)
                        onSelectIndex(tappedIdx)
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 26f
        val paddingTop = 16f
        val chartHeight = height - paddingBottom - paddingTop

        // 1. Draw horizontal grid lines (0%, 50%, 100%)
        val gridSteps = 3
        for (i in 0..gridSteps) {
            val y = paddingTop + (chartHeight * (i.toFloat() / gridSteps))
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
        }

        val itemCount = metrics.size
        val colWidth = width / itemCount

        if (chartType == "BAR") {
            // Draw Bar Chart with stacked/grouped elements
            val barWidth = (colWidth * 0.45f).coerceIn(10f, 28f)

            metrics.forEachIndexed { index, item ->
                val centerX = (index * colWidth) + (colWidth / 2f)
                val isSelected = selectedIndex == index

                // Calculate heights
                val onTimeHeight = (item.onTimeCheckIn / maxVal) * chartHeight
                val lateHeight = (item.lateCheckIn / maxVal) * chartHeight
                val checkOutHeight = (item.totalCheckOut / maxVal) * chartHeight

                val baseY = height - paddingBottom

                // Highlight column background if selected
                if (isSelected) {
                    drawRoundRect(
                        color = highlightColor.copy(alpha = 0.15f),
                        topLeft = Offset(index * colWidth, paddingTop),
                        size = Size(colWidth, chartHeight),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }

                // Check In Stack (OnTime green bottom, Late amber top)
                if (onTimeHeight > 0) {
                    drawRoundRect(
                        color = Color(0xFF10B981),
                        topLeft = Offset(centerX - barWidth - 1f, baseY - onTimeHeight),
                        size = Size(barWidth, onTimeHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                if (lateHeight > 0) {
                    drawRoundRect(
                        color = Color(0xFFF59E0B),
                        topLeft = Offset(centerX - barWidth - 1f, baseY - onTimeHeight - lateHeight),
                        size = Size(barWidth, lateHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                // Check Out Bar (Sky blue next to check in bar)
                if (checkOutHeight > 0) {
                    drawRoundRect(
                        color = Color(0xFF38BDF8),
                        topLeft = Offset(centerX + 1f, baseY - checkOutHeight),
                        size = Size(barWidth, checkOutHeight),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                // Indicator dot for zero attendance days
                if (item.totalCheckIn == 0 && item.totalCheckOut == 0) {
                    drawCircle(
                        color = gridColor,
                        radius = 3f,
                        center = Offset(centerX, baseY - 4f)
                    )
                }
            }
        } else {
            // Draw Smooth Area / Curve Line Chart
            val pathCheckIn = Path()
            val pathFill = Path()

            metrics.forEachIndexed { index, item ->
                val x = (index * colWidth) + (colWidth / 2f)
                val ratio = (item.totalCheckIn.toFloat() / maxVal).coerceIn(0f, 1f)
                val y = (height - paddingBottom) - (ratio * chartHeight)

                if (index == 0) {
                    pathCheckIn.moveTo(x, y)
                    pathFill.moveTo(x, height - paddingBottom)
                    pathFill.lineTo(x, y)
                } else {
                    val prevX = ((index - 1) * colWidth) + (colWidth / 2f)
                    val prevRatio = (metrics[index - 1].totalCheckIn.toFloat() / maxVal).coerceIn(0f, 1f)
                    val prevY = (height - paddingBottom) - (prevRatio * chartHeight)

                    val cx1 = prevX + (x - prevX) / 2f
                    val cy1 = prevY
                    val cx2 = prevX + (x - prevX) / 2f
                    val cy2 = y
                    pathCheckIn.cubicTo(cx1, cy1, cx2, cy2, x, y)
                    pathFill.cubicTo(cx1, cy1, cx2, cy2, x, y)
                }

                if (index == metrics.size - 1) {
                    pathFill.lineTo(x, height - paddingBottom)
                    pathFill.close()
                }
            }

            // Fill gradient area
            drawPath(
                path = pathFill,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.35f),
                        Color(0xFF10B981).copy(alpha = 0.02f)
                    ),
                    startY = paddingTop,
                    endY = height - paddingBottom
                )
            )

            // Draw line stroke
            drawPath(
                path = pathCheckIn,
                color = Color(0xFF10B981),
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // Draw Data Points
            metrics.forEachIndexed { index, item ->
                val x = (index * colWidth) + (colWidth / 2f)
                val ratio = (item.totalCheckIn.toFloat() / maxVal).coerceIn(0f, 1f)
                val y = (height - paddingBottom) - (ratio * chartHeight)
                val isSelected = selectedIndex == index

                drawCircle(
                    color = if (isSelected) Color(0xFF0369A1) else Color.White,
                    radius = if (isSelected) 6f else 4f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = if (item.lateCheckIn > 0) Color(0xFFF59E0B) else Color(0xFF10B981),
                    radius = if (isSelected) 4f else 2.5f,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun AttendanceDonutChart(
    onTime: Int,
    late: Int,
    leave: Int,
    pending: Int,
    isDarkTheme: Boolean
) {
    val total = (onTime + late + leave + pending).coerceAtLeast(1).toFloat()
    val onTimeSweep = (onTime / total) * 360f
    val lateSweep = (late / total) * 360f
    val leaveSweep = (leave / total) * 360f
    val pendingSweep = (pending / total) * 360f

    val strokeWidth = 18f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val sizeVal = size.minDimension
        val radius = (sizeVal - strokeWidth) / 2f
        val centerOffset = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        // On Time
        if (onTimeSweep > 0) {
            drawArc(
                color = Color(0xFF10B981),
                startAngle = startAngle,
                sweepAngle = onTimeSweep,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += onTimeSweep
        }

        // Late
        if (lateSweep > 0) {
            drawArc(
                color = Color(0xFFF59E0B),
                startAngle = startAngle,
                sweepAngle = lateSweep,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += lateSweep
        }

        // Leave
        if (leaveSweep > 0) {
            drawArc(
                color = Color(0xFF8B5CF6),
                startAngle = startAngle,
                sweepAngle = leaveSweep,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            startAngle += leaveSweep
        }

        // Pending
        if (pendingSweep > 0) {
            drawArc(
                color = if (isDarkTheme) Color(0xFF334155) else Color(0xFFCBD5E1),
                startAngle = startAngle,
                sweepAngle = pendingSweep,
                useCenter = false,
                topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                size = Size(radius * 2f, radius * 2f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun PeakHourHistogram(
    attendanceLogs: List<AttendanceLog>,
    isDarkTheme: Boolean
) {
    val hours = listOf("06", "07", "08", "09", "10", "16", "17", "18")
    val cal = Calendar.getInstance()

    val hourCounts = remember(attendanceLogs) {
        val map = hours.associateWith { 0 }.toMutableMap()
        for (log in attendanceLogs) {
            cal.timeInMillis = log.timestamp
            val h = String.format("%02d", cal.get(Calendar.HOUR_OF_DAY))
            if (h in map) {
                map[h] = (map[h] ?: 0) + 1
            }
        }
        map
    }

    val maxCount = max(hourCounts.values.maxOrNull() ?: 1, 1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        hours.forEach { hour ->
            val count = hourCounts[hour] ?: 0
            val ratio = (count.toFloat() / maxCount).coerceIn(0.1f, 1f)
            val isMorningPeak = hour in listOf("07", "08")

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                if (count > 0) {
                    Text(
                        text = "$count",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMorningPeak) Color(0xFF10B981) else Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(ratio * 0.7f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (isMorningPeak) Color(0xFF10B981)
                            else if (hour in listOf("17", "18")) Color(0xFF38BDF8)
                            else if (isDarkTheme) Color(0xFF334155)
                            else Color(0xFFCBD5E1)
                        )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${hour}:00",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)
                )
            }
        }
    }
}
