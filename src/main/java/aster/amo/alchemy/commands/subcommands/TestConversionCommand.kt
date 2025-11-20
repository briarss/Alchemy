package aster.amo.alchemy.commands.subcommands

import aster.amo.alchemy.Alchemy
import aster.amo.alchemy.utils.SubCommand
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import com.mojang.serialization.Dynamic
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.nbt.NbtOps
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack

class TestConversionCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("test-conversion")
            .requires { it.hasPermission(2) }
            .executes(Companion::testConversion)
            .build()
    }

    companion object {
        fun testConversion(ctx: CommandContext<CommandSourceStack>): Int {
            val player = ctx.source.playerOrException
            val heldItem = player.mainHandItem

            if (heldItem.isEmpty) {
                ctx.source.sendFailure(Component.literal("§c[Alchemy] Hold an item in your main hand to test conversion"))
                return 0
            }

            // Serialize the item to NBT
            val nbtOps = Alchemy.INSTANCE.nbtOpts
            val encoded = ItemStack.CODEC.encodeStart(nbtOps, heldItem)

            if (encoded.error().isPresent) {
                ctx.source.sendFailure(Component.literal("§c[Alchemy] Failed to serialize item: ${encoded.error().get().message()}"))
                return 0
            }

            val nbt = encoded.result().get()

            ctx.source.sendSuccess(
                { Component.literal("§e[Alchemy] Item before conversion: ${heldItem.item.description.string}") },
                false
            )

            // Deserialize the item (this will trigger conversion)
            val decoded = ItemStack.CODEC.parse(Dynamic(nbtOps, nbt))

            if (decoded.error().isPresent) {
                ctx.source.sendFailure(Component.literal("§c[Alchemy] Failed to deserialize item: ${decoded.error().get().message()}"))
                return 0
            }

            val convertedItem = decoded.result().get()

            // Replace the held item with the converted one
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, convertedItem)

            ctx.source.sendSuccess(
                { Component.literal("§a[Alchemy] Item after conversion: ${convertedItem.item.description.string}") },
                false
            )

            return 1
        }
    }
}
