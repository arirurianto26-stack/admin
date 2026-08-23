package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "approval_requests")
data class ApprovalRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeNama: String,
    val employeeNik: String,
    val type: String, // "Cuti Tahunan", "FDA (Formulir Dinas)", "Izin Sakit", "Lembur"
    val reason: String,
    val timestamp: Long = System.currentTimeMillis(),
    val timeString: String,
    val status: String = "Pending", // "Pending", "Approved", "Rejected"
    val reviewerNote: String = ""
)
