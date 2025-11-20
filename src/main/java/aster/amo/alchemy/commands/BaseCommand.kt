package aster.amo.alchemy.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.tree.LiteralCommandNode
import aster.amo.alchemy.Alchemy
import aster.amo.alchemy.commands.subcommands.DebugCommand
import aster.amo.alchemy.commands.subcommands.ReloadCommand
import aster.amo.alchemy.commands.subcommands.TestItemCommand
import aster.amo.alchemy.commands.subcommands.TestConversionCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

class BaseCommand {
    private val aliases = listOf("alchemy")

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        val rootCommands: List<LiteralCommandNode<CommandSourceStack>> = aliases.map {
            Commands.literal(it)
                .requires { source -> source.hasPermission(2) }
                .build()
        }

        val subCommands: List<LiteralCommandNode<CommandSourceStack>> = listOf(
            ReloadCommand().build(),
            DebugCommand().build(),
            TestItemCommand().build(),
            TestConversionCommand().build(),
        )

        rootCommands.forEach { root ->
            subCommands.forEach { sub -> root.addChild(sub) }
            dispatcher.root.addChild(root)
        }
    }
}
