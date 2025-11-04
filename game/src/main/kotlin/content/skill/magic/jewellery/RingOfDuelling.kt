package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.inject

class RingOfDuelling : Script {

    val areas: AreaDefinitions by inject()

    val duelArena = areas["duel_arena_teleport"]
    val castleWars = areas["castle_wars_teleport"]
    val mobilisingArmies = areas["mobilising_armies_teleport"]
    val fistOfGuthix = areas["fist_of_guthix_teleport"]

    init {
        itemOption("Rub", "ring_of_duelling_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Al Kharid Duel Arena.") {
                    jewelleryTeleport(this, it.inventory, it.slot, duelArena)
                }
                option("Castle Wars Arena.") {
                    jewelleryTeleport(this, it.inventory, it.slot, castleWars)
                }
                option("Mobilising Armies Command Centre.") {
                    jewelleryTeleport(this, it.inventory, it.slot, mobilisingArmies)
                }
                option("Fist of Guthix.") {
                    jewelleryTeleport(this, it.inventory, it.slot, fistOfGuthix)
                }
                option("Nowhere.")
            }
        }

        itemOption("*", "ring_of_duelling_#", "worn_equipment") {
            if (contains("delay")) {
                return@itemOption
            }
            val area = when (it.option) {
                "Duel Arena" -> duelArena
                "Castle Wars" -> castleWars
                "Mobilising Armies" -> mobilisingArmies
                "Fist of Guthix" -> fistOfGuthix
                else -> return@itemOption
            }
            jewelleryTeleport(this, it.inventory, it.slot, area)
        }
    }
}
