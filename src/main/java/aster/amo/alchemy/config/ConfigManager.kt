package aster.amo.alchemy.config

import com.google.gson.stream.JsonReader
import aster.amo.alchemy.Alchemy
import aster.amo.alchemy.conversion.config.ConversionConfig
import aster.amo.alchemy.conversion.config.ItemConversionRule
import aster.amo.alchemy.utils.Utils
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object ConfigManager {
    private var assetPackage = "assets/${Alchemy.MOD_ID}"

    lateinit var CONFIG: AlchemyConfig
    var conversions: List<ItemConversionRule> = emptyList()
        private set

    fun load() {
        // Load defaulted configs if they do not exist
        copyDefaults()

        // Load all files
        CONFIG = loadFile("config.json", AlchemyConfig())

        // Load all conversion files
        conversions = loadConversions()
    }

    private fun loadConversions(): List<ItemConversionRule> {
        val conversionsDir = Alchemy.INSTANCE.configDir.resolve("transmutations")
        if (!conversionsDir.exists()) {
            conversionsDir.mkdirs()
            Alchemy.LOGGER.info("Created transmutations directory at ${conversionsDir.absolutePath}")
        }

        val allRules = mutableListOf<ItemConversionRule>()

        conversionsDir.listFiles { file -> file.extension == "json" }?.forEach { file ->
            try {
                FileReader(file).use { reader ->
                    val config = Alchemy.INSTANCE.gsonPretty.fromJson(
                        reader,
                        ConversionConfig::class.java
                    )
                    allRules.addAll(config.conversions)
                    Alchemy.LOGGER.info("Loaded ${config.conversions.size} transmutation rules from ${file.name}")
                }
            } catch (e: Exception) {
                Alchemy.LOGGER.error("Failed to load transmutation file ${file.name}: ${e.message}", e)
            }
        }

        // Sort by priority (higher priority first)
        val sorted = allRules.sortedByDescending { it.priority }
        Alchemy.LOGGER.info("Loaded ${sorted.size} total transmutation rules")
        return sorted
    }

    private fun copyDefaults() {
        val classLoader = Alchemy::class.java.classLoader

        Alchemy.INSTANCE.configDir.mkdirs()

        attemptDefaultFileCopy(classLoader, "config.json")
    }

    fun <T : Any> loadFile(filename: String, default: T, path: String = "", create: Boolean = false): T {
        var dir = Alchemy.INSTANCE.configDir
        if (path.isNotEmpty()) {
            dir = dir.resolve(path)
        }
        val file = File(dir, filename)
        var value: T = default
        try {
            Files.createDirectories(Alchemy.INSTANCE.configDir.toPath())
            if (file.exists()) {
                FileReader(file).use { reader ->
                    val jsonReader = JsonReader(reader)
                    value = Alchemy.INSTANCE.gsonPretty.fromJson(jsonReader, default::class.java)
                }
            } else if (create) {
                Files.createFile(file.toPath())
                FileWriter(file).use { fileWriter ->
                    fileWriter.write(Alchemy.INSTANCE.gsonPretty.toJson(default))
                    fileWriter.flush()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return value
    }

    fun <T> saveFile(filename: String, `object`: T): Boolean {
        val dir = Alchemy.INSTANCE.configDir
        val file = File(dir, filename)
        try {
            FileWriter(file).use { fileWriter ->
                fileWriter.write(Alchemy.INSTANCE.gsonPretty.toJson(`object`))
                fileWriter.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun attemptDefaultFileCopy(classLoader: ClassLoader, fileName: String) {
        val file = Alchemy.INSTANCE.configDir.resolve(fileName)
        if (!file.exists()) {
            file.mkdirs()
            try {
                val stream = classLoader.getResourceAsStream("${assetPackage}/$fileName")
                    ?: throw NullPointerException("File not found $fileName")

                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default file '$fileName': $e")
            }
        }
    }

    private fun attemptDefaultDirectoryCopy(classLoader: ClassLoader, directoryName: String) {
        val directory = Alchemy.INSTANCE.configDir.resolve(directoryName)
        if (!directory.exists()) {
            directory.mkdirs()
            try {
                val sourceUrl = classLoader.getResource("${assetPackage}/$directoryName")
                    ?: throw NullPointerException("Directory not found $directoryName")
                val sourcePath = Paths.get(sourceUrl.toURI())

                Files.walk(sourcePath).use { stream ->
                    stream.forEach { sourceFile ->
                        val destinationFile = directory.resolve(sourcePath.relativize(sourceFile).toString())
                        if (Files.isDirectory(sourceFile)) {
                            // Create subdirectories in the destination
                            destinationFile.mkdirs()
                        } else {
                            // Copy files to the destination
                            Files.copy(sourceFile, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
                        }
                    }
                }
            } catch (e: Exception) {
                Utils.printError("Failed to copy the default directory '$directoryName': " + e.message)
            }
        }
    }
}
