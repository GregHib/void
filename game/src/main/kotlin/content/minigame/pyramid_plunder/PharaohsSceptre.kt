package content.minigame.pyramid_plunder

import content.entity.player.dialogue.type.choice
import content.skill.magic.jewellery.itemTeleport
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player

class PharaohsSceptre : Script {

    init {
        itemOption("Teleport", "pharaohs_sceptre_#", "inventory") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Which Pyramid do you want to teleport to?") {
                option("Jalsavrah") {
                    itemTeleport(this, it.inventory, it.slot, Areas["jalsavrah_teleport"], "pharaohs_sceptre")
                }
                option("Jaleustrophos") {
                    itemTeleport(this, it.inventory, it.slot, Areas["jaleustrophos_teleport"], "pharaohs_sceptre")
                }
                option("Jaldraocht") {
                    itemTeleport(this, it.inventory, it.slot, Areas["jaldraocht_teleport"], "pharaohs_sceptre")
                }
                option("I'm happy where I am actually.")
            }
        }

        itemOption("Jalsavrah", "pharaohs_sceptre_#", "worn_equipment", ::teleport)
        itemOption("Jaleustrophos", "pharaohs_sceptre_#", "worn_equipment", ::teleport)
        itemOption("Jaldraocht", "pharaohs_sceptre_#", "worn_equipment", ::teleport)
    }

    private fun teleport(player: Player, option: ItemOption) {
        if (player.contains("delay")) {
            return
        }
        val area = when (option.option) {
            "Jalsavrah" -> Areas["jalsavrah_teleport"]
            "Jaleustrophos" -> Areas["jaleustrophos_teleport"]
            "Jaldraocht" -> Areas["jaldraocht_teleport"]
            else -> return
        }
        itemTeleport(player, option.inventory, option.slot, area, "pharaohs_sceptre")
    }
}
