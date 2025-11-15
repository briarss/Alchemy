package com.cobblemonodyssey.$mod_id$.storage.database.sql.providers

import com.cobblemonodyssey.$mod_id$.$mod_name$
import com.cobblemonodyssey.$mod_id$.config.StorageOptions
import com.zaxxer.hikari.HikariConfig
import java.io.File

class SQLiteProvider(config: StorageOptions) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:sqlite:%s",
        File($mod_name$.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.sqlite.JDBC"
    override fun getDriverName(): String = "sqlite"
    override fun configure(config: HikariConfig) {}
}
