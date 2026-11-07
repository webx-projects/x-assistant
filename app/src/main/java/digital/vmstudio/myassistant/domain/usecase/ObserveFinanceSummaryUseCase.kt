package digital.vmstudio.myassistant.domain.usecase

import digital.vmstudio.myassistant.domain.model.FinanceSummary
import digital.vmstudio.myassistant.domain.repository.AssistantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ObserveFinanceSummaryUseCase(
    private val repository: AssistantRepository
) {
    operator fun invoke(
        startInclusive: Long,
        endExclusive: Long,
        currencyCode: String = "INR"
    ): Flow<FinanceSummary> {
        require(startInclusive < endExclusive) { "The reporting period is invalid." }
        val normalizedCurrency = currencyCode.trim().uppercase()
        require(Regex("^[A-Z]{3}$").matches(normalizedCurrency)) {
            "Currency must be a three-letter ISO code."
        }

        return combine(
            repository.observeIncomeTotal(startInclusive, endExclusive, normalizedCurrency),
            repository.observeExpenseTotal(startInclusive, endExclusive, normalizedCurrency)
        ) { income, expense ->
            FinanceSummary(
                currencyCode = normalizedCurrency,
                incomeMinorUnits = income,
                expenseMinorUnits = expense
            )
        }
    }
}
