package rs.dusk.engine.entity.character.update

import rs.dusk.engine.entity.character.Character

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
interface Visual {
    /**
     * Optional reset to be performed at the end of an update
     */
    fun reset(character: Character) {
    }
}