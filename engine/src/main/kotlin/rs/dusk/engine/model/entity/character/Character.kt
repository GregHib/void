package rs.dusk.engine.model.entity.character

import rs.dusk.engine.action.Action
import rs.dusk.engine.model.entity.Entity
import rs.dusk.engine.model.entity.Size
import rs.dusk.engine.model.entity.character.update.Visuals

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 25, 2020
 */
interface Character : Entity, Comparable<Character> {
    val index: Int
    val size: Size
    val visuals: Visuals
    var change: LocalChange?
    val movement: Movement
    val action: Action
    val values: CharacterValues

    override fun compareTo(other: Character): Int {
        return index.compareTo(other.index)
    }
}