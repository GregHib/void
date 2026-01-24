package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player

class RingOfSlaying : Script {

    init {
        itemOption("Rub", "ring_of_slaying_#") {
            if (contains("delay")) {
                return@itemOption
            }
            menu(it.inventory, it.slot)
        }

        itemOption("Rub", "ring_of_slaying_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            menu(it.inventory, it.slot)
        }
    }

    private suspend fun Player.menu(inventory: String, slot: Int) {
        choice("Where would you like to teleport to?") {
            option("Sumona in Pollnivneach.") {
                jewelleryTeleport(this, inventory, slot, Areas["pollnivneach_slayer_teleport"])
            }
            option("Morytania Slayer Tower.") {
                jewelleryTeleport(this, inventory, slot, Areas["slayer_tower_teleport"])
            }
            option("Rellekka Slayer Caves.") {
                jewelleryTeleport(this, inventory, slot, Areas["rellekka_slayer_cave_teleport"])
            }
            option("Tarn's Lair.") {
                jewelleryTeleport(this, inventory, slot, Areas["tarns_lair_teleport"])
            }
            option("Nowhere. Give me a Slayer update.") {
                // TODO
            }
        }
    }
}
