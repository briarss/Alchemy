package aster.amo.alchemy.conversion.config

/**
 * A single item conversion rule with conditions and operations
 */
data class ItemConversionRule(
    val id: String,
    val priority: Int = 0,
    val condition: ConversionCondition,
    val operations: List<ConversionOperation>
)
