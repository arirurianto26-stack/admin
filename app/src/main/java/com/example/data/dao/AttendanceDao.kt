package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttendanceLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_logs ORDER BY timestamp DESC")
    fun getAllAttendanceLogs(): Flow<List<AttendanceLog>>

    @Query("SELECT COUNT(*) FROM attendance_logs")
    suspend fun getAttendanceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(log: AttendanceLog): Long

    @Query("DELETE FROM attendance_logs WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)

    @Query("DELETE FROM attendance_logs")
    suspend fun clearAll()
}
