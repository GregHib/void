package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.MAX_PLAYERS
import world.gregs.voidps.engine.entity.character.CharacterList

class Players : CharacterList<Player>(MAX_PLAYERS) {

    override val indexArray: Array<Player?> = arrayOfNulls(MAX_PLAYERS)

    fun get(name: String): Player? = firstOrNull { it.name == name }

}