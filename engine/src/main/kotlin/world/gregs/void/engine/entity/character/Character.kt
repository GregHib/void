package world.gregs.void.engine.entity.character

import world.gregs.void.engine.action.Action
import world.gregs.void.engine.entity.Entity
import world.gregs.void.engine.entity.Size
import world.gregs.void.engine.entity.character.move.Movement
import world.gregs.void.engine.entity.character.update.LocalChange
import world.gregs.void.engine.entity.character.update.Visuals
import world.gregs.void.engine.path.TargetStrategy

/**
 * @author GregHib <greg@gregs.world>
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
    var interactTarget: TargetStrategy

    override fun compareTo(other: Character): Int {
        return index.compareTo(other.index)
    }
}