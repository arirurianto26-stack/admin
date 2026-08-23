package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AttendanceLog
import com.example.util.AttendanceExportHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smart HC", appName)
  }

  @Test
  fun `generate HRD CSV and verify header and data`() {
    val sampleLogs = listOf(
      AttendanceLog(
        id = 1L,
        employeeNik = "EMP-001",
        employeeNama = "Budi Santoso",
        jabatan = "Software Engineer",
        type = "Check In",
        timestamp = 1755820800000L,
        timeString = "08:00 WIB",
        dateString = "2026-08-22",
        latitude = -6.2088,
        longitude = 106.8456,
        status = "Tepat Waktu"
      )
    )

    val csv = AttendanceExportHelper.generateHrdCsv(sampleLogs)
    assertTrue("CSV header should contain NIK", csv.contains("NIK"))
    assertTrue("CSV should contain employee name", csv.contains("Budi Santoso"))
    assertTrue("CSV should contain Check In", csv.contains("Check In"))
  }

  @Test
  fun `generate IT JSON and verify telemetry keys`() {
    val sampleLogs = listOf(
      AttendanceLog(
        id = 2L,
        employeeNik = "EMP-002",
        employeeNama = "Siti Rahma",
        jabatan = "HR Officer",
        type = "Check Out",
        timestamp = 1755853200000L,
        timeString = "17:00 WIB",
        dateString = "2026-08-22",
        latitude = -6.2088,
        longitude = 106.8456,
        status = "Sesuai Jadwal"
      )
    )

    val json = AttendanceExportHelper.generateItJson(sampleLogs)
    assertTrue("JSON should contain audit_export_version", json.contains("audit_export_version"))
    assertTrue("JSON should contain EMP-002", json.contains("EMP-002"))
    assertTrue("JSON should contain gps_coordinates", json.contains("gps_coordinates"))
  }

  @Test
  fun `saveExportFile writes to storage successfully`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sampleLogs = listOf(
      AttendanceLog(
        id = 3L,
        employeeNik = "EMP-003",
        employeeNama = "Agus Prasetyo",
        jabatan = "Accountant",
        type = "Check In",
        timestamp = 1755821000000L,
        timeString = "08:05 WIB",
        dateString = "2026-08-22",
        latitude = -6.2088,
        longitude = 106.8456,
        status = "Tepat Waktu"
      )
    )

    val csv = AttendanceExportHelper.generateHrdCsv(sampleLogs)
    val result = AttendanceExportHelper.saveExportFile(
      context = context,
      prefix = "test_export_presensi",
      extension = "csv",
      content = csv,
      recordCount = sampleLogs.size
    )

    assertTrue("Export should be successful", result.isSuccess)
    assertNotNull("File path should not be null", result.fileAbsolutePath)
    assertEquals(1, result.recordCount)
  }
}

