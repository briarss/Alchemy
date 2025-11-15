package com.cobblemonodyssey.$mod_id$.storage

import com.cobblemonodyssey.$mod_id$.storage.StorageType
import com.cobblemonodyssey.$mod_id$.config.StorageOptions
import com.cobblemonodyssey.$mod_id$.data.UserData
import com.cobblemonodyssey.$mod_id$.storage.database.MongoStorage
import com.cobblemonodyssey.$mod_id$.storage.database.sql.SQLStorage
import com.cobblemonodyssey.$mod_id$.storage.file.FileStorage
import java.util.*
import java.util.concurrent.CompletableFuture

interface IStorage {
    companion object {
        fun load(config: StorageOptions): IStorage {
            return when (config.type) {
                StorageType.JSON -> FileStorage()
                StorageType.MONGO -> MongoStorage(config)
                StorageType.MYSQL, StorageType.SQLITE -> SQLStorage(config)
            }
        }
    }

    fun getUser(uuid: UUID): UserData
    fun saveUser(uuid: UUID, userData: UserData): Boolean

    fun getUserAsync(uuid: UUID): CompletableFuture<UserData>
    fun saveUserAsync(uuid: UUID, userData: UserData): CompletableFuture<Boolean>

    fun close() {}
}
