package com.barutdev.tullab.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barutdev.tullab.data.local.entity.PaymentRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: PaymentRecordEntity)

    @Query(
        "SELECT * FROM payment_records WHERE studentId = :studentId ORDER BY paidAtEpochMs DESC"
    )
    fun observeByStudent(studentId: Int): Flow<List<PaymentRecordEntity>>

    @Query(
        "DELETE FROM payment_records WHERE studentId = :studentId AND paidAtEpochMs = :paidAtEpochMs"
    )
    suspend fun deleteByStudentAndTimestamp(studentId: Int, paidAtEpochMs: Long): Int

    @Query(
        "SELECT * FROM payment_records WHERE studentId = :studentId ORDER BY paidAtEpochMs DESC LIMIT 1"
    )
    suspend fun getLatestPaymentRecord(studentId: Int): PaymentRecordEntity?
}