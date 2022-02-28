package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character

@Suppress("ArrayInDataClass")
data class Visuals(
    var flag: Int = 0,
    var aspects: MutableMap<Int, Visual> = mutableMapOf(),
    var update: ByteArray? = null,
    var addition: ByteArray? = null
) {

    inline fun <reified T : Visual> getOrPut(mask: Int, put: () -> T): T {
        return aspects.getOrPut(mask, put) as T
    }

    fun flag(mask: Int) {
        flag = flag or mask
    }

    fun flagged(mask: Int): Boolean {
        return flag and mask != 0
    }

    fun reset(character: Character) {
        flag = 0
        aspects.forEach { (_, visual) ->
            visual.resetWhenNeeded(character)
        }
    }

    fun clear() {
        flag = 0
        aspects.clear()
        update = null
        addition = null
    }
}