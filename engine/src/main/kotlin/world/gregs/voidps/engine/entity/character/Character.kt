package world.gregs.voidps.engine.entity.character

import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.Steps
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.login.protocol.visual.Visuals
import kotlin.coroutines.Continuation

interface Character : Entity, Variable, EventDispatcher, Comparable<Character> {
    val index: Int
    val visuals: Visuals
    val levels: Levels
    var collision: CollisionStrategy
    var mode: Mode
    var queue: ActionQueue
    var softTimers: Timers
    var suspension: Suspension?
    var delay: Continuation<Unit>?
    override var variables: Variables
    val steps: Steps

    override fun compareTo(other: Character): Int {
        return index.compareTo(other.index)
    }

    fun say(message: String) {
        visuals.say.text = message
        flagSay()
    }

}

val Entity.size: Int
    get() = when (this) {
        is NPC -> def.size
        is Player -> appearance.size
        else -> 1
    }