package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player
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

        itemOption("Warriors' Guild", "combat_bracelet_#", "worn_equipment", ::teleport)
        itemOption("Champions' Guild", "combat_bracelet_#", "worn_equipment", ::teleport)
        itemOption("Monastery", "combat_bracelet_#", "worn_equipment", ::teleport)
        itemOption("Ranging Guild", "combat_bracelet_#", "worn_equipment", ::teleport)
    }

    private fun teleport(player: Player, option: ItemOption) {
        if (player.contains("delay")) {
            return
        }
        val area = when (option.option) {
            "Warriors' Guild" -> Areas["warriors_guild_teleport"]
            "Champions' Guild" -> Areas["champions_guild_teleport"]
            "Monastery" -> Areas["monastery_teleport"]
            "Ranging Guild" -> Areas["ranging_guild_teleport"]
            else -> return
        }
        player.message("You rub the bracelet...", ChatType.Filter)
        jewelleryTeleport(player, option.inventory, option.slot, area)
    }
}
