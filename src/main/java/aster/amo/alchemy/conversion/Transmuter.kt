package aster.amo.alchemy.conversion

import aster.amo.alchemy.Alchemy
import aster.amo.alchemy.config.ConfigManager
import aster.amo.alchemy.conversion.config.*
import com.mojang.serialization.*
import net.minecraft.nbt.*

/**
 * A flexible, JSON-configurable item data converter that transforms ItemStacks
 * during deserialization based on rules defined in config/alchemy/transmutations/
 */
object Transmuter {

    /**
     * Main conversion entry point called from ItemStackMixin
     */
    fun <T> convertItem(input: T, ops: DynamicOps<T>): T {
        val dynamic = Dynamic(ops, input)
        var result = dynamic

        if (ConfigManager.CONFIG.debug) {
            val itemId = getStringAtPath(dynamic, "id")
            Alchemy.LOGGER.info("[Conversion] Processing item: $itemId")
        }

        // Try each conversion rule in priority order
        for (rule in ConfigManager.conversions) {
            if (ConfigManager.CONFIG.debug) {
                Alchemy.LOGGER.info("[Conversion] Checking rule: ${rule.id} (priority: ${rule.priority})")
            }

            if (evaluateCondition(result, rule.condition)) {
                if (ConfigManager.CONFIG.debug) {
                    Alchemy.LOGGER.info("[Conversion] ✓ Rule ${rule.id} matched! Applying ${rule.operations.size} operations")
                }

                val beforeId = getStringAtPath(result, "id")
                result = applyOperations(result, rule.operations)
                val afterId = getStringAtPath(result, "id")

                if (ConfigManager.CONFIG.debug) {
                    if (beforeId != afterId) {
                        Alchemy.LOGGER.info("[Conversion] Item transformed: $beforeId → $afterId")
                    } else {
                        Alchemy.LOGGER.info("[Conversion] Item modified (ID unchanged): $beforeId")
                    }
                }

                // Only apply the first matching rule
                break
            } else if (ConfigManager.CONFIG.debug) {
                Alchemy.LOGGER.info("[Conversion] ✗ Rule ${rule.id} did not match")
            }
        }

        return result.value
    }

    /**
     * Evaluate a condition against the current item data
     */
    private fun <T> evaluateCondition(dynamic: Dynamic<T>, condition: ConversionCondition): Boolean {
        return when (condition.type) {
            ConditionType.ALWAYS -> true

            ConditionType.COMPONENT_EXISTS -> {
                val path = condition.path ?: return false
                val exists = getValueAtPath(dynamic, path) != null
                if (ConfigManager.CONFIG.debug) {
                    Alchemy.LOGGER.info("[Conversion]   Checking existence at path '$path': $exists")
                }
                exists
            }

            ConditionType.COMPONENT_EQUALS -> {
                val path = condition.path ?: return false
                val expectedValue = condition.value ?: return false
                val actualTag = getValueAtPath(dynamic, path)

                val matches = compareValues(actualTag, expectedValue)

                if (ConfigManager.CONFIG.debug) {
                    val actualStr = actualTag?.let { tagToString(it) } ?: "null"
                    Alchemy.LOGGER.info("[Conversion]   Comparing at path '$path': actual='$actualStr' vs expected='$expectedValue' -> $matches")
                }
                matches
            }

            ConditionType.ITEM_ID -> {
                val expectedId = condition.itemId ?: return false
                val actualId = getStringAtPath(dynamic, "id")
                if (ConfigManager.CONFIG.debug) {
                    Alchemy.LOGGER.info("[Conversion]   Checking item ID: actual='$actualId' vs expected='$expectedId'")
                }
                actualId == expectedId
            }

            ConditionType.ANY -> {
                val conditions = condition.anyConditions ?: return false
                conditions.any { evaluateCondition(dynamic, it) }
            }

            ConditionType.ALL -> {
                val conditions = condition.allConditions ?: return false
                conditions.all { evaluateCondition(dynamic, it) }
            }
        }
    }

    /**
     * Apply a list of operations to transform the item
     */
    private fun <T> applyOperations(
        dynamic: Dynamic<T>,
        operations: List<ConversionOperation>
    ): Dynamic<T> {
        var result = dynamic

        for (operation in operations) {
            result = applyOperation(result, operation)
        }

        return result
    }

    /**
     * Apply a single operation
     */
    private fun <T> applyOperation(dynamic: Dynamic<T>, operation: ConversionOperation): Dynamic<T> {
        if (ConfigManager.CONFIG.debug) {
            Alchemy.LOGGER.info("[Conversion]   Operation: ${operation.type}")
        }

        return when (operation.type) {
            OperationType.SET_ITEM_ID -> {
                val newId = operation.itemId ?: return dynamic
                if (ConfigManager.CONFIG.debug) {
                    Alchemy.LOGGER.info("[Conversion]     Setting item ID to: $newId")
                }
                setValueAtPath(dynamic, "id", dynamic.ops.createString(newId))
            }

            OperationType.SET_ITEM_ID_FROM_LOOKUP -> {
                val sourcePath = operation.source ?: return dynamic
                val mapping = operation.mapping ?: return dynamic
                val oldValue = getStringAtPath(dynamic, sourcePath) ?: run {
                    if (ConfigManager.CONFIG.debug) {
                        Alchemy.LOGGER.warn("[Conversion]     Source path not found: $sourcePath")
                    }
                    return dynamic
                }
                val newId = mapping[oldValue] ?: operation.defaultValue ?: run {
                    Alchemy.LOGGER.warn("[Conversion]     No mapping found for '$oldValue' in lookup table")
                    return dynamic
                }
                if (ConfigManager.CONFIG.debug) {
                    Alchemy.LOGGER.info("[Conversion]     Mapping: $oldValue → $newId")
                }
                setValueAtPath(dynamic, "id", dynamic.ops.createString(newId))
            }

            OperationType.SET_ITEM_ID_FROM_COMPONENT -> {
                val sourcePath = operation.source ?: return dynamic
                var newId = getStringAtPath(dynamic, sourcePath) ?: return dynamic

                // Strip namespace if requested (e.g., "flourish:abomasite" -> "abomasite")
                if (operation.stripNamespace && newId.contains(':')) {
                    newId = newId.substringAfter(':')
                }

                // Apply prefix and/or suffix if provided
                if (operation.prefix != null) {
                    newId = operation.prefix + newId
                }
                if (operation.suffix != null) {
                    newId = newId + operation.suffix
                }

                setValueAtPath(dynamic, "id", dynamic.ops.createString(newId))
            }

            OperationType.REMOVE_COMPONENT -> {
                val path = operation.path ?: return dynamic
                if (ConfigManager.CONFIG.debug) {
                    Alchemy.LOGGER.info("[Conversion]     Removing component at: $path")
                }
                removeAtPath(dynamic, path)
            }

            OperationType.REMOVE_CUSTOM_DATA_KEY -> {
                val key = operation.path ?: return dynamic
                if (ConfigManager.CONFIG.debug) {
                    Alchemy.LOGGER.info("[Conversion]     Removing custom_data key: $key")
                }
                removeAtPath(dynamic, "components.minecraft:custom_data.$key")
            }

            OperationType.ADD_COMPONENT, OperationType.SET_COMPONENT -> {
                val componentPath = operation.component ?: return dynamic
                val value = operation.value ?: return dynamic
                val encodedValue = encodeComponentValue(dynamic.ops, value, dynamic) ?: return dynamic
                setValueAtPath(dynamic, "components.$componentPath", encodedValue)
            }

            OperationType.COPY_COMPONENT -> {
                val fromPath = operation.from ?: return dynamic
                val toPath = operation.to ?: return dynamic
                val value = getValueAtPath(dynamic, fromPath) ?: return dynamic
                setValueAtPath(dynamic, toPath, value)
            }

            OperationType.TRANSFORM_COMPONENT -> {
                // Custom transformers can be implemented here in the future
                Alchemy.LOGGER.warn("TRANSFORM_COMPONENT not yet implemented")
                dynamic
            }
        }
    }

    /**
     * Get a value at a dot-separated path (e.g., "components.minecraft:custom_data.key")
     */
    private fun <T> getValueAtPath(dynamic: Dynamic<T>, path: String): T? {
        val parts = path.split(".")
        var current = OptionalDynamic(dynamic.ops, DataResult.success(dynamic))

        for (part in parts) {
            current = current.get(part)
            if (!current.result().isPresent) {
                return null
            }
        }

        return current.result().map { it.value }.orElse(null)
    }

    /**
     * Get a string value at a path
     */
    private fun <T> getStringAtPath(dynamic: Dynamic<T>, path: String): String? {
        val value = getValueAtPath(dynamic, path) ?: return null

        // Try to parse as string from NBT
        if (value is StringTag) {
            return value.asString
        }

        // Handle byte tags that might represent booleans (must check before NumericTag since ByteTag extends NumericTag)
        if (value is ByteTag) {
            val byteVal = value.asByte
            // If it's 0 or 1, it might be a boolean, but return numeric string for consistency
            return byteVal.toString()
        }

        // Handle numeric NBT tags (includes IntTag, LongTag, FloatTag, DoubleTag, ShortTag)
        if (value is NumericTag) {
            return value.asNumber.toString()
        }

        // Try to get string result from dynamic
        val parts = path.split(".")
        var current = OptionalDynamic(dynamic.ops, DataResult.success(dynamic))
        for (part in parts) {
            current = current.get(part)
        }

        return current.asString().result().orElse(null)
    }

    /**
     * Convert an NBT tag to a string representation
     */
    private fun <T> tagToString(tag: T): String {
        return when (tag) {
            is StringTag -> tag.asString
            is NumericTag -> tag.asNumber.toString()
            else -> tag.toString()
        }
    }

    /**
     * Compare an NBT tag value with an expected value (from JSON config)
     * Handles numeric comparisons properly (19 == 19.0)
     */
    private fun <T> compareValues(actualTag: T?, expectedValue: Any): Boolean {
        if (actualTag == null) return false

        // If actual is numeric, try numeric comparison first
        if (actualTag is NumericTag) {
            val actualNumber = actualTag.asNumber

            // Try to parse expected value as number
            val expectedNumber = when (expectedValue) {
                is Number -> expectedValue
                is String -> expectedValue.toDoubleOrNull()
                else -> null
            }

            if (expectedNumber != null) {
                // Compare as doubles for flexibility (19.0 == 19)
                return actualNumber.toDouble() == expectedNumber.toDouble()
            }
        }

        // If actual is string, compare as strings
        if (actualTag is StringTag) {
            return actualTag.asString == expectedValue.toString()
        }

        // Fallback to string comparison
        return tagToString(actualTag) == expectedValue.toString()
    }

    /**
     * Set a value at a path, creating intermediate objects as needed
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> setValueAtPath(dynamic: Dynamic<T>, path: String, value: T): Dynamic<T> {
        val parts = path.split(".")
        if (parts.isEmpty()) return dynamic

        if (parts.size == 1) {
            return dynamic.set(parts[0], Dynamic(dynamic.ops, value))
        }

        return dynamic.update(parts[0]) { child ->
            setValueAtPath(child as Dynamic<T>, parts.drop(1).joinToString("."), value) as Dynamic<*>
        } as Dynamic<T>
    }

    /**
     * Remove a value at a path
     */
    private fun <T> removeAtPath(dynamic: Dynamic<T>, path: String): Dynamic<T> {
        val parts = path.split(".")
        if (parts.isEmpty()) return dynamic

        if (parts.size == 1) {
            return dynamic.remove(parts[0])
        }

        return dynamic.update(parts[0]) { child ->
            removeAtPath(child, parts.drop(1).joinToString("."))
        }
    }

    /**
     * Encode a ComponentValue into the appropriate ops type
     */
    private fun <T> encodeComponentValue(
        ops: DynamicOps<T>,
        value: ComponentValue,
        sourceDynamic: Dynamic<T>
    ): T? {
        return when (value.type) {
            ComponentValueType.STRING -> {
                value.string?.let { ops.createString(it) }
            }

            ComponentValueType.NUMBER -> {
                value.number?.let {
                    when (it) {
                        is Int -> ops.createInt(it)
                        is Long -> ops.createLong(it)
                        is Float -> ops.createFloat(it)
                        is Double -> ops.createDouble(it)
                        else -> ops.createDouble(it.toDouble())
                    }
                }
            }

            ComponentValueType.BOOLEAN -> {
                value.boolean?.let { ops.createBoolean(it) }
            }

            ComponentValueType.LIST -> {
                value.list?.let { list ->
                    val encoded = list.mapNotNull { encodeComponentValue(ops, it, sourceDynamic) }
                    ops.createList(encoded.stream() as java.util.stream.Stream<T>)
                }
            }

            ComponentValueType.MAP -> {
                value.map?.let { map ->
                    var result = ops.createMap(emptyMap())
                    for ((k, v) in map) {
                        val encodedValue = encodeComponentValue(ops, v, sourceDynamic) ?: continue
                        result = ops.mergeToMap(result, ops.createString(k), encodedValue).result().orElse(result)
                    }
                    result
                }
            }

            ComponentValueType.FROM_SOURCE -> {
                value.fromPath?.let { path ->
                    getValueAtPath(sourceDynamic, path)
                }
            }
        }
    }

    fun init() {
        Alchemy.LOGGER.info("ItemDataConverter initialized")
    }
}
