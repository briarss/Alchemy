package aster.amo.alchemy.commands.subcommands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import aster.amo.alchemy.Alchemy
import aster.amo.alchemy.config.ConfigManager
import aster.amo.alchemy.utils.SubCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component

class DebugCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("debug")
            .executes(Companion::debug)
            .build()
    }

    companion object {
        fun debug(ctx: CommandContext<CommandSourceStack>): Int {
            val newMode = !ConfigManager.CONFIG.debug
            ConfigManager.CONFIG.debug = newMode
            ConfigManager.saveFile("config.json", ConfigManager.CONFIG)

            val message = if (newMode) {
                Component.literal("§a[Alchemy] Debug mode ENABLED - Verbose conversion logging active")
            } else {
                Component.literal("§c[Alchemy] Debug mode DISABLED")
            }

            ctx.source.sendSuccess({ message }, true)
            Alchemy.LOGGER.info("Debug mode ${if (newMode) "enabled" else "disabled"}")

            return 1
        }
    }
}
