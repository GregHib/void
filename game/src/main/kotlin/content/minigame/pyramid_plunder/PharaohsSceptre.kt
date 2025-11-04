package content.minigame.pyramid_plunder

import content.entity.player.dialogue.type.choice
import content.skill.magic.jewellery.itemTeleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.inject

class PharaohsSceptre : Script {

    val areas: AreaDefinitions by inject()

    val jalsavrah = areas["jalsavrah_teleport"]
    val jaleustrophos = areas["jaleustrophos_teleport"]
    val jaldraocht = areas["jaldraocht_teleport"]

    init {
        itemOption("Teleport", "pharaohs_sceptre_#", "inventory") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Which Pyramid do you want to teleport to?") {
                option("Jalsavrah") {
                    itemTeleport(this, it.inventory, it.slot, jalsavrah, "pharaohs_sceptre")
                }
                option("Jaleustrophos") {
                    itemTeleport(this, it.inventory, it.slot, jaleustrophos, "pharaohs_sceptre")
                }
                option("Jaldraocht") {
                    itemTeleport(this, it.inventory, it.slot, jaldraocht, "pharaohs_sceptre")
                }
                option("I'm happy where I am actually.")
            }
        }

        itemOption("*", "pharaohs_sceptre_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Jalsavrah" -> jalsavrah
                "Jaleustrophos" -> jaleustrophos
                "Jaldraocht" -> jaldraocht
                else -> return@itemOption
            }
            itemTeleport(this, it.inventory, it.slot, area, "pharaohs_sceptre")
        }
    }
}
