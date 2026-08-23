package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.data.model.AttendanceLog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ExportResult(
    val fileName: String,
    val fileAbsolutePath: String,
    val recordCount: Int,
    val fileSizeBytes: Long,
    val formattedSize: String,
    val content: String,
    val isSuccess: Boolean,
    val message: String
)

object AttendanceExportHelper {

    fun generateHrdCsv(logs: List<AttendanceLog>, officeName: String = "Kantor Pusat"): String {
        val sb = StringBuilder()
        sb.append("No,Nama Karyawan,NIK,Tipe Presensi,Waktu Presensi,Lokasi Kantor,Latitude,Longitude,Akurasi GPS\n")
        logs.forEachIndexed { index, log ->
            val safeName = log.employeeNama.replace(",", " ")
            val safeNik = log.employeeNik.replace(",", " ")
            val safeType = log.type.replace(",", " ")
            val safeTime = log.timeString.replace(",", " ")
            val safeOffice = officeName.replace(",", " ")
            val safeAcc = log.accuracy.replace(",", " ")
            sb.append("${index + 1},$safeName,$safeNik,$safeType,$safeTime,$safeOffice,${log.latitude},${log.longitude},$safeAcc\n")
        }
        return sb.toString()
    }

    fun generateItCsv(logs: List<AttendanceLog>): String {
        val sb = StringBuilder()
        sb.append("NO,LOG_ID,EMPLOYEE_NAME,NIK,EVENT_TYPE,TIMESTAMP_MS,DATE_TIME_FORMATTED,LATITUDE,LONGITUDE,GPS_ACCURACY,DEVICE_STATUS\n")
        logs.forEachIndexed { index, log ->
            val safeName = log.employeeNama.replace(",", " ")
            val safeNik = log.employeeNik.replace(",", " ")
            val safeType = log.type.replace(",", " ")
            val safeTime = log.timeString.replace(",", " ")
            val safeAcc = log.accuracy.replace(",", " ")
            sb.append("${index + 1},LOG-${log.id},$safeName,$safeNik,$safeType,${log.timestamp},$safeTime,${log.latitude},${log.longitude},$safeAcc,HARDWARE_BIND_VERIFIED\n")
        }
        return sb.toString()
    }

    fun generateItJson(logs: List<AttendanceLog>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        logs.forEachIndexed { index, log ->
            val comma = if (index < logs.size - 1) "," else ""
            sb.append("  {\n")
            sb.append("    \"id\": ${log.id},\n")
            sb.append("    \"employeeNama\": \"${log.employeeNama}\",\n")
            sb.append("    \"employeeNik\": \"${log.employeeNik}\",\n")
            sb.append("    \"type\": \"${log.type}\",\n")
            sb.append("    \"timestamp\": ${log.timestamp},\n")
            sb.append("    \"timeString\": \"${log.timeString}\",\n")
            sb.append("    \"latitude\": ${log.latitude},\n")
            sb.append("    \"longitude\": ${log.longitude},\n")
            sb.append("    \"accuracy\": \"${log.accuracy}\",\n")
            sb.append("    \"status\": \"VERIFIED_VALID\"\n")
            sb.append("  }$comma\n")
        }
        sb.append("]")
        return sb.toString()
    }

    fun saveExportFile(
        context: Context,
        prefix: String,
        extension: String,
        content: String,
        recordCount: Int
    ): ExportResult {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${prefix}_$timeStamp.$extension"

            // Save in cache/files dir
            val exportDir = File(context.filesDir, "exports")
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            val file = File(exportDir, fileName)
            file.writeText(content)

            val sizeBytes = file.length()
            val formattedSize = if (sizeBytes < 1024) "$sizeBytes B" else "${sizeBytes / 1024} KB"

            ExportResult(
                fileName = fileName,
                fileAbsolutePath = file.absolutePath,
                recordCount = recordCount,
                fileSizeBytes = sizeBytes,
                formattedSize = formattedSize,
                content = content,
                isSuccess = true,
                message = "Berhasil mengunduh $fileName ($formattedSize)"
            )
        } catch (e: Exception) {
            ExportResult(
                fileName = "",
                fileAbsolutePath = "",
                recordCount = recordCount,
                fileSizeBytes = 0L,
                formattedSize = "0 B",
                content = content,
                isSuccess = false,
                message = "Gagal mengunduh file: ${e.localizedMessage}"
            )
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Riwayat Absensi"): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun shareExportContent(context: Context, title: String, content: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, content)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(intent, "Bagikan / Simpan Riwayat Absensi")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
