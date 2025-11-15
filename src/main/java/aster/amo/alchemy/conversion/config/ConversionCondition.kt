package aster.amo.alchemy.conversion.config

import com.google.gson.annotations.SerializedName

/**
 * Condition that determines when a conversion should be applied
 */
data class ConversionCondition(
    val type: ConditionType,
    val path: String? = null,
    val value: String? = null,
    val itemId: String? = null,
    @SerializedName("any")
    val anyConditions: List<ConversionCondition>? = null,
    @SerializedName("all")
    val allConditions: List<ConversionCondition>? = null
)

/**
 * Types of conditions that can be evaluated
 */
enum class ConditionType {
    @SerializedName("component_exists")
    COMPONENT_EXISTS,

    @SerializedName("component_equals")
    COMPONENT_EQUALS,

    @SerializedName("item_id")
    ITEM_ID,

    @SerializedName("any")
    ANY,

    @SerializedName("all")
    ALL,

    @SerializedName("always")
    ALWAYS
}
