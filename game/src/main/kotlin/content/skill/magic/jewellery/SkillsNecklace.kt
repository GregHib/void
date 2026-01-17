package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaTypes
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

class SkillsNecklace : Script {

    init {
        itemOption("Rub", "skills_necklace_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Fishing Guild.") {
                    message("You rub the necklace...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["fishing_guild_teleport"])
                }
                option("Mining Guild.") {
                    message("You rub the necklace...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["mining_guild_teleport"])
                }
                option("Crafting Guild.") {
                    message("You rub the necklace...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["crafting_guild_teleport"])
                }
                option("Cooking Guild.") {
                    message("You rub the necklace...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["cooking_guild_teleport"])
                }
                option("Nowhere.")
            }
        }

        itemOption("*", "skills_necklace_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Fishing Guild" -> "fishing_guild_teleport"
                "Mining Guild" -> "mining_guild_teleport"
                "Crafting Guild" -> "crafting_guild_teleport"
                "Cooking Guild" -> "cooking_guild_teleport"
                else -> return@itemOption
            }
            message("You rub the necklace...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, AreaTypes[area])
        }
    }
}
