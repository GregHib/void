package world.gregs.voidps.engine.entity.character

import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.visual.Visuals

interface Character : Entity, Comparable<Character> {
    val index: Int
    val visuals: Visuals
    val levels: Levels
    var collision: CollisionStrategy
    var mode: Mode
    var queue: ActionQueue
    var softTimers: Timers
    var suspension: Suspension?
    var variables: Variables
    var interaction: SuspendableEvent?

    override fun compareTo(other: Character): Int {
        return index.compareTo(other.index)
    }
}