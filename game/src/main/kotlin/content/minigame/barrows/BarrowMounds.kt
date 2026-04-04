package content.minigame.barrows

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele

class BarrowMounds : Script {
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
            val destination = Tables.tile("barrows_brothers.${brother}.crypt")
            delay(2)
            message("You've broken into a crypt!")
            tele(destination)
        }
    }
}