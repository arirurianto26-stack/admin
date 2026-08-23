package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ApprovalRequest
import com.example.data.model.AttendanceLog
import com.example.data.model.Employee
import com.example.data.repository.SmartHCRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppRole(val label: String) {
    KARYAWAN("Karyawan"),
    HRD("HRD"),
    IT("IT Control")
}

enum class KaryawanModule {
    LEAVE,
    ATTENDANCE,
    OVERTIME,
    SLIP,
    HISTORY
}

enum class HrdModule {
    KARYAWAN,
    LAPORAN,
    APPROVAL,
    ANALYTICS,
    HAPUS_ABSEN,
    PAYROLL_SUMMARY
}

enum class GpsStatus {
    IDLE,
    SCANNING,
    SUCCESS
}

data class LoginStatus(
    val isSuccess: Boolean,
    val message: String
)

data class CombinedReportItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val category: String, // "Presensi", "Cuti", "FDA", "Lembur", "Sakit"
    val timestamp: Long,
    val timeFormatted: String,
    val statusBadge: String, // "approved", "rejected", "present", "pending"
    val statusText: String,
    val note: String = ""
)

class SmartHCViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmartHCRepository

    val employees: StateFlow<List<Employee>>
    val attendanceLogs: StateFlow<List<AttendanceLog>>
    val approvalRequests: StateFlow<List<ApprovalRequest>>
    val isMaintenanceMode: StateFlow<Boolean>
    val announcement: StateFlow<String>
    val officeName: StateFlow<String>
    val officeRadius: StateFlow<String>

    private val _currentRole = MutableStateFlow(AppRole.KARYAWAN)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    private val _selectedEmployeeNik = MutableStateFlow("OTSM23E27A001")
    val selectedEmployeeNik: StateFlow<String> = _selectedEmployeeNik.asStateFlow()

    private val _currentKaryawanModule = MutableStateFlow<KaryawanModule?>(null)
    val currentKaryawanModule: StateFlow<KaryawanModule?> = _currentKaryawanModule.asStateFlow()

    private val _currentHrdModule = MutableStateFlow<HrdModule?>(null)
    val currentHrdModule: StateFlow<HrdModule?> = _currentHrdModule.asStateFlow()

    private val _selectedDeviceSim = MutableStateFlow("HP-Ari-Primary")
    val selectedDeviceSim: StateFlow<String> = _selectedDeviceSim.asStateFlow()

    private val _loginStatus = MutableStateFlow(
        LoginStatus(
            isSuccess = true,
            message = "Status: Terverifikasi di HP Terdaftar ✅"
        )
    )
    val loginStatus: StateFlow<LoginStatus> = _loginStatus.asStateFlow()

    private val _gpsStatus = MutableStateFlow(GpsStatus.IDLE)
    val gpsStatus: StateFlow<GpsStatus> = _gpsStatus.asStateFlow()

    private val _gpsDetails = MutableStateFlow("Mencari titik koordinat akurat...")
    val gpsDetails: StateFlow<String> = _gpsDetails.asStateFlow()

    private val _attendanceFeedback = MutableStateFlow<String?>(null)
    val attendanceFeedback: StateFlow<String?> = _attendanceFeedback.asStateFlow()

    private val _leaveFeedback = MutableStateFlow<String?>(null)
    val leaveFeedback: StateFlow<String?> = _leaveFeedback.asStateFlow()

    private val _overtimeFeedback = MutableStateFlow<String?>(null)
    val overtimeFeedback: StateFlow<String?> = _overtimeFeedback.asStateFlow()

    private val _slipFeedback = MutableStateFlow<String?>(null)
    val slipFeedback: StateFlow<String?> = _slipFeedback.asStateFlow()

    private val _uiNotice = MutableStateFlow<String?>(null)
    val uiNotice: StateFlow<String?> = _uiNotice.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = SmartHCRepository(
            employeeDao = db.employeeDao(),
            attendanceDao = db.attendanceDao(),
            approvalDao = db.approvalDao(),
            systemConfigDao = db.systemConfigDao()
        )

        employees = repository.employees.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        attendanceLogs = repository.attendanceLogs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        approvalRequests = repository.approvalRequests.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        isMaintenanceMode = repository.isMaintenanceMode.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false
        )

        announcement = repository.announcement.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "📢 Pengingat: Batas pengajuan cuti libur nasional & rekap kehadiran sampai tanggal 25 setiap bulannya."
        )

        officeName = repository.officeName.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "Smart HC Head Office Tower Jakarta"
        )

        officeRadius = repository.officeRadius.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "100"
        )

        viewModelScope.launch {
            repository.ensureDefaultData()
        }
    }

    fun switchRole(role: AppRole) {
        _currentRole.value = role
        _currentKaryawanModule.value = null
        _currentHrdModule.value = null
    }

    fun switchActiveEmployee(nik: String) {
        _selectedEmployeeNik.value = nik
        val emp = employees.value.firstOrNull { it.nik == nik }
        if (emp != null) {
            _selectedDeviceSim.value = emp.device
            _loginStatus.value = LoginStatus(
                isSuccess = true,
                message = "Status: Terverifikasi di ${emp.device} ✅"
            )
            _uiNotice.value = "Akun aktif dialihkan ke ${emp.nama}"
        }
    }

    fun openKaryawanModule(module: KaryawanModule) {
        _currentKaryawanModule.value = module
        if (module == KaryawanModule.ATTENDANCE) {
            startGpsDetection()
        }
    }

    fun closeKaryawanModule() {
        _currentKaryawanModule.value = null
        _attendanceFeedback.value = null
        _leaveFeedback.value = null
        _overtimeFeedback.value = null
        _slipFeedback.value = null
    }

    fun openHrdModule(module: HrdModule) {
        _currentHrdModule.value = module
    }

    fun closeHrdModule() {
        _currentHrdModule.value = null
    }

    fun setSimulatedDevice(device: String) {
        _selectedDeviceSim.value = device
    }

    fun attemptLogin() {
        val currentEmp = employees.value.firstOrNull { it.nik == _selectedEmployeeNik.value } 
            ?: employees.value.firstOrNull()

        if (currentEmp == null) return

        val selected = _selectedDeviceSim.value
        if (selected == currentEmp.device) {
            _loginStatus.value = LoginStatus(
                isSuccess = true,
                message = "Status: Login Berhasil di HP Terdaftar (${currentEmp.device}) ✅"
            )
            _uiNotice.value = "Login Berhasil! Perangkat terverifikasi resmi."
        } else {
            viewModelScope.launch {
                repository.requestDeviceReset(currentEmp.nik)
            }
            _loginStatus.value = LoginStatus(
                isSuccess = false,
                message = "❌ AKSES DITOLAK! Perangkat ($selected) tidak cocok dengan ${currentEmp.device}. Permintaan reset dikirim ke HRD."
            )
            _uiNotice.value = "Peringatan: HP belum terdaftar! Permintaan reset dikirim ke HRD."
        }
    }

    fun startGpsDetection() {
        _gpsStatus.value = GpsStatus.SCANNING
        _gpsDetails.value = "Mengambil koordinat satelit GPS..."
        _attendanceFeedback.value = null

        viewModelScope.launch {
            delay(1200)
            _gpsStatus.value = GpsStatus.SUCCESS
            _gpsDetails.value = "Lat: -6.2088° S, Long: 106.8456° E (Radius: 24m dari ${officeName.value})"
        }
    }

    private fun isSameDay(timestamp1: Long, timestamp2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun recordAttendance(type: String) {
        val currentEmp = employees.value.firstOrNull { it.nik == _selectedEmployeeNik.value } 
            ?: employees.value.firstOrNull() ?: Employee(nama = "Ari Rurianto", nik = "OTSM23E27A001", device = "HP-Ari-Primary")

        val now = System.currentTimeMillis()
        val currentLogs = attendanceLogs.value.filter { it.employeeNik == currentEmp.nik }

        val alreadyCheckedIn = currentLogs.firstOrNull { 
            it.type.contains("In", ignoreCase = true) && isSameDay(it.timestamp, now) 
        }
        val alreadyCheckedOut = currentLogs.firstOrNull { 
            it.type.contains("Out", ignoreCase = true) && isSameDay(it.timestamp, now) 
        }

        val isCheckInAttempt = type.contains("In", ignoreCase = true)
        val isCheckOutAttempt = type.contains("Out", ignoreCase = true)

        if (isCheckInAttempt && alreadyCheckedIn != null) {
            val timeDone = alreadyCheckedIn.timeString.substringAfter("- ").trim()
            _attendanceFeedback.value = "⚠️ Tidak dapat absen ganda! Anda sudah melakukan Check In hari ini pada pukul $timeDone."
            return
        }

        if (isCheckOutAttempt && alreadyCheckedOut != null) {
            val timeDone = alreadyCheckedOut.timeString.substringAfter("- ").trim()
            _attendanceFeedback.value = "⚠️ Tidak dapat absen ganda! Anda sudah melakukan Check Out hari ini pada pukul $timeDone."
            return
        }

        if (isCheckOutAttempt && alreadyCheckedIn == null) {
            _attendanceFeedback.value = "⚠️ Anda belum melakukan Check In hari ini! Silakan lakukan Check In terlebih dahulu sebelum Check Out."
            return
        }

        val sdf = SimpleDateFormat("dd MMM yyyy - HH:mm 'WIB'", Locale("id", "ID"))
        val timeString = sdf.format(Date(now))

        viewModelScope.launch {
            repository.recordAttendance(
                nama = currentEmp.nama,
                nik = currentEmp.nik,
                type = type,
                timeString = timeString
            )
            val timeShort = SimpleDateFormat("HH:mm 'WIB'", Locale("id", "ID")).format(Date(now))
            _attendanceFeedback.value = "✅ Berhasil melakukan $type pada pukul $timeShort (${officeName.value})."
        }
    }

    fun submitLeave(type: String, reason: String) {
        if (reason.isBlank()) {
            _leaveFeedback.value = "⚠️ Mohon masukkan keterangan atau alasan pengajuan!"
            return
        }

        val currentEmp = employees.value.firstOrNull { it.nik == _selectedEmployeeNik.value } 
            ?: employees.value.firstOrNull() ?: Employee(nama = "Ari Rurianto", nik = "OTSM23E27A001", device = "HP-Ari-Primary")

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val timeString = sdf.format(Date())

        viewModelScope.launch {
            repository.submitApprovalRequest(
                nama = currentEmp.nama,
                nik = currentEmp.nik,
                type = type,
                reason = reason,
                timeString = timeString
            )
            _leaveFeedback.value = "✅ Pengajuan $type berhasil dikirim ke HRD!"
            delay(1000)
            _currentKaryawanModule.value = null
            _leaveFeedback.value = null
            _uiNotice.value = "Pengajuan $type berhasil diajukan."
        }
    }

    fun submitOvertime(duration: String, taskDescription: String) {
        if (duration.isBlank() || taskDescription.isBlank()) {
            _overtimeFeedback.value = "⚠️ Mohon lengkapi durasi dan deskripsi pekerjaan lembur!"
            return
        }

        val currentEmp = employees.value.firstOrNull { it.nik == _selectedEmployeeNik.value } 
            ?: employees.value.firstOrNull() ?: Employee(nama = "Ari Rurianto", nik = "OTSM23E27A001", device = "HP-Ari-Primary")

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val timeString = sdf.format(Date())

        viewModelScope.launch {
            repository.submitApprovalRequest(
                nama = currentEmp.nama,
                nik = currentEmp.nik,
                type = "Lembur ($duration Jam)",
                reason = taskDescription,
                timeString = timeString
            )
            _overtimeFeedback.value = "✅ Pengajuan lembur ($duration Jam) berhasil dikirim ke HRD!"
            delay(1000)
            _currentKaryawanModule.value = null
            _overtimeFeedback.value = null
            _uiNotice.value = "Pengajuan lembur berhasil diajukan."
        }
    }

    fun checkSlipEmail(email: String) {
        if (email.isBlank() || !email.contains("@")) {
            _slipFeedback.value = "⚠️ Harap masukkan format alamat email yang valid!"
            return
        }
        _slipFeedback.value = "✅ Tautan dan rincian slip gaji PDF telah dikirim ke $email."
    }

    fun addEmployee(
        nama: String,
        nik: String,
        device: String,
        jabatan: String = "Staff Operasional",
        departemen: String = "Technology & HC",
        sisaCuti: Int = 12,
        email: String = "",
        onSuccess: () -> Unit
    ) {
        if (nama.isBlank() || nik.isBlank() || device.isBlank()) {
            _uiNotice.value = "Semua kolom utama (Nama, NIK, Perangkat HP) wajib diisi!"
            return
        }

        viewModelScope.launch {
            repository.addEmployee(
                nama = nama,
                nik = nik,
                device = device,
                jabatan = jabatan,
                departemen = departemen,
                sisaCuti = sisaCuti,
                email = email
            )
            _uiNotice.value = "Karyawan baru $nama berhasil didaftarkan!"
            onSuccess()
        }
    }

    fun updateEmployee(employee: Employee) {
        viewModelScope.launch {
            repository.updateEmployee(employee)
            _uiNotice.value = "Data karyawan ${employee.nama} berhasil diperbarui."
        }
    }

    fun deleteEmployee(id: Long) {
        viewModelScope.launch {
            repository.deleteEmployee(id)
            _uiNotice.value = "Karyawan berhasil dihapus dari sistem."
        }
    }

    fun processApproval(requestId: Long, status: String, note: String = "") {
        viewModelScope.launch {
            repository.updateApprovalStatus(requestId, status, note)
            _uiNotice.value = "Pengajuan telah di-$status."
        }
    }

    fun approveDeviceReset(nik: String, employeeName: String) {
        viewModelScope.launch {
            val emp = employees.value.firstOrNull { it.nik == nik }
            val newDevice = if (emp != null && emp.device.contains("Primary")) "HP-Ari-NewDevice" else "HP-Baru-Verified"
            repository.approveDeviceReset(nik, newDevice)
            _uiNotice.value = "Reset device untuk $employeeName disetujui! HP baru ($newDevice) terikat resmi."
        }
    }

    fun deleteAttendance(id: Long) {
        viewModelScope.launch {
            repository.deleteAttendance(id)
            _uiNotice.value = "Data presensi berhasil dihapus."
        }
    }

    fun toggleMaintenance(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMaintenanceMode(enabled)
            _uiNotice.value = if (enabled) "System Maintenance Mode DIAKTIFKAN." else "System Maintenance Mode DINONAKTIFKAN."
        }
    }

    fun updateAnnouncement(text: String) {
        viewModelScope.launch {
            repository.setAnnouncement(text)
            _uiNotice.value = "Pengumuman broadcast berhasil diperbarui."
        }
    }

    fun updateOfficeSettings(name: String, radius: String) {
        viewModelScope.launch {
            repository.setOfficeConfig(name, radius)
            _uiNotice.value = "Pengaturan geofence kantor disimpan."
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.seedSampleData()
            _selectedEmployeeNik.value = "OTSM23E27A001"
            _selectedDeviceSim.value = "HP-Ari-Primary"
            _loginStatus.value = LoginStatus(isSuccess = true, message = "Status: Terverifikasi di HP Terdaftar ✅")
            _uiNotice.value = "Data simulasi telah di-reset ke data bawaan."
        }
    }

    fun clearUiNotice() {
        _uiNotice.value = null
    }
}
