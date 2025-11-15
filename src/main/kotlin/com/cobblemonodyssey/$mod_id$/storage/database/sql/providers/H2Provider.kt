package com.cobblemonodyssey.$mod_id$.storage.database.sql.providers

import com.cobblemonodyssey.$mod_id$.$mod_name$
import com.cobblemonodyssey.$mod_id$.config.StorageOptions
import com.zaxxer.hikari.HikariConfig
import java.io.File

class H2Provider(config: StorageOptions) : HikariCPProvider(config) {
    override fun getConnectionURL(): String = String.format(
        "jdbc:h2:%s;AUTO_SERVER=TRUE",
        File($mod_name$.INSTANCE.configDir, "storage.db").toPath().toAbsolutePath()
    )

    override fun getDriverClassName(): String = "org.h2.Driver"
    override fun getDriverName(): String = "h2"
    override fun configure(config: HikariConfig) {}
}
