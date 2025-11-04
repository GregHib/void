package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inject

class AmuletOfGlory : Script {

    val areas: AreaDefinitions by inject()

    val edgeville = areas["edgeville_teleport"]
    val karamja = areas["karamja_teleport"]
    val draynorVillage = areas["draynor_village_teleport"]
    val alKharid = areas["al_kharid_teleport"]

    init {
        itemOption("Rub", "amulet_of_glory_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Edgeville") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, edgeville)
                }
                option("Karamja") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, karamja)
                }
                option("Draynor Village") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, draynorVillage)
                }
                option("Al Kharid") {
                    message("You rub the amulet...", ChatType.Filter)
                    jewelleryTeleport(this, it.inventory, it.slot, alKharid)
                }
                option("Nowhere")
            }
        }

        itemOption("*", "amulet_of_glory_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Edgeville" -> edgeville
                "Karamja" -> karamja
                "Draynor Village" -> draynorVillage
                "Al Kharid" -> alKharid
                else -> return@itemOption
            }
            message("You rub the amulet...", ChatType.Filter)
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
