package rs.dusk.engine.entity.character

import rs.dusk.engine.action.Action
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.Size
import rs.dusk.engine.entity.character.move.Movement
import rs.dusk.engine.entity.character.update.LocalChange
import rs.dusk.engine.entity.character.update.Visuals

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
    val effects: CharacterEffects

    override fun compareTo(other: Character): Int {
        return index.compareTo(other.index)
    }
}