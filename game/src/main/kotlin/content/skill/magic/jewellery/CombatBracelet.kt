package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaTypes
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

class CombatBracelet : Script {

    init {
        itemOption("Rub", "combat_bracelet_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Warriors' Guild") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["warriors_guild_teleport"])
                }
                option("Champions' Guild") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["champions_guild_teleport"])
                }
                option("Monastery") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["monastery_teleport"])
                }
                option("Ranging Guild") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["ranging_guild_teleport"])
                }
                option("Nowhere")
            }
        }

        itemOption("*", "combat_bracelet_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Warriors' Guild" -> AreaTypes["warriors_guild_teleport"]
                "Champions' Guild" -> AreaTypes["champions_guild_teleport"]
                "Monastery" -> AreaTypes["monastery_teleport"]
                "Ranging Guild" -> AreaTypes["ranging_guild_teleport"]
                else -> return@itemOption
            }
            message("You rub the bracelet...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
