package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character

interface Visual {
    open fun needsReset(character: Character): Boolean {
        return false
    }

    /**
     * Optional reset to be performed at the end of an update
     */
    fun reset(character: Character) {
    }
}