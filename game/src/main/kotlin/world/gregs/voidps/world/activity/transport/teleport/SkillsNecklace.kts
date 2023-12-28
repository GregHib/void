package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val areas: AreaDefinitions by inject()

val fishing = areas["fishing_guild_teleport"]
val mining = areas["mining_guild_teleport"]
val crafting = areas["crafting_guild_teleport"]
val cooking = areas["cooking_guild_teleport"]

on<InventoryOption>({ inventory == "inventory" && item.id.startsWith("skills_necklace_") && option == "Rub" }) { player: Player ->
    choice("Where would you like to teleport to?") {
        option("Fishing Guild.") {
            player.message("You rub the necklace...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, fishing)
        }
        option("Mining Guild.") {
            player.message("You rub the necklace...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, mining)
        }
        option("Crafting Guild.") {
            player.message("You rub the necklace...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, crafting)
        }
        option("Cooking Guild.") {
            player.message("You rub the necklace...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, cooking)
        }
        option("Nowhere.")
    }
}

on<InventoryOption>({ inventory == "worn_equipment" && item.id.startsWith("skills_necklace_") }) { player: Player ->
    val area = when (option) {
        "Fishing Guild" -> fishing
        "Mining Guild" -> mining
        "Crafting Guild" -> crafting
        "Cooking Guild" -> cooking
        else -> return@on
    }
    player.message("You rub the necklace...", ChatType.Filter)
    jewelleryTeleport(player, inventory, slot, area)
}