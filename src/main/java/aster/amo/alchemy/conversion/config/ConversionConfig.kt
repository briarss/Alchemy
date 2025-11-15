package aster.amo.alchemy.conversion.config

/**
 * Root configuration for item transmutations loaded from JSON files
 */
data class ConversionConfig(
    val conversions: List<ItemConversionRule> = emptyList()
)
