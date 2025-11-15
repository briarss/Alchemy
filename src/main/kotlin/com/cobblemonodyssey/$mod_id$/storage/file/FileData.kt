package com.cobblemonodyssey.$mod_id$.storage.file

import com.cobblemonodyssey.$mod_id$.data.UserData
import java.util.*

class FileData {
    var userdata: HashMap<UUID, UserData> = HashMap()
    override fun toString(): String {
        return "FileData(userdata=$userdata)"
    }
}
