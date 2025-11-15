package com.cobblemonodyssey.$mod_id$.utils

import com.cobblemonodyssey.$mod_id$.$mod_name$
import net.minecraft.network.chat.Component

object TextUtils {
    fun toNative(text: String): Component {
        return $mod_name$.INSTANCE.adventure.toNative($mod_name$.MINI_MESSAGE.deserialize(text))
    }

    fun toComponent(text: String): net.kyori.adventure.text.Component {
        return $mod_name$.MINI_MESSAGE.deserialize(text)
    }
}
