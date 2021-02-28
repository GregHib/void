package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.entity.character.update.LocalChange
import world.gregs.voidps.engine.entity.character.update.Visuals
import world.gregs.voidps.engine.path.strat.TileTargetStrategy

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
    var interactTarget: TileTargetStrategy

    override fun compareTo(other: Character): Int {
        return index.compareTo(other.index)
    }
}