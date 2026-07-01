package world.gregs.voidps.engine.entity.character.npc

import org.rsmod.game.pathfinder.collision.CollisionStrategy
import org.rsmod.game.pathfinder.flag.CollisionFlag
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.Steps
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.timer.TimerSlot
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.login.protocol.visual.NPCVisuals
import world.gregs.voidps.type.Tile

/**
 * A non-player character
 */
data class NPC(
    val id: String = "",
    override var tile: Tile = Tile.EMPTY,
    val def: NPCDefinition = NPCDefinition.EMPTY,
    override var index: Int = -1,
    override val levels: Levels = Levels(),
) : Character {
    override val visuals: NPCVisuals = NPCVisuals()

    var hide = false
    override val blockMove: Int
        get() {
            if (!transformDef["solid", true]) {
                return 0
            }
            // Owned followers (familiars/pets) phase through players - including their owner - so a
            // player standing between them and their target can't block them. They still collide
            // with other npcs (BLOCK_NPCS) and route around them.
            return if (this["owner_index", -1] != -1) {
                CollisionFlag.BLOCK_NPCS
            } else {
                CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS
            }
        }
    override val collisionFlag: Int
        get() = CollisionFlag.BLOCK_NPCS or if (transformDef["solid", false]) CollisionFlag.FLOOR else 0

    val transformId: String
        get() = this["transform_id", id]

    val transformDef: NPCDefinition
        get() {
            if (contains("transform_id")) {
                return NPCDefinitions.get(get("transform_id", id))
            }
            return def
        }

    init {
        if (index != -1) {
            visuals.hits.self = -index
        }
    }

    var lifecycle: Int = 0

    override val size = def.size
    override var mode: Mode = EmptyMode
        set(value) {
            field.stop(value)
            field = value
            value.start()
        }

    override var queue: ActionQueue<*> = ActionQueue(this)
    override var softTimers: Timers = TimerSlot(this)
    override var suspension: Suspension? = null
    override var variables: Variables = Variables(this)
    override val steps: Steps = Steps(this)
    override var walkTrigger: (() -> Unit)? = null

    override lateinit var collision: CollisionStrategy

    var regenCounter = 0
    var huntMode: String? = null
    var huntCounter = 0

    fun def(player: Player): NPCDefinition {
        if (contains("transform_id")) {
            return NPCDefinitions.get(this["transform_id", ""])
        }
        return NPCDefinitions.resolve(def, player)
    }

    /**
     * Respawn an npc after [ticks]
     */
    fun respawn(ticks: Int) {
        hide = true
        lifecycle = ticks + 1
    }

    /**
     * Revert the transform of an npc after [ticks]
     */
    fun revert(ticks: Int) {
        lifecycle = ticks + 1
    }

    /**
     * Remove then npc completely after [ticks]
     */
    fun despawn(ticks: Int = 0) {
        lifecycle = -(ticks + 1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as NPC
        return index == other.index
    }

    override fun hashCode(): Int = index

    override fun toString(): String = "NPC(id=$id, index=$index, tile=$tile)"
}
