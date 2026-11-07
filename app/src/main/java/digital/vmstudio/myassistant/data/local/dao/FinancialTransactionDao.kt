package digital.vmstudio.myassistant.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import digital.vmstudio.myassistant.data.local.entity.FinancialTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinancialTransactionDao {
    @Query("SELECT * FROM financial_transactions ORDER BY occurredAtMillis DESC")
    fun observeTransactions(): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE occurredAtMillis >= :startInclusive AND occurredAtMillis < :endExclusive ORDER BY occurredAtMillis DESC")
    fun observeTransactionsBetween(startInclusive: Long, endExclusive: Long): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT COALESCE(SUM(amountMinorUnits), 0) FROM financial_transactions WHERE type = 'EXPENSE' AND currencyCode = :currencyCode AND occurredAtMillis >= :startInclusive AND occurredAtMillis < :endExclusive")
    fun observeExpenseTotal(startInclusive: Long, endExclusive: Long, currencyCode: String = "INR"): Flow<Long>

    @Query("SELECT COALESCE(SUM(amountMinorUnits), 0) FROM financial_transactions WHERE type = 'INCOME' AND currencyCode = :currencyCode AND occurredAtMillis >= :startInclusive AND occurredAtMillis < :endExclusive")
    fun observeIncomeTotal(startInclusive: Long, endExclusive: Long, currencyCode: String = "INR"): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTransaction(transaction: FinancialTransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: FinancialTransactionEntity)

    @Query("DELETE FROM financial_transactions WHERE transactionId = :transactionId")
    suspend fun deleteTransaction(transactionId: String)
}
