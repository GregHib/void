package world.gregs.voidps.engine.entity.character

import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.network.visual.Visuals

interface Character : InteractiveEntity, Comparable<Character> {
    val index: Int
    val visuals: Visuals
    val action: Action
    val levels: Levels
    var collision: CollisionStrategy
    var mode: Mode

    override fun compareTo(other: Character): Int {
        return index.compareTo(other.index)
    }
}