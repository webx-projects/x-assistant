package digital.vmstudio.myassistant.domain.usecase

import digital.vmstudio.myassistant.domain.model.MoneyTransaction
import digital.vmstudio.myassistant.domain.repository.AssistantRepository
import java.util.Locale
import java.util.UUID

class CreateTransactionUseCase(
    private val repository: AssistantRepository,
    private val now: () -> Long = System::currentTimeMillis,
    private val newId: () -> String = { UUID.randomUUID().toString() }
) {
    suspend operator fun invoke(
        type: String,
        amountMinorUnits: Long,
        category: String,
        description: String = "",
        currencyCode: String = "INR",
        paymentMethod: String? = null,
        occurredAtMillis: Long = now()
    ): MoneyTransaction {
        val normalizedType = type.trim().uppercase(Locale.ROOT)
        require(normalizedType == "EXPENSE" || normalizedType == "INCOME") {
            "Transaction type must be EXPENSE or INCOME."
        }
        require(amountMinorUnits > 0L) { "Amount must be greater than zero." }
        val normalizedCategory = category.trim()
        require(normalizedCategory.isNotEmpty()) { "A category is required." }
        val normalizedCurrency = currencyCode.trim().uppercase(Locale.ROOT)
        require(Regex("^[A-Z]{3}$").matches(normalizedCurrency)) {
            "Currency must be a three-letter ISO code."
        }

        val transaction = MoneyTransaction(
            id = newId(),
            type = normalizedType,
            amountMinorUnits = amountMinorUnits,
            currencyCode = normalizedCurrency,
            category = normalizedCategory,
            description = description.trim(),
            paymentMethod = paymentMethod?.trim()?.takeIf(String::isNotEmpty),
            occurredAtMillis = occurredAtMillis,
            createdAtMillis = now()
        )
        repository.addTransaction(transaction)
        return transaction
    }
}