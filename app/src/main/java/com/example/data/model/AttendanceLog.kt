package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_logs")
data class AttendanceLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeNama: String,
    val employeeNik: String,
    val type: String, // "Check In" or "Check Out"
    val timestamp: Long = System.currentTimeMillis(),
    val timeString: String,
    val latitude: Double = -6.200000,
    val longitude: Double = 106.816666,
    val accuracy: String = "±3 meter"
)
