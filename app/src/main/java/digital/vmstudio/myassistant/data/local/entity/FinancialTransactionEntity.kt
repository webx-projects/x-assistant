package digital.vmstudio.myassistant.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "financial_transactions",
    indices = [
        Index(value = ["occurredAtMillis"]),
        Index(value = ["type", "occurredAtMillis"]),
        Index(value = ["category", "occurredAtMillis"])
    ]
)
data class FinancialTransactionEntity(
    @PrimaryKey val transactionId: String,
    /** EXPENSE or INCOME. */
    val type: String,
    /** Store money as integer minor units (paise for INR), never floating point. */
    val amountMinorUnits: Long,
    val currencyCode: String = "INR",
    val category: String,
    val description: String = "",
    val paymentMethod: String? = null,
    val occurredAtMillis: Long,
    val createdAtMillis: Long
)
