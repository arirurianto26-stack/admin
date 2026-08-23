package com.example.data.repository

import com.example.data.dao.ApprovalDao
import com.example.data.dao.AttendanceDao
import com.example.data.dao.EmployeeDao
import com.example.data.dao.SystemConfigDao
import com.example.data.model.ApprovalRequest
import com.example.data.model.AttendanceLog
import com.example.data.model.Employee
import com.example.data.model.SystemConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SmartHCRepository(
    private val employeeDao: EmployeeDao,
    private val attendanceDao: AttendanceDao,
    private val approvalDao: ApprovalDao,
    private val systemConfigDao: SystemConfigDao
) {
    val employees: Flow<List<Employee>> = employeeDao.getAllEmployees()
    val attendanceLogs: Flow<List<AttendanceLog>> = attendanceDao.getAllAttendanceLogs()
    val approvalRequests: Flow<List<ApprovalRequest>> = approvalDao.getAllApprovalRequests()

    val isMaintenanceMode: Flow<Boolean> = systemConfigDao.getConfigFlow("maintenance_mode")
        .map { it?.configValue?.toBooleanStrictOrNull() ?: false }

    val announcement: Flow<String> = systemConfigDao.getConfigFlow("announcement")
        .map { it?.configValue ?: "📢 Selamat datang di Smart HC System. Pastikan absensi tepat waktu." }

    val officeName: Flow<String> = systemConfigDao.getConfigFlow("office_name")
        .map { it?.configValue ?: "Smart HC Head Office Tower Jakarta" }

    val officeRadius: Flow<String> = systemConfigDao.getConfigFlow("office_radius")
        .map { it?.configValue ?: "100" }

    suspend fun ensureDefaultData() {
        if (employeeDao.getEmployeeCount() == 0) {
            seedSampleData()
        } else if (attendanceDao.getAttendanceCount() < 5) {
            seedAttendanceHistory()
        }
    }

    private suspend fun seedAttendanceHistory() {
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        val sampleDays = listOf(
            Triple(5, "17 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Budi Pratama", "OTSM23E27A002"), Pair("Siti Rahmawati", "OTSM23E27A003"))),
            Triple(4, "18 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Budi Pratama", "OTSM23E27A002"), Pair("Siti Rahmawati", "OTSM23E27A003"))),
            Triple(3, "19 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Siti Rahmawati", "OTSM23E27A003"))),
            Triple(2, "20 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Budi Pratama", "OTSM23E27A002"), Pair("Siti Rahmawati", "OTSM23E27A003"))),
            Triple(1, "21 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Budi Pratama", "OTSM23E27A002")))
        )

        for ((daysAgo, dateStr, emps) in sampleDays) {
            for ((empName, empNik) in emps) {
                val checkInTime = if (empNik == "OTSM23E27A002" && daysAgo == 2) "08:42 WIB" else "08:0${(1..9).random()} WIB"
                attendanceDao.insertAttendance(
                    AttendanceLog(
                        employeeNama = empName,
                        employeeNik = empNik,
                        type = "Check In",
                        timestamp = now - (daysAgo * oneDay) + 28800000L,
                        timeString = "$dateStr - $checkInTime"
                    )
                )
                attendanceDao.insertAttendance(
                    AttendanceLog(
                        employeeNama = empName,
                        employeeNik = empNik,
                        type = "Check Out",
                        timestamp = now - (daysAgo * oneDay) + 61200000L,
                        timeString = "$dateStr - 17:0${(2..8).random()} WIB"
                    )
                )
            }
        }
    }

    suspend fun seedSampleData() {
        employeeDao.clearAll()
        attendanceDao.clearAll()
        approvalDao.clearAll()

        employeeDao.insertAll(
            listOf(
                Employee(
                    nama = "Ari Rurianto",
                    nik = "OTSM23E27A001",
                    device = "HP-Ari-Primary",
                    jabatan = "IT System Specialist",
                    departemen = "Technology & HC",
                    sisaCuti = 12,
                    email = "ari.rurianto@company.com"
                ),
                Employee(
                    nama = "Budi Pratama",
                    nik = "OTSM23E27A002",
                    device = "HP-Budi-Redmi",
                    jabatan = "Operations Officer",
                    departemen = "Operations",
                    sisaCuti = 9,
                    email = "budi.pratama@company.com"
                ),
                Employee(
                    nama = "Siti Rahmawati",
                    nik = "OTSM23E27A003",
                    device = "HP-Siti-Galaxy",
                    jabatan = "Finance & Tax Specialist",
                    departemen = "Finance",
                    sisaCuti = 14,
                    email = "siti.rahmawati@company.com"
                ),
                Employee(
                    nama = "Dewi Lestari",
                    nik = "OTSM23E27A004",
                    device = "HP-Dewi-Pixel",
                    jabatan = "People Development Lead",
                    departemen = "Technology & HC",
                    sisaCuti = 11,
                    email = "dewi.lestari@company.com"
                )
            )
        )

        val now = System.currentTimeMillis()
        val oneDay = 86400000L

        // Historical attendance for the week
        val historyDays = listOf(
            Triple(4, "18 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Budi Pratama", "OTSM23E27A002"), Pair("Siti Rahmawati", "OTSM23E27A003"), Pair("Dewi Lestari", "OTSM23E27A004"))),
            Triple(3, "19 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Siti Rahmawati", "OTSM23E27A003"), Pair("Dewi Lestari", "OTSM23E27A004"))),
            Triple(2, "20 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Budi Pratama", "OTSM23E27A002"), Pair("Siti Rahmawati", "OTSM23E27A003"), Pair("Dewi Lestari", "OTSM23E27A004"))),
            Triple(1, "21 Agu 2026", listOf(Pair("Ari Rurianto", "OTSM23E27A001"), Pair("Budi Pratama", "OTSM23E27A002"), Pair("Dewi Lestari", "OTSM23E27A004")))
        )

        for ((daysAgo, dateStr, emps) in historyDays) {
            for ((empName, empNik) in emps) {
                val isLate = (empNik == "OTSM23E27A002" && daysAgo == 2)
                val checkInTime = if (isLate) "08:45 WIB" else "07:5${(1..9).random()} WIB"
                attendanceDao.insertAttendance(
                    AttendanceLog(
                        employeeNama = empName,
                        employeeNik = empNik,
                        type = "Check In",
                        timestamp = now - (daysAgo * oneDay) + 28800000L,
                        timeString = "$dateStr - $checkInTime"
                    )
                )
                attendanceDao.insertAttendance(
                    AttendanceLog(
                        employeeNama = empName,
                        employeeNik = empNik,
                        type = "Check Out",
                        timestamp = now - (daysAgo * oneDay) + 61200000L,
                        timeString = "$dateStr - 17:05 WIB"
                    )
                )
            }
        }

        // Today attendance
        attendanceDao.insertAttendance(
            AttendanceLog(
                employeeNama = "Ari Rurianto",
                employeeNik = "OTSM23E27A001",
                type = "Check In",
                timestamp = now - 3600000 * 5,
                timeString = "22 Agu 2026 - 08:02 WIB"
            )
        )
        attendanceDao.insertAttendance(
            AttendanceLog(
                employeeNama = "Budi Pratama",
                employeeNik = "OTSM23E27A002",
                type = "Check In",
                timestamp = now - 3600000 * 4,
                timeString = "22 Agu 2026 - 08:15 WIB"
            )
        )
        attendanceDao.insertAttendance(
            AttendanceLog(
                employeeNama = "Dewi Lestari",
                employeeNik = "OTSM23E27A004",
                type = "Check In",
                timestamp = now - 3600000 * 3,
                timeString = "22 Agu 2026 - 08:28 WIB"
            )
        )

        approvalDao.insertApprovalRequest(
            ApprovalRequest(
                employeeNama = "Siti Rahmawati",
                employeeNik = "OTSM23E27A003",
                type = "Cuti Tahunan",
                reason = "Keperluan keluarga di luar kota",
                timeString = "22 Agu 2026",
                status = "Pending"
            )
        )
    }

    suspend fun addEmployee(
        nama: String,
        nik: String,
        device: String,
        jabatan: String = "Staff Operasional",
        departemen: String = "Operations",
        sisaCuti: Int = 12,
        email: String = ""
    ): Long {
        return employeeDao.insertEmployee(
            Employee(
                nama = nama.trim(),
                nik = nik.trim(),
                device = device.trim(),
                jabatan = jabatan.ifBlank { "Staff Operasional" },
                departemen = departemen.ifBlank { "General" },
                sisaCuti = sisaCuti,
                email = if (email.isNotBlank()) email.trim() else "${nama.trim().lowercase().replace(" ", ".")}@company.com",
                statusRequestReset = false
            )
        )
    }

    suspend fun updateEmployee(employee: Employee) {
        employeeDao.updateEmployee(employee)
    }

    suspend fun deleteEmployee(id: Long) {
        employeeDao.deleteEmployeeById(id)
    }

    suspend fun requestDeviceReset(nik: String) {
        employeeDao.updateResetStatus(nik, true)
    }

    suspend fun approveDeviceReset(nik: String, newDevice: String) {
        employeeDao.approveDeviceReset(nik, newDevice)
    }

    suspend fun recordAttendance(nama: String, nik: String, type: String, timeString: String): Long {
        return attendanceDao.insertAttendance(
            AttendanceLog(
                employeeNama = nama,
                employeeNik = nik,
                type = type,
                timeString = timeString
            )
        )
    }

    suspend fun deleteAttendance(id: Long) {
        attendanceDao.deleteAttendanceById(id)
    }

    suspend fun submitApprovalRequest(nama: String, nik: String, type: String, reason: String, timeString: String): Long {
        return approvalDao.insertApprovalRequest(
            ApprovalRequest(
                employeeNama = nama,
                employeeNik = nik,
                type = type,
                reason = reason,
                timeString = timeString,
                status = "Pending"
            )
        )
    }

    suspend fun updateApprovalStatus(id: Long, status: String, note: String = "") {
        approvalDao.updateStatusWithNote(id, status, note)
    }

    suspend fun setMaintenanceMode(enabled: Boolean) {
        systemConfigDao.setConfig(
            SystemConfig(
                configKey = "maintenance_mode",
                configValue = enabled.toString()
            )
        )
    }

    suspend fun setAnnouncement(text: String) {
        systemConfigDao.setConfig(
            SystemConfig(
                configKey = "announcement",
                configValue = text
            )
        )
    }

    suspend fun setOfficeConfig(name: String, radius: String) {
        systemConfigDao.setConfig(
            SystemConfig(
                configKey = "office_name",
                configValue = name
            )
        )
        systemConfigDao.setConfig(
            SystemConfig(
                configKey = "office_radius",
                configValue = radius
            )
        )
    }
}
