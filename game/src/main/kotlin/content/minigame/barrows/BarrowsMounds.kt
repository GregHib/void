package content.minigame.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC

class BarrowsMounds : Script {
    init {
        itemOption("Dig", "spade") {
            if (tile !in Areas["barrows"]) {
                return@itemOption
            }
            val brother = when (tile) {
                in Areas["dharok_hill"] -> "dharok"
                in Areas["verac_hill"] -> "verac"
                in Areas["ahrim_hill"] -> "ahrim"
                in Areas["guthan_hill"] -> "guthan"
                in Areas["torag_hill"] -> "torag"
                in Areas["karil_hill"] -> "karil"
                else -> return@itemOption
            }
            val destination = Tables.tile("barrows_brothers.$brother.crypt")
            delay(2)
            message("You've broken into a crypt!")
            tele(destination)
        }

        canAttack("dharok_the_wretched,verac_the_defiled,ahrim_the_blighted,guthan_the_infested,torag_the_corrupted,karil_the_tainted") { target ->
            val brother = target.id.substringBefore("_the_")
            if (get<NPC>("${brother}_spawn") != target) {
                message("Someone else is fighting that.")
                false
            } else {
                true
            }
        }
    }
}
