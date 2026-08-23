package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.ApprovalDao
import com.example.data.dao.AttendanceDao
import com.example.data.dao.EmployeeDao
import com.example.data.dao.SystemConfigDao
import com.example.data.model.ApprovalRequest
import com.example.data.model.AttendanceLog
import com.example.data.model.Employee
import com.example.data.model.SystemConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Employee::class,
        AttendanceLog::class,
        ApprovalRequest::class,
        SystemConfig::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun approvalDao(): ApprovalDao
    abstract fun systemConfigDao(): SystemConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smarthc_database.db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            // Seed default employees
                            database.employeeDao().insertAll(
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
                                    )
                                )
                            )
                            database.systemConfigDao().setConfig(
                                SystemConfig(
                                    configKey = "maintenance_mode",
                                    configValue = "false"
                                )
                            )
                            database.systemConfigDao().setConfig(
                                SystemConfig(
                                    configKey = "announcement",
                                    configValue = "📢 Pengingat: Batas pengajuan cuti libur nasional & rekap kehadiran sampai tanggal 25 setiap bulannya."
                                )
                            )
                            database.systemConfigDao().setConfig(
                                SystemConfig(
                                    configKey = "office_name",
                                    configValue = "Smart HC Head Office Tower Jakarta"
                                )
                            )
                            database.systemConfigDao().setConfig(
                                SystemConfig(
                                    configKey = "office_radius",
                                    configValue = "100"
                                )
                            )
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
