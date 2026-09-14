package content.area.tirannwn.lletya

import content.entity.player.dialogue.type.choice
import content.skill.magic.jewellery.itemTeleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas

class TeleportCrystal : Script {

    init {
        itemOption("Activate", "crystal_teleport_seed_#") {
            if (contains("delay")) {
                return@itemOption
            }
            val item = it.item.id
            choice("Select an Option") {
                option("Teleport to Lletya.") {
                    if (itemTeleport(this, it.inventory, it.slot, Areas["lletya_teleport"], "jewellery")) {
                        if (item == "crystal_teleport_seed_1") {
                            message("Your teleportation crystal has degraded to a tiny elf crystal.")
                            message("Eluned can re-enchant it.")
                        } else {
                            message("Your teleportation crystal has degraded from use.")
                        }
                    }
                }
                option("Cancel.")
            }
        }
    }
}
