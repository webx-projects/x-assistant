package digital.vmstudio.myassistant.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceSummaryTest {
    @Test
    fun netIsIncomeMinusExpensesInMinorUnits() {
        val summary = FinanceSummary(
            currencyCode = "INR",
            incomeMinorUnits = 125_000,
            expenseMinorUnits = 45_500
        )

        assertEquals(79_500L, summary.netMinorUnits)
    }

    @Test
    fun expensesGreaterThanIncomeProduceNegativeNet() {
        val summary = FinanceSummary(
            currencyCode = "INR",
            incomeMinorUnits = 10_000,
            expenseMinorUnits = 12_500
        )

        assertEquals(-2_500L, summary.netMinorUnits)
    }

    @Test
    fun zeroActivityHasZeroNet() {
        val summary = FinanceSummary(
            currencyCode = "INR",
            incomeMinorUnits = 0,
            expenseMinorUnits = 0
        )

        assertEquals(0L, summary.netMinorUnits)
    }
}
