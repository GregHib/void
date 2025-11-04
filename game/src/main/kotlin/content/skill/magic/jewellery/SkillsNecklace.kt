package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject

class SkillsNecklace : Script {

    val areas: AreaDefinitions by inject()

    val fishing = areas["fishing_guild_teleport"]
    val mining = areas["mining_guild_teleport"]
    val crafting = areas["crafting_guild_teleport"]
    val cooking = areas["cooking_guild_teleport"]

    init {
        itemOption("Rub", "skills_necklace_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Fishing Guild.") {
                    message("You rub the necklace...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, fishing)
                }
                option("Mining Guild.") {
                    message("You rub the necklace...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, mining)
                }
                option("Crafting Guild.") {
                    message("You rub the necklace...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, crafting)
                }
                option("Cooking Guild.") {
                    message("You rub the necklace...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, cooking)
                }
                option("Nowhere.")
            }
        }

        itemOption("*", "skills_necklace_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Fishing Guild" -> fishing
                "Mining Guild" -> mining
                "Crafting Guild" -> crafting
                "Cooking Guild" -> cooking
                else -> return@itemOption
            }
            message("You rub the necklace...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
