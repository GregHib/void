package world.gregs.voidps.world.activity.quest

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

    fun randomiseOrder(player: Player) {
        val order = (1 .. 5).shuffled()
        player["demon_slayer_aber"] = order[0]
        player["demon_slayer_camerinthum"] = order[1]
        player["demon_slayer_carlem"] = order[2]
        player["demon_slayer_gabindo"] = order[3]
        player["demon_slayer_purchai"] = order[4]
    }

    fun clear(player: Player) {
        player.clear("demon_slayer_aber")
        player.clear("demon_slayer_camerinthum")
        player.clear("demon_slayer_carlem")
        player.clear("demon_slayer_gabindo")
        player.clear("demon_slayer_purchai")
    }
}