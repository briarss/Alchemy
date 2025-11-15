package com.cobblemonodyssey.$mod_id$.storage.database.sql

import com.cobblemonodyssey.$mod_id$.$mod_name$
import com.cobblemonodyssey.$mod_id$.config.StorageOptions
import com.cobblemonodyssey.$mod_id$.data.UserData
import com.cobblemonodyssey.$mod_id$.storage.IStorage
import com.cobblemonodyssey.$mod_id$.storage.StorageType
import com.cobblemonodyssey.$mod_id$.storage.database.sql.providers.MySQLProvider
import com.cobblemonodyssey.$mod_id$.storage.database.sql.providers.SQLiteProvider
import com.cobblemonodyssey.$mod_id$.utils.Utils
import java.sql.SQLException
import java.util.*
import java.util.concurrent.CompletableFuture

class SQLStorage(private val config: StorageOptions) : IStorage {
    private val connectionProvider: ConnectionProvider = when (config.type) {
        StorageType.MYSQL -> MySQLProvider(config)
        StorageType.SQLITE -> SQLiteProvider(config)
        else -> throw IllegalStateException("Invalid storage type!")
    }
//    private val keysType: Type = object : TypeToken<HashMap<String, Int>>() {}.type

    init {
        connectionProvider.init()
    }

    override fun getUser(uuid: UUID): UserData {
        val userData = UserData(uuid)
        println("running getUser for $uuid")
        try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                val result = statement.executeQuery(String.format("SELECT * FROM ${config.tablePrefix}userdata WHERE uuid='%s'", uuid.toString()))
                if (result != null && result.next()) {
//                    userData.example = SkiesCrates.INSTANCE.gson.fromJson(result.getString("example"), exampleType)
                }
            }
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return userData
    }

    override fun saveUser(uuid: UUID, userData: UserData): Boolean {
        println("running saveUser for $uuid")
        return try {
            connectionProvider.createConnection().use {
                val statement = it.createStatement()
                statement.execute(String.format("REPLACE INTO ${config.tablePrefix}userdata (uuid, `example`) VALUES ('%s', '%s')",
                    uuid.toString(),
                    $mod_name$.INSTANCE.gson.toJson(userData.example),
                ))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun getUserAsync(uuid: UUID): CompletableFuture<UserData> {
        return CompletableFuture.supplyAsync({
            try {
                val result = getUser(uuid)
                result
            } catch (e: Exception) {
                UserData(uuid)  // Return default data rather than throwing
            }
        }, $mod_name$.INSTANCE.asyncExecutor)
    }

    override fun saveUserAsync(uuid: UUID, userData: UserData): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync({
            saveUser(uuid, userData)
        }, $mod_name$.INSTANCE.asyncExecutor)
    }

    override fun close() {
        connectionProvider.shutdown()
    }
}
