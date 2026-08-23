package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY id ASC")
    fun getAllEmployees(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): Employee?

    @Query("SELECT * FROM employees WHERE nik = :nik LIMIT 1")
    suspend fun getEmployeeByNik(nik: String): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: Employee): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(employees: List<Employee>)

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Query("UPDATE employees SET statusRequestReset = :status WHERE nik = :nik")
    suspend fun updateResetStatus(nik: String, status: Boolean)

    @Query("UPDATE employees SET device = :newDevice, statusRequestReset = 0 WHERE nik = :nik")
    suspend fun approveDeviceReset(nik: String, newDevice: String)

    @Query("UPDATE employees SET sisaCuti = :sisa WHERE nik = :nik")
    suspend fun updateSisaCuti(nik: String, sisa: Int)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Long)

    @Query("DELETE FROM employees")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int
}
