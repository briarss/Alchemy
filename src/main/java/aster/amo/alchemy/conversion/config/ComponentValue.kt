package aster.amo.alchemy.conversion.config

import com.google.gson.annotations.SerializedName

/**
 * Represents a component value with type information for building complex structures
 */
data class ComponentValue(
    val type: ComponentValueType,
    val string: String? = null,
    val number: Number? = null,
    val boolean: Boolean? = null,
    val list: List<ComponentValue>? = null,
    val map: Map<String, ComponentValue>? = null,

    // For copying from source item
    val fromPath: String? = null
)

/**
 * Types of values that can be used in components
 */
enum class ComponentValueType {
    @SerializedName("string")
    STRING,

    @SerializedName("number")
    NUMBER,

    @SerializedName("boolean")
    BOOLEAN,

    @SerializedName("list")
    LIST,

    @SerializedName("map")
    MAP,

    @SerializedName("from_source")
    FROM_SOURCE
}
