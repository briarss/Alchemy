package com.cobblemonodyssey.$mod_id$

import com.google.common.util.concurrent.ThreadFactoryBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import com.cobblemonodyssey.$mod_id$.commands.BaseCommand
import com.cobblemonodyssey.$mod_id$.config.ConfigManager
import com.cobblemonodyssey.$mod_id$.economy.EconomyType
import com.cobblemonodyssey.$mod_id$.economy.IEconomyService
import com.cobblemonodyssey.$mod_id$.storage.IStorage
import com.cobblemonodyssey.$mod_id$.storage.StorageType
import com.cobblemonodyssey.$mod_id$.utils.Utils
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File

class $mod_name$ : ModInitializer {
    companion object {
        lateinit var INSTANCE: $mod_name$

        var MOD_ID = "$mod_id$"
        var MOD_NAME = "$mod_name$"

        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
        val MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

        val asyncScope = CoroutineScope(Dispatchers.IO)

        @JvmStatic
        fun asResource(path: String): ResourceLocation {
            return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
        }
    }

    lateinit var configDir: File
    var storage: IStorage? = null

    lateinit var adventure: FabricServerAudiences
    lateinit var server: MinecraftServer
    lateinit var nbtOpts: RegistryOps<Tag>

    private var economyServices: Map<EconomyType, IEconomyService> = emptyMap()

    val asyncExecutor: ExecutorService = Executors.newFixedThreadPool(8, ThreadFactoryBuilder()
        .setNameFormat("$mod_name$-Async-%d")
        .setDaemon(true)
        .build())

    var gson: Gson = GsonBuilder().disableHtmlEscaping()
        .registerTypeAdapter(StorageType::class.java, StorageType.StorageTypeAdaptor())
        .create()

    var gsonPretty: Gson = gson.newBuilder().setPrettyPrinting().create()

    override fun onInitialize() {
        INSTANCE = this

        this.configDir = File(FabricLoader.getInstance().configDirectory, MOD_ID)
        ConfigManager.load()
        try {
            this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        } catch (e: IOException) {
            Utils.printError(e.message)
            this.storage = null
        }

        this.economyServices = IEconomyService.getLoadedEconomyServices()

        registerEvents()
    }

    private fun registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(ServerLifecycleEvents.ServerStarting { server: MinecraftServer ->
            this.adventure = FabricServerAudiences.of(
                server
            )
            this.server = server
            this.nbtOpts = server.registryAccess().createSerializationContext(NbtOps.INSTANCE)
        })
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            BaseCommand().register(
                dispatcher
            )
        }
    }

    fun reload() {
        ConfigManager.load()
        try {
            this.storage = IStorage.load(ConfigManager.CONFIG.storage)
        } catch (e: IOException) {
            Utils.printError(e.message)
            this.storage = null
        }

        this.economyServices = IEconomyService.getLoadedEconomyServices()
    }
}
