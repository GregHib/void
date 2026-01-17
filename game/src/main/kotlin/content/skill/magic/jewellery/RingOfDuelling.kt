package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions

class RingOfDuelling : Script {

    init {
        itemOption("Rub", "ring_of_duelling_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Al Kharid Duel Arena.") {
                    jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["duel_arena_teleport"])
                }
                option("Castle Wars Arena.") {
                    jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["castle_wars_teleport"])
                }
                option("Mobilising Armies Command Centre.") {
                    jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["mobilising_armies_teleport"])
                }
                option("Fist of Guthix.") {
                    jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions["fist_of_guthix_teleport"])
                }
                option("Nowhere.")
            }
        }

        itemOption("*", "ring_of_duelling_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Duel Arena" -> "duel_arena_teleport"
                "Castle Wars" -> "castle_wars_teleport"
                "Mobilising Armies" -> "mobilising_armies_teleport"
                "Fist of Guthix" -> "fist_of_guthix_teleport"
                else -> return@itemOption
            }
            jewelleryTeleport(this, it.inventory, it.slot, AreaDefinitions[area])
        }
    }
}
