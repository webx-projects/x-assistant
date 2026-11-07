package digital.vmstudio.myassistant.domain.model

data class FinanceSummary(
    val currencyCode: String,
    val incomeMinorUnits: Long,
    val expenseMinorUnits: Long
) {
    val netMinorUnits: Long
        get() = incomeMinorUnits - expenseMinorUnits
}
