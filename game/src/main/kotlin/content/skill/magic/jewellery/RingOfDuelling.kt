package content.skill.magic.jewellery

import content.entity.player.dialogue.type.choice
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.ItemOption
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.player.Player

class RingOfDuelling : Script {

    init {
        itemOption("Rub", "ring_of_duelling_#") {
            if (contains("delay")) {
                return@itemOption
            }
            choice("Where would you like to teleport to?") {
                option("Al Kharid Duel Arena.") {
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["duel_arena_teleport"])
                }
                option("Castle Wars Arena.") {
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["castle_wars_teleport"])
                }
                option("Mobilising Armies Command Centre.") {
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["mobilising_armies_teleport"])
                }
                option("Fist of Guthix.") {
                    jewelleryTeleport(this, it.inventory, it.slot, Areas["fist_of_guthix_teleport"])
                }
                option("Nowhere.")
            }
        }

        itemOption("Duel Arena", "ring_of_duelling_#", "worn_equipment", ::teleport)
        itemOption("Castle Wars", "ring_of_duelling_#", "worn_equipment", ::teleport)
        itemOption("Mobilising Armies", "ring_of_duelling_#", "worn_equipment", ::teleport)
        itemOption("Fist of Guthix", "ring_of_duelling_#", "worn_equipment", ::teleport)
    }

    private fun teleport(player: Player, option: ItemOption) {
        if (player.contains("delay")) {
            return
        }
        val area = when (option.option) {
            "Duel Arena" -> "duel_arena_teleport"
            "Castle Wars" -> "castle_wars_teleport"
            "Mobilising Armies" -> "mobilising_armies_teleport"
            "Fist of Guthix" -> "fist_of_guthix_teleport"
            else -> return
        }
        jewelleryTeleport(player, option.inventory, option.slot, Areas[area])
    }
}
