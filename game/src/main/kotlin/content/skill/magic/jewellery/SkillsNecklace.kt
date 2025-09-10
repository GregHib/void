package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import content.entity.player.inv.inventoryItem
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class SkillsNecklace {

    val areas: AreaDefinitions by inject()

    val fishing = areas["fishing_guild_teleport"]
    val mining = areas["mining_guild_teleport"]
    val crafting = areas["crafting_guild_teleport"]
    val cooking = areas["cooking_guild_teleport"]

    init {
        inventoryItem("Rub", "skills_necklace_#", "inventory") {
            if (player.contains("delay")) {
                return@inventoryItem
            }
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

        inventoryItem("*", "skills_necklace_#", "worn_equipment") {
            if (player.contains("delay")) {
                return@inventoryItem
            }
            val area = when (option) {
                "Fishing Guild" -> fishing
                "Mining Guild" -> mining
                "Crafting Guild" -> crafting
                "Cooking Guild" -> cooking
                else -> return@inventoryItem
            }
            player.message("You rub the necklace...", ChatType.Filter)
            jewelleryTeleport(player, inventory, slot, area)
        }
    }
}
