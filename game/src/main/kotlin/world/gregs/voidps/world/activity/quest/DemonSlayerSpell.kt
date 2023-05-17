package world.gregs.voidps.world.activity.quest

import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.entity.character.player.Player

object DemonSlayerSpell {
    fun getWord(player: Player, index: Int): String = when (index) {
        player["demon_slayer_aber", -1] -> "Aber"
        player["demon_slayer_camerinthum", -1] -> "Camerinthum"
        player["demon_slayer_carlem", -1] -> "Carlem"
        player["demon_slayer_gabindo", -1] -> "Gabindo"
        player["demon_slayer_purchai", -1] -> "Purchai"
        else -> "null"
    }
}