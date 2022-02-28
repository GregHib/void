package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character

interface Visual {
    fun needsReset(character: Character): Boolean {
        return false
    }

    /**
     * Optional reset to be performed at the end of an update
     */
    fun reset(character: Character) {
    }

    fun resetWhenNeeded(character: Character) {
        if (needsReset(character)) {
            reset(character)
        }
    }
}