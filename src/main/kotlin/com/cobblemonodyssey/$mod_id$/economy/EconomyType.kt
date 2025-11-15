package com.cobblemonodyssey.$mod_id$.economy

import com.google.gson.*
import com.cobblemonodyssey.$mod_id$.economy.services.ImpactorEconomyService
import com.cobblemonodyssey.$mod_id$.utils.Utils
import net.fabricmc.loader.api.FabricLoader
import java.lang.reflect.Type

enum class EconomyType(
    val identifier: String,
    val modId: String,
    val clazz: Class<out IEconomyService>
) {
    IMPACTOR("impactor", "impactor", ImpactorEconomyService::class.java)

    fun isModPresent() : Boolean {
        return FabricLoader.getInstance().isModLoaded(modId)
    }

    companion object {
        fun valueOfAnyCase(name: String): EconomyType? {
            for (type in entries) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }

    internal class EconomyTypeAdaptor : JsonSerializer<EconomyType>, JsonDeserializer<EconomyType> {
        override fun serialize(src: EconomyType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.identifier)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): EconomyType? {
            val economyType = valueOfAnyCase(json.asString)

            if (economyType == null) {
                Utils.printError("Could not deserialize EconomyType '${json.asString}'!")
                return null
            }

            return economyType
        }
    }
}
