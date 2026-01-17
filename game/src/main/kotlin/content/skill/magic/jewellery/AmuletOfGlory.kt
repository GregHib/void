package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaTypes
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
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["edgeville_teleport"])
                }
                option("Karamja") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["karamja_teleport"])
                }
                option("Draynor Village") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["draynor_village_teleport"])
                }
                option("Al Kharid") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, AreaTypes["al_kharid_teleport"])
                }
                option("Nowhere")
            }
        }

        itemOption("*", "amulet_of_glory_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Edgeville" -> AreaTypes["edgeville_teleport"]
                "Karamja" -> AreaTypes["karamja_teleport"]
                "Draynor Village" -> AreaTypes["draynor_village_teleport"]
                "Al Kharid" -> AreaTypes["al_kharid_teleport"]
                else -> return@itemOption
            }
            message("You rub the amulet...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
