package world.gregs.voidps.engine.entity.character

import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.client.variable.Variable
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.move.Steps
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.movementType
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.login.protocol.visual.VisualMask
import world.gregs.voidps.network.login.protocol.visual.Visuals
import world.gregs.voidps.network.login.protocol.visual.update.player.MoveType
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
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

    /**
     * Gradually move the characters appeared location to [delta] over [delay] time
     */
    fun exactMove(delta: Delta, delay: Int = tile.distanceTo(tile.add(delta)) * 30, direction: Direction = Direction.NONE, startDelay: Int = 0) {
        tele(delta)
        if (this is Player) {
            movementType = MoveType.Walk
        }
        val startDelta = delta.invert()
        visuals.exactMovement.apply {
            startX = startDelta.x
            startY = startDelta.y
            this.startDelay = startDelay
            endX = 0
            endY = 0
            endDelay = delay
            this.direction = direction.ordinal
        }
        flagExactMovement()
    }

    /**
     * Gradually move the characters appeared location to [target] over [delay] time
     */
    fun exactMove(target: Tile, delay: Int = tile.distanceTo(target) * 30, direction: Direction = Direction.NONE, startDelay: Int = 0) {
        exactMove(target.delta(tile), delay, direction, startDelay)
    }

    fun say(message: String) {
        visuals.say.text = message
        flagSay()
    }

    /**
     * Apply [id] graphical effect (aka spotanim) to the character with optional [delay]
     * @see GraphicDefinitions for adjusting height, rotation and refresh
     */
    fun gfx(id: String, delay: Int? = null) {
        val definition = get<GraphicDefinitions>().getOrNull(id) ?: return
        val mask = if (this is Player) VisualMask.PLAYER_GRAPHIC_1_MASK else VisualMask.NPC_GRAPHIC_1_MASK
        val graphic = if (visuals.flagged(mask)) visuals.primaryGraphic else visuals.secondaryGraphic
        graphic.id = definition.id
        graphic.delay = delay ?: definition["delay", 0]
        val characterHeight = (this as? NPC)?.def?.get("height", 0) ?: 40
        graphic.height = (characterHeight + definition["height", -1000]).coerceAtLeast(0)
        graphic.rotation = definition["rotation", 0]
        graphic.forceRefresh = definition["force_refresh", false]
        if (visuals.flagged(mask)) {
            flagPrimaryGraphic()
        } else {
            flagSecondaryGraphic()
        }
    }

    fun clearGraphic() {
        visuals.primaryGraphic.reset()
        flagPrimaryGraphic()
        visuals.secondaryGraphic.reset()
        flagSecondaryGraphic()
    }
}

val Entity.size: Int
    get() = when (this) {
        is NPC -> def.size
        is Player -> appearance.size
        else -> 1
    }