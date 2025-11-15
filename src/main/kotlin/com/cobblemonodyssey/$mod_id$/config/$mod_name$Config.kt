package com.cobblemonodyssey.$mod_id$.config

class $mod_name$Config(
    var debug: Boolean = false,
    var storage: StorageOptions = StorageOptions()
) {
    override fun toString(): String {
        return "$mod_name$Config(debug=$debug, storage=$storage)"
    }
}
