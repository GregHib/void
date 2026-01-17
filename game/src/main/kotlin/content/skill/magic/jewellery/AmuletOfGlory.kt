package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.chat.ChatType

class AmuletOfGlory : Script {

    init {
        itemOption("Rub", "amulet_of_glory_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Edgeville") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["edgeville_teleport"])
                }
                option("Karamja") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["karamja_teleport"])
                }
                option("Draynor Village") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["draynor_village_teleport"])
                }
                option("Al Kharid") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["al_kharid_teleport"])
                }
                option("Nowhere")
            }
        }

        itemOption("*", "amulet_of_glory_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Edgeville" -> Areas["edgeville_teleport"]
                "Karamja" -> Areas["karamja_teleport"]
                "Draynor Village" -> Areas["draynor_village_teleport"]
                "Al Kharid" -> Areas["al_kharid_teleport"]
                else -> return@itemOption
            }
            message("You rub the amulet...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
