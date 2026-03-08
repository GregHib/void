package world.gregs.voidps.engine.entity.item.drop

import world.gregs.voidps.engine.entity.character.player.Player

interface Drop {
    val chance: Int
    val predicate: ((Player) -> Boolean)?
    fun print(indent: Int = 0, multiplier: Double = 1.0): String
}
