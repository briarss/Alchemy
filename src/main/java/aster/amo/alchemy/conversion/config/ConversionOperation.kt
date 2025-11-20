package aster.amo.alchemy.conversion.config

import com.google.gson.annotations.SerializedName

/**
 * Operation to perform during conversion
 */
data class ConversionOperation(
    val type: OperationType,

    // For SET_ITEM_ID
    val itemId: String? = null,

    // For SET_ITEM_ID_FROM_LOOKUP
    val source: String? = null,
    val mapping: Map<String, String>? = null,
    val defaultValue: String? = null,

    // For SET_ITEM_ID_FROM_COMPONENT
    val prefix: String? = null,
    val suffix: String? = null,
    val stripNamespace: Boolean = false,

    // For REMOVE_COMPONENT
    val path: String? = null,

    // For ADD_COMPONENT, SET_COMPONENT
    val component: String? = null,
    val value: ComponentValue? = null,

    // For COPY_COMPONENT
    val from: String? = null,
    val to: String? = null,

    // For TRANSFORM_COMPONENT
    val transformer: String? = null,
    val transformerConfig: Map<String, Any>? = null
)

/**
 * Types of operations that can be performed
 */
enum class OperationType {
    @SerializedName("set_item_id")
    SET_ITEM_ID,

    @SerializedName("set_item_id_from_lookup")
    SET_ITEM_ID_FROM_LOOKUP,

    @SerializedName("set_item_id_from_component")
    SET_ITEM_ID_FROM_COMPONENT,

    @SerializedName("remove_component")
    REMOVE_COMPONENT,

    @SerializedName("add_component")
    ADD_COMPONENT,

    @SerializedName("set_component")
    SET_COMPONENT,

    @SerializedName("copy_component")
    COPY_COMPONENT,

    @SerializedName("remove_custom_data_key")
    REMOVE_CUSTOM_DATA_KEY,

    @SerializedName("transform_component")
    TRANSFORM_COMPONENT
}
