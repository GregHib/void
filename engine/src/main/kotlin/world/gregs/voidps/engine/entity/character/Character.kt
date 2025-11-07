package world.gregs.voidps.engine.entity.character

import kotlinx.coroutines.suspendCancellableCoroutine
import org.rsmod.game.pathfinder.collision.CollisionStrategy
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.variable.VariableStore
import world.gregs.voidps.engine.client.variable.Variables
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Mode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.Steps
import world.gregs.voidps.engine.entity.character.mode.move.target.TileTargetStrategy
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.appearance
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.character.player.temporaryMoveType
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.queue.ActionQueue
import world.gregs.voidps.engine.suspend.Suspension
import world.gregs.voidps.engine.timer.Timers
import world.gregs.voidps.network.login.protocol.visual.VisualMask
import world.gregs.voidps.network.login.protocol.visual.Visuals
import world.gregs.voidps.network.login.protocol.visual.update.Face
import world.gregs.voidps.network.login.protocol.visual.update.player.MoveType
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile
import kotlin.coroutines.Continuation
import kotlin.math.round

interface Character :
    Entity,
    VariableStore,
    Comparable<Character> {
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
    val size: Int
    val blockMove: Int
    val collisionFlag: Int

    override fun compareTo(other: Character): Int = index.compareTo(other.index)

    /**
     * Gradually move the characters appeared location to [delta] over [delay] time
     */
    fun exactMove(delta: Delta, delay: Int = tile.distanceTo(tile.add(delta)) * 30, direction: Direction = Direction.NONE, startDelay: Int = 0) {
        if (delta == Delta.EMPTY) {
            return
        }
        tele(delta)
        if (this is Player) {
            temporaryMoveType = MoveType.Walk
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

    /**
     * Force a message to be displayed above character
     */
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

    /**
     * Remove any graphical effects in progress
     */
    fun clearGfx() {
        visuals.primaryGraphic.reset()
        flagPrimaryGraphic()
        visuals.secondaryGraphic.reset()
        flagSecondaryGraphic()
    }

    /**
     * Temporarily perform animation [id] (aka sequence)
     * with optional [delay] and [override]ing of the previous animation
     */
    fun anim(id: String, delay: Int? = null, override: Boolean = false): Int {
        val definition = get<AnimationDefinitions>().getOrNull(id) ?: return -1
        val anim = visuals.animation
        if (!override && definition.priority < anim.priority) {
            return -1
        }
        val stand = definition["stand", true]
        if (stand) {
            anim.stand = definition.id
        }
        val force = definition["force", true]
        if (force) {
            anim.force = definition.id
        }
        val walk = definition["walk", true]
        if (walk) {
            anim.walk = definition.id
        }
        val run = definition["run", true]
        if (run) {
            anim.run = definition.id
        }
        anim.infinite = definition["infinite", false]
        if (stand || force || walk || run) {
            anim.delay = delay ?: definition["delay", 0]
            anim.priority = definition.priority
        }
        flagAnimation()
        return definition["ticks", 0]
    }

    /**
     * Clear the characters current animation
     */
    fun clearAnim() {
        visuals.animation.reset()
        flagAnimation()
    }

    /**
     * Walks player to [target]
     * Specify [noCollision] to walk through [GameObject]s and
     * [forceWalk] to force walking even if the player has running active
     */
    fun walkTo(target: Tile, noCollision: Boolean = false, forceWalk: Boolean = false) {
        if (tile == target) {
            return
        }
        mode = Movement(this, TileTargetStrategy(target, noCollision, forceWalk))
    }

    /**
     * The direction the character is currently facing
     */
    val direction: Direction
        get() = Direction.of(visuals.face.targetX - tile.x, visuals.face.targetY - tile.y)

    /**
     * Turn to face a [direction]
     */
    fun face(direction: Direction, update: Boolean = true) = face(direction.delta, update)

    /**
     * Turn to face a [tile]
     */
    fun face(tile: Tile, update: Boolean = true) = face(tile.delta(this.tile), update)

    /**
     * Turn to face [delta]
     */
    fun face(delta: Delta, update: Boolean = true): Boolean {
        if (delta == Delta.EMPTY) {
            clearFace()
            return false
        }
        val turn = visuals.face
        turn.targetX = tile.x + delta.x
        turn.targetY = tile.y + delta.y
        turn.direction = Face.getFaceDirection(delta.x, delta.y)
        if (update) {
            flagTurn()
        }
        return true
    }

    /**
     * Turn to face [entity]
     */
    fun face(entity: Entity, update: Boolean = true) {
        val tile = nearestTile(entity)
        if (!face(tile, update) && entity is GameObject) {
            when {
                ObjectShape.isWall(entity.shape) -> face(Direction.cardinal[(entity.rotation + 3) and 0x3], update)
                ObjectShape.isCorner(entity.shape) -> face(Direction.ordinal[entity.rotation], update)
                else -> {
                    val delta = tile.add(entity.width, entity.height).delta(entity.tile.add(entity.width, entity.height))
                    face(delta, update)
                }
            }
        }
    }

    /**
     * Reset character face direction
     */
    fun clearFace(): Boolean {
        visuals.face.reset()
        return true
    }

    private fun nearestTile(entity: Entity): Tile = when (entity) {
        is GameObject -> Distance.getNearest(entity.tile, entity.width, entity.height, this.tile)
        is NPC -> Distance.getNearest(entity.tile, entity.def.size, entity.def.size, this.tile)
        is Player -> Distance.getNearest(entity.tile, entity.appearance.size, entity.appearance.size, this.tile)
        else -> entity.tile
    }

    /**
     * Track facing a [character] until otherwise specified
     */
    fun watch(character: Character) {
        if (character is Player) {
            visuals.watch.index = character.index or 0x8000
        } else {
            visuals.watch.index = character.index
        }
        visuals.face.clear()
        flagWatch()
    }

    /**
     * Check if character is currently watching [character]
     */
    fun watching(character: Character): Boolean = if (character is Player) {
        visuals.watch.index == character.index or 0x8000
    } else {
        visuals.watch.index == character.index
    }

    /**
     * Stop watching the targeted entity
     */
    fun clearWatch() {
        visuals.watch.index = -1
        flagWatch()
    }


    /**
     * Prevents non-interface player input and most processing
     * Cannot be cancelled.
     */
    suspend fun delay(ticks: Int = 1) {
        if (ticks <= 0) {
            return
        }
        this["delay"] = ticks
        suspendCancellableCoroutine {
            delay = it
        }
    }

    /**
     * Delay until the appeared location of the character has moved [delta] in [delay] time
     */
    suspend fun exactMoveDelay(delta: Delta, delay: Int = tile.distanceTo(tile.add(delta)) * 30, direction: Direction = Direction.NONE) {
        exactMove(delta, delay, direction)
        delay(round(delay / 30.0).toInt())
    }

    /**
     * Delay until the appeared location of the character has moved to [target] in [delay] time
     */
    suspend fun exactMoveDelay(target: Tile, delay: Int = tile.distanceTo(target) * 30, direction: Direction = Direction.NONE, startDelay: Int = 0) {
        exactMove(target, delay, direction, startDelay)
        delay(round(delay / 30.0).toInt())
    }

    /**
     * Delay until characters animation [id] is complete
     * @param override the current animation
     */
    suspend fun animDelay(id: String, override: Boolean = false) {
        val ticks = anim(id, override = override)
        delay(ticks)
    }

    /**
     * Forces the character to walk to a tile
     */
    suspend fun walkToDelay(tile: Tile, forceWalk: Boolean = false) {
        walkTo(tile, noCollision = false, forceWalk = forceWalk)
        delayTarget(tile)
    }

    /**
     * Force a character to walk to tile ignoring collisions
     */
    suspend fun walkOverDelay(tile: Tile, forceWalk: Boolean = true) {
        walkTo(tile, noCollision = true, forceWalk = forceWalk)
        delayTarget(tile)
    }

    private suspend fun delayTarget(target: Tile) {
        var count = 0
        if (tile.distanceTo(target) >= 50) {
            return
        }
        while (tile != target && count++ < 50 && mode != EmptyMode) {
            delay()
        }
    }
    /**
     * Interrupt-able pausing of scripts
     * Note: can't be used after a dialogue suspension in an interaction as the
     * interaction will have finished and there will be nothing to resume the suspension
     */
    suspend fun pause(ticks: Int) {
        Suspension.start(this, ticks)
    }

    /**
     * Movement delay, typically operating/interacting with an object or floor item that performs an animation or exact movement
     */
    suspend fun arriveDelay() {
        val delay = steps.last - GameLoop.tick
        if (delay <= 0) {
            return
        }
        delay(delay)
    }

    /**
     * Set the range a player can interact with their target from
     */
    suspend fun approachRange(range: Int?, update: Boolean = true) {
        val interact = mode as? Interact ?: return
        interact.updateRange(range, update)
        if (range != null) {
            while (!interact.arrived(range) && steps.isNotEmpty()) {
                delay(1)
            }
            interact.launched = true
        }
    }

}
