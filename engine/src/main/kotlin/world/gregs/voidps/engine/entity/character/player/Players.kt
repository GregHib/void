package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.CharacterList
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.list.MAX_PLAYERS

class Players : CharacterList<Player>(MAX_PLAYERS) {

    override val indices: Array<Player?> = arrayOfNulls(MAX_PLAYERS)

    fun get(name: String): Player? = firstOrNull { it.name == name }

}