package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character

class NPCVisuals(
    var flag: Int = 0,
    override var aspects: MutableMap<Int, Visual> = mutableMapOf()
) : Visuals {

    override fun <T : Visual> getOrPut(mask: Int, put: () -> T): T {
        return aspects.getOrPut(mask, put) as T
    }

    override fun flag(mask: Int) {
        flag = flag or mask
    }

    override fun flagged(mask: Int): Boolean {
        return flag and mask != 0
    }

    override fun reset(character: Character) {
        flag = 0
        aspects.forEach { (_, visual) ->
            visual.resetWhenNeeded(character)
        }
    }
}