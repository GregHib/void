package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

class CombatBracelet(val areas: AreaDefinitions) : Script {

    val warriorsGuild = areas["warriors_guild_teleport"]
    val championsGuild = areas["champions_guild_teleport"]
    val monastery = areas["monastery_teleport"]
    val rangingGuild = areas["ranging_guild_teleport"]

    init {
        itemOption("Rub", "combat_bracelet_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Warriors' Guild") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, warriorsGuild)
                }
                option("Champions' Guild") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, championsGuild)
                }
                option("Monastery") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, monastery)
                }
                option("Ranging Guild") {
                    message("You rub the bracelet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, rangingGuild)
                }
                option("Nowhere")
            }
        }

        itemOption("*", "combat_bracelet_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Warriors' Guild" -> warriorsGuild
                "Champions' Guild" -> championsGuild
                "Monastery" -> monastery
                "Ranging Guild" -> rangingGuild
                else -> return@itemOption
            }
            message("You rub the bracelet...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
