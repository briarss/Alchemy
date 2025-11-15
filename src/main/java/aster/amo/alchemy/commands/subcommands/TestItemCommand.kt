package aster.amo.alchemy.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.tree.LiteralCommandNode
import aster.amo.alchemy.utils.SubCommand
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.CustomData

class TestItemCommand : SubCommand {
    override fun build(): LiteralCommandNode<CommandSourceStack> {
        return Commands.literal("testitem")
            .then(
                Commands.argument("type", StringArgumentType.word())
                    .suggests { _, builder ->
                        listOf(
                            "oraxen_ruby_sword",
                            "oraxen_emerald_pickaxe",
                            "oraxen_sapphire_axe",
                            "armor_ruby_helmet",
                            "armor_emerald_chestplate",
                            "magic_wand",
                            "legacy_stick",
                            "flourish_showdown_item",
                            "legacy_nested_data"
                        ).forEach { builder.suggest(it) }
                        builder.buildFuture()
                    }
                    .executes { ctx ->
                        giveTestItem(ctx, ctx.source.playerOrException)
                    }
                    .then(
                        Commands.argument("player", EntityArgument.player())
                            .executes { ctx ->
                                giveTestItem(ctx, EntityArgument.getPlayer(ctx, "player"))
                            }
                    )
            )
            .build()
    }

    companion object {
        fun giveTestItem(ctx: CommandContext<CommandSourceStack>, player: ServerPlayer): Int {
            val type = StringArgumentType.getString(ctx, "type")

            val itemStack = when (type) {
                "oraxen_ruby_sword" -> createOraxenItem("ruby_sword", Items.DIAMOND_SWORD)
                "oraxen_emerald_pickaxe" -> createOraxenItem("emerald_pickaxe", Items.DIAMOND_PICKAXE)
                "oraxen_sapphire_axe" -> createOraxenItem("sapphire_axe", Items.DIAMOND_AXE)
                "armor_ruby_helmet" -> createArmorItem("ruby_set", Items.DIAMOND_HELMET)
                "armor_emerald_chestplate" -> createArmorItem("emerald_set", Items.DIAMOND_CHESTPLATE)
                "magic_wand" -> createMagicWand()
                "legacy_stick" -> createLegacyStick()
                "flourish_showdown_item" -> createFlourishShowdownItem()
                "legacy_nested_data" -> createLegacyNestedData()
                else -> {
                    ctx.source.sendFailure(Component.literal("§cUnknown test item type: $type"))
                    return 0
                }
            }

            player.addItem(itemStack)
            ctx.source.sendSuccess(
                { Component.literal("§aGave test item '$type' to ${player.name.string}") },
                true
            )

            return 1
        }

        private fun createOraxenItem(oraxenId: String, baseItem: net.minecraft.world.item.Item): ItemStack {
            val stack = ItemStack(baseItem)

            val customData = CompoundTag()
            val bukkitValues = CompoundTag()
            bukkitValues.putString("oraxen:id", oraxenId)
            customData.put("PublicBukkitValues", bukkitValues)

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
            stack.set(
                DataComponents.ITEM_NAME,
                Component.literal("§6Oraxen Item: §e$oraxenId")
            )
            stack.set(
                DataComponents.LORE,
                net.minecraft.world.item.component.ItemLore(
                    listOf(
                        Component.literal("§7This item should convert to:"),
                        Component.literal("§bcustom:$oraxenId"),
                        Component.literal(""),
                        Component.literal("§7NBT: PublicBukkitValues.oraxen:id")
                    )
                )
            )

            return stack
        }

        private fun createArmorItem(armorSet: String, baseItem: net.minecraft.world.item.Item): ItemStack {
            val stack = ItemStack(baseItem)
            val armorType = when (baseItem) {
                Items.DIAMOND_HELMET -> "helmet"
                Items.DIAMOND_CHESTPLATE -> "chestplate"
                Items.DIAMOND_LEGGINGS -> "leggings"
                Items.DIAMOND_BOOTS -> "boots"
                else -> "helmet"
            }

            val customData = CompoundTag()
            val bukkitValues = CompoundTag()
            bukkitValues.putString("armament:armor", "${armorSet}_$armorType")
            customData.put("PublicBukkitValues", bukkitValues)

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
            stack.set(
                DataComponents.ITEM_NAME,
                Component.literal("§6Armament Armor: §e${armorSet}_$armorType")
            )
            stack.set(
                DataComponents.LORE,
                net.minecraft.world.item.component.ItemLore(
                    listOf(
                        Component.literal("§7This item should convert to:"),
                        Component.literal("§bcustom:${armorSet}_$armorType"),
                        Component.literal(""),
                        Component.literal("§7NBT: PublicBukkitValues.armament:armor")
                    )
                )
            )

            return stack
        }

        private fun createMagicWand(): ItemStack {
            val stack = ItemStack(Items.STICK)

            val customData = CompoundTag()
            customData.putString("legacy_item_id", "magic_wand")
            customData.putInt("power_level", 9001)

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
            stack.set(
                DataComponents.ITEM_NAME,
                Component.literal("§5Legacy Magic Wand")
            )
            stack.set(
                DataComponents.LORE,
                net.minecraft.world.item.component.ItemLore(
                    listOf(
                        Component.literal("§7This item should convert to:"),
                        Component.literal("§bcustom:magic_wand"),
                        Component.literal("§7With custom_model_data: 100"),
                        Component.literal("§7Power level should copy"),
                        Component.literal(""),
                        Component.literal("§7NBT: legacy_item_id, power_level")
                    )
                )
            )

            return stack
        }

        private fun createLegacyStick(): ItemStack {
            val stack = ItemStack(Items.STICK)

            stack.set(
                DataComponents.LORE,
                net.minecraft.world.item.component.ItemLore(
                    listOf(
                        Component.literal("§7This is a plain stick that"),
                        Component.literal("§7should get renamed by the"),
                        Component.literal("§7'simple_rename' conversion"),
                        Component.literal(""),
                        Component.literal("§7Item ID: minecraft:stick")
                    )
                )
            )

            return stack
        }

        private fun createFlourishShowdownItem(): ItemStack {
            val stack = ItemStack(Items.NETHER_STAR)

            val customData = CompoundTag()

            // Create the flourish:showdown_item component structure
            val showdownItemData = CompoundTag()
            showdownItemData.putString("showdownItem", "gengarite")

            // Create nested storedPokemon data
            val storedPokemon = CompoundTag()
            storedPokemon.putString("species", "gengar")
            storedPokemon.putInt("level", 50)
            storedPokemon.putString("ability", "levitate")

            val stats = CompoundTag()
            stats.putInt("hp", 261)
            stats.putInt("attack", 166)
            stats.putInt("defense", 156)
            storedPokemon.put("stats", stats)

            val moves = CompoundTag()
            moves.putString("move1", "shadow_ball")
            moves.putString("move2", "sludge_bomb")
            moves.putString("move3", "focus_blast")
            moves.putString("move4", "thunderbolt")
            storedPokemon.put("moves", moves)

            showdownItemData.put("storedPokemon", storedPokemon)

            // Store in custom_data under a component path
            // We'll simulate this as nested data that will be restructured
            customData.put("flourish:showdown_item", showdownItemData)

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
            stack.set(
                DataComponents.ITEM_NAME,
                Component.literal("§5Flourish Showdown Item: §dGengarite")
            )
            stack.set(
                DataComponents.LORE,
                net.minecraft.world.item.component.ItemLore(
                    listOf(
                        Component.literal("§7This item should be restructured:"),
                        Component.literal("§8flourish:showdown_item → gtg:showdownitem"),
                        Component.literal(""),
                        Component.literal("§7Structure changes:"),
                        Component.literal("§8  showdownItem → item_id"),
                        Component.literal("§8  storedPokemon → data.stored_pokemon"),
                        Component.literal(""),
                        Component.literal("§7Contains nested Pokemon NBT")
                    )
                )
            )

            return stack
        }

        private fun createLegacyNestedData(): ItemStack {
            val stack = ItemStack(Items.ENCHANTED_BOOK)

            val customData = CompoundTag()

            val legacyData = CompoundTag()
            legacyData.putString("old_id", "ancient_tome")
            legacyData.putInt("power_level", 100)

            val statistics = CompoundTag()
            statistics.putInt("uses", 50)
            statistics.putInt("max_uses", 200)
            statistics.putString("owner", "TestPlayer")

            legacyData.put("statistics", statistics)
            customData.put("legacy_data", legacyData)

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(customData))
            stack.set(
                DataComponents.ITEM_NAME,
                Component.literal("§6Legacy Nested Data Item")
            )
            stack.set(
                DataComponents.LORE,
                net.minecraft.world.item.component.ItemLore(
                    listOf(
                        Component.literal("§7Tests complex nested restructuring"),
                        Component.literal(""),
                        Component.literal("§7Creates: custom:new_structure"),
                        Component.literal("§8  identifier: from old_id"),
                        Component.literal("§8  metadata.level: from power_level"),
                        Component.literal("§8  metadata.type: 'migrated'"),
                        Component.literal("§8  metadata.stats: from statistics"),
                        Component.literal("§8  version: 2")
                    )
                )
            )

            return stack
        }
    }
}
