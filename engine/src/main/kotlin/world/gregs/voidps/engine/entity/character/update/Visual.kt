package world.gregs.voidps.engine.entity.character.update

import world.gregs.voidps.engine.entity.character.Character

/**
 * @author GregHib <greg@gregs.world>
 * @since April 25, 2020
 */
interface Visual {
    /**
     * Optional reset to be performed at the end of an update
     */
    fun reset(character: Character) {
    }
}