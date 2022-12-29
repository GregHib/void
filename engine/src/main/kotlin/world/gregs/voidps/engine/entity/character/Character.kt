package world.gregs.voidps.engine.entity.character

import org.rsmod.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.entity.character.move.Movement
import world.gregs.voidps.engine.path.strat.TileTargetStrategy
import world.gregs.voidps.network.visual.Visuals

interface Character : InteractiveEntity, Comparable<Character> {
    val index: Int
    val visuals: Visuals
    val movement: Movement
    val action: Action
    val levels: Levels
    var followTarget: TileTargetStrategy
    var collision: CollisionStrategy
    val interact: Interaction

    override fun compareTo(other: Character): Int {
        return index.compareTo(other.index)
    }
}