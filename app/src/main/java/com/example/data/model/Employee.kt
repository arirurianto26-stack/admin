package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nama: String,
    val nik: String,
    val device: String,
    val jabatan: String = "Staff Operasional",
    val departemen: String = "Human Capital & Tech",
    val sisaCuti: Int = 12,
    val email: String = "karyawan@company.com",
    val statusRequestReset: Boolean = false
)
