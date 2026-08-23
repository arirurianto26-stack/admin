package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.RoleNavigationBar
import com.example.ui.hrd.HrdScreen
import com.example.ui.it.ItControlScreen
import com.example.ui.karyawan.KaryawanScreen
import com.example.ui.theme.SmartHCTheme
import com.example.viewmodel.AppRole
import com.example.viewmodel.SmartHCViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SmartHCViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartHCTheme {
                SmartHCApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SmartHCApp(
    viewModel: SmartHCViewModel,
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val selectedEmployeeNik by viewModel.selectedEmployeeNik.collectAsStateWithLifecycle()
    val attendanceLogs by viewModel.attendanceLogs.collectAsStateWithLifecycle()
    val approvalRequests by viewModel.approvalRequests.collectAsStateWithLifecycle()
    val isMaintenanceMode by viewModel.isMaintenanceMode.collectAsStateWithLifecycle()
    val announcement by viewModel.announcement.collectAsStateWithLifecycle()
    val officeName by viewModel.officeName.collectAsStateWithLifecycle()
    val officeRadius by viewModel.officeRadius.collectAsStateWithLifecycle()

    val currentKaryawanModule by viewModel.currentKaryawanModule.collectAsStateWithLifecycle()
    val currentHrdModule by viewModel.currentHrdModule.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDeviceSim.collectAsStateWithLifecycle()
    val loginStatus by viewModel.loginStatus.collectAsStateWithLifecycle()
    val gpsStatus by viewModel.gpsStatus.collectAsStateWithLifecycle()
    val gpsDetails by viewModel.gpsDetails.collectAsStateWithLifecycle()
    val attendanceFeedback by viewModel.attendanceFeedback.collectAsStateWithLifecycle()
    val leaveFeedback by viewModel.leaveFeedback.collectAsStateWithLifecycle()
    val overtimeFeedback by viewModel.overtimeFeedback.collectAsStateWithLifecycle()
    val slipFeedback by viewModel.slipFeedback.collectAsStateWithLifecycle()
    val uiNotice by viewModel.uiNotice.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiNotice) {
        uiNotice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUiNotice()
        }
    }

    val currentEmployee = employees.firstOrNull { it.nik == selectedEmployeeNik } 
        ?: employees.firstOrNull()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        containerColor = Color(0xFF0F172A),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Role Switcher Bar
                RoleNavigationBar(
                    currentRole = currentRole,
                    onRoleSelected = { viewModel.switchRole(it) },
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Main Content View
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    AnimatedContent(
                        targetState = currentRole,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "roleTransition"
                    ) { role ->
                        when (role) {
                            AppRole.KARYAWAN -> {
                                KaryawanScreen(
                                    currentEmployee = currentEmployee,
                                    allEmployees = employees,
                                    attendanceLogs = attendanceLogs,
                                    approvalRequests = approvalRequests,
                                    isMaintenanceMode = isMaintenanceMode,
                                    announcement = announcement,
                                    officeName = officeName,
                                    officeRadius = officeRadius,
                                    activeModule = currentKaryawanModule,
                                    gpsStatus = gpsStatus,
                                    gpsDetails = gpsDetails,
                                    attendanceFeedback = attendanceFeedback,
                                    leaveFeedback = leaveFeedback,
                                    overtimeFeedback = overtimeFeedback,
                                    slipFeedback = slipFeedback,
                                    onSwitchEmployee = { viewModel.switchActiveEmployee(it) },
                                    onOpenModule = { viewModel.openKaryawanModule(it) },
                                    onCloseModule = { viewModel.closeKaryawanModule() },
                                    onSubmitLeave = { type, reason -> viewModel.submitLeave(type, reason) },
                                    onRecordAttendance = { type -> viewModel.recordAttendance(type) },
                                    onSubmitOvertime = { dur, task -> viewModel.submitOvertime(dur, task) },
                                    onCheckSlip = { email -> viewModel.checkSlipEmail(email) }
                                )
                            }
                            AppRole.HRD -> {
                                HrdScreen(
                                    employees = employees,
                                    attendanceLogs = attendanceLogs,
                                    approvalRequests = approvalRequests,
                                    isMaintenanceMode = isMaintenanceMode,
                                    activeModule = currentHrdModule,
                                    onOpenModule = { viewModel.openHrdModule(it) },
                                    onCloseModule = { viewModel.closeHrdModule() },
                                    onAddEmployee = { name, nik, dev, jab, dep, cuti, email, onDone ->
                                        viewModel.addEmployee(name, nik, dev, jab, dep, cuti, email, onDone)
                                    },
                                    onDeleteEmployee = { viewModel.deleteEmployee(it) },
                                    onProcessApproval = { id, st, note -> viewModel.processApproval(id, st, note) },
                                    onApproveResetDevice = { nik, name -> viewModel.approveDeviceReset(nik, name) },
                                    onDeleteAttendance = { viewModel.deleteAttendance(it) }
                                )
                            }
                            AppRole.IT -> {
                                ItControlScreen(
                                    isMaintenanceMode = isMaintenanceMode,
                                    currentAnnouncement = announcement,
                                    currentOfficeName = officeName,
                                    currentOfficeRadius = officeRadius,
                                    totalEmployees = employees.size,
                                    totalAttendance = attendanceLogs.size,
                                    totalApprovals = approvalRequests.size,
                                    attendanceLogs = attendanceLogs,
                                    approvalRequests = approvalRequests,
                                    currentEmployee = currentEmployee,
                                    allEmployees = employees,
                                    selectedDevice = selectedDevice,
                                    loginStatus = loginStatus,
                                    onToggleMaintenance = { viewModel.toggleMaintenance(it) },
                                    onUpdateAnnouncement = { viewModel.updateAnnouncement(it) },
                                    onUpdateOfficeSettings = { name, rad -> viewModel.updateOfficeSettings(name, rad) },
                                    onResetDemoData = { viewModel.resetDemoData() },
                                    onSelectDevice = { viewModel.setSimulatedDevice(it) },
                                    onAttemptLogin = { viewModel.attemptLogin() },
                                    onSwitchEmployee = { viewModel.switchActiveEmployee(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
