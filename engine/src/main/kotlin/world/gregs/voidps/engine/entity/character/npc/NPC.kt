package world.gregs.voidps.engine.entity.character.npc

import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.entity.Size
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.Steps
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.timer.TimerSlot
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.visual.NPCVisuals

/**
 * A non-player character
 */
data class NPC(
    val id: String = "",
    override var tile: Tile = Tile.EMPTY,
    override val size: Size = Size.ONE,
    override val levels: Levels = Levels()
) : Character {

    override var mode: Mode = EmptyMode
        set(value) {
            field.stop()
            field = value
            value.start()
        }
    override val events: Events = Events(this)
    lateinit var def: NPCDefinition
    override var queue = ActionQueue(this)
    override var softTimers: Timers = TimerSlot(events)
    override var suspension: Suspension? = null
        set(value) {
            field?.cancel()
            field = value
        }
    override var variables: Variables = Variables(events)
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