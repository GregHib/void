package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character

interface Visuals {
    fun <T : Visual> getOrPut(mask: Int, put: () -> T): T
    val aspects: MutableMap<Int, Visual>
    fun flag(mask: Int)
    fun flagged(mask: Int): Boolean
    fun reset(character: Character)
}