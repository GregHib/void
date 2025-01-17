package world.gregs.voidps.engine.entity.character.npc

import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.Steps
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.timer.TimerSlot
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.type.Tile
import kotlin.coroutines.Continuation

/**
 * A non-player character
 */
data class NPC(
    val id: String = "",
    override var tile: Tile = Tile.EMPTY,
    override val levels: Levels = Levels()
) : Character {

    override var mode: Mode = EmptyMode
        set(value) {
            field.stop(value)
            field = value
            value.start()
        }
    lateinit var def: NPCDefinition
    override var queue = ActionQueue(this)
    override var softTimers: Timers = TimerSlot(this)
    override var delay: Continuation<Unit>? = null
    override var suspension: Suspension? = null
    override var variables: Variables = Variables(this)
    override val steps: Steps = Steps(this)

    override lateinit var collision: CollisionStrategy
    override lateinit var visuals: NPCVisuals

    constructor(id: String = "", tile: Tile = Tile.EMPTY, index: Int) : this(id, tile) {
        this.index = index
    }

    override var index: Int = -1
        set(value) {
            field = value
            visuals = NPCVisuals(value)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NPC
        return index == other.index
    }

    override fun hashCode(): Int {
        return index
    }

    override fun toString(): String {
        return "NPC(id=$id, index=$index, tile=$tile)"
    }
}