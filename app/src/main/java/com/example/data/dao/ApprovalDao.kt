package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApprovalRequest
import kotlinx.coroutines.flow.Flow

@Dao
interface ApprovalDao {
    @Query("SELECT * FROM approval_requests ORDER BY timestamp DESC")
    fun getAllApprovalRequests(): Flow<List<ApprovalRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApprovalRequest(request: ApprovalRequest): Long

    @Update
    suspend fun updateApprovalRequest(request: ApprovalRequest)

    @Query("UPDATE approval_requests SET status = :status, reviewerNote = :note WHERE id = :id")
    suspend fun updateStatusWithNote(id: Long, status: String, note: String)

    @Query("DELETE FROM approval_requests WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM approval_requests")
    suspend fun clearAll()
}
