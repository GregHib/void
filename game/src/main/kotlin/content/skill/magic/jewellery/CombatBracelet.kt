package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
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
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["warriors_guild_teleport"])
                }
                option("Champions' Guild") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["champions_guild_teleport"])
                }
                option("Monastery") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["monastery_teleport"])
                }
                option("Ranging Guild") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["ranging_guild_teleport"])
                }
                option("Nowhere")
            }
        }

        itemOption("*", "combat_bracelet_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Warriors' Guild" -> Areas["warriors_guild_teleport"]
                "Champions' Guild" -> Areas["champions_guild_teleport"]
                "Monastery" -> Areas["monastery_teleport"]
                "Ranging Guild" -> Areas["ranging_guild_teleport"]
                else -> return@itemOption
            }
            message("You rub the bracelet...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
