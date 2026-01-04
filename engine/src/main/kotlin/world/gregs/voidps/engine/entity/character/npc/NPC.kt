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
import world.gregs.voidps.engine.get
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
    val def: NPCDefinition = NPCDefinition.EMPTY,
    override var index: Int = -1,
    override val levels: Levels = Levels(),
) : Character {
    override val visuals: NPCVisuals = NPCVisuals()
    val targets: MutableList<Any> = mutableListOf()

    var hide = false
    override var blockMove: Int = if (def["solid", true]) CollisionFlag.BLOCK_PLAYERS or CollisionFlag.BLOCK_NPCS else 0
    override var collisionFlag: Int = CollisionFlag.BLOCK_NPCS or if (def["solid", false]) CollisionFlag.FLOOR else 0

    init {
        if (index != -1) {
            visuals.hits.self = -index
        }
    }

    override val size = def.size
    override var mode: Mode = EmptyMode
        set(value) {
            field.stop(value)
            field = value
            value.start()
        }

    override var queue = ActionQueue(this)
    override var softTimers: Timers = TimerSlot(this)
    override var delay: Continuation<Unit>? = null
    override var suspension: Suspension? = null
    override var variables: Variables = Variables(this)
    override val steps: Steps = Steps(this)

    override lateinit var collision: CollisionStrategy

    var regenCounter = 0
    var huntMode: String? = null
    var huntCounter = 0

    fun def(player: Player, definitions: NPCDefinitions = get()): NPCDefinition {
        if (contains("transform_id")) {
            return definitions.get(this["transform_id", ""])
        }
        return definitions.resolve(def, player)
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
