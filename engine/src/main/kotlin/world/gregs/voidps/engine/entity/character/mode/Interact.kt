package world.gregs.voidps.engine.entity.character.mode

import org.rsmod.pathfinder.LineValidator
import org.rsmod.pathfinder.PathFinder
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.*
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.target.TargetStrategies
import world.gregs.voidps.engine.entity.character.target.TargetStrategy
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.get

class Interact(
    character: Character,
    val target: Entity,
    private val option: String,
    private val strategy: TargetStrategy = TargetStrategies.get(target),
    shape: Int? = null,
    approachRange: Int? = null,
    private val persistent: Boolean = false,
    private val faceTarget: Boolean = true,
    forceMovement: Boolean = false
) : MovementMode(character) {

    init {
        if (character is Player) {
            val pf = PathFinder(flags = get<Collisions>().data, useRouteBlockerFlags = true)
            val route = pf.findPath(
                character.tile.x,
                character.tile.y,
                strategy.tile.x,
                strategy.tile.y,
                character.tile.plane,
                srcSize = character.size.width,
                destWidth = strategy.size.width,
                destHeight = strategy.size.height,
                objShape = shape ?: strategy.exitStrategy)
            queueRoute(route)
        } else {
            queueStep(strategy.tile, forceMovement)
        }
    }

    private var cancelTime: Long = 0
    private val startTime = GameLoop.tick
    private var updateRange: Boolean = false
    private var interacted = false
    private var moved = false
    var approachRange: Int? = approachRange
        private set

    fun setApproachRange(range: Int?) {
        updateRange = true
        this.approachRange = range
    }

    override fun tick() {
        if (faceTarget) {
            character.face(target)
        }
        /*if (!target.exists) {
            clear(resetFace = true)
        } else if(cancelCheck()) {
            clear()
            character.start("face_lock")
        }*/
        updateRange = false
        interacted = false
        moved = false
        interacted = interact(after = false)

        if (interacted && reached()) {
            if (persistent) {
                character.moving = false
                steps.clear()
            } else {
                clearMovement()
            }
        }
        val before = character.tile
        if (canMove()) {
            super.tick()
        }
        this.moved = character.tile != before
        if (moved) {
            character.start("last_movement", ticks = 1)
        }
        interacted = interacted or interact(after = true)
        reset()
    }

    private fun canMove(): Boolean {
        if (delayed() || character.hasModalOpen()) {
            return false
        }
        return true
    }

    fun reached(): Boolean {
        return !updateRange || arrived(approachRange ?: -1)
    }

    private fun interactedWithoutRangeUpdate() = interacted && !updateRange

    private fun interact(after: Boolean): Boolean {
        if (delayed() || character.hasModalOpen()) {
            return false
        }
        // Only process the second block if no interaction occurred or the approach range was changed
        if (after && interactedWithoutRangeUpdate()) {
            return false
        }
        val withinMelee = arrived()
        val withinRange = arrived(approachRange ?: 10)
        when {
            withinMelee && character.events.emit(Operate(target, option, partial)) -> {}
            withinRange && character.events.emit(Approach(target, option, partial)) -> if (after) updateRange = false
            withinMelee || withinRange -> (character as? Player)?.message("Nothing interesting happens.", ChatType.Engine)
            else -> return false
        }
        return true
    }

    private fun arrived(distance: Int = -1): Boolean {
        val strategy = strategy
        if (distance == -1) {
            return strategy.reached(this)
        }
        if (!character.tile.within(strategy.tile, distance)) {
            return false
        }
        return get<LineValidator>().hasLineOfSight(
            srcX = character.tile.x,
            srcY = character.tile.y,
            z = character.tile.plane,
            srcSize = character.size.width,
            destX = strategy.tile.x,
            destY = strategy.tile.y,
            destWidth = strategy.size.width,
            destHeight = strategy.size.height
        )
    }

    private fun reset() {
        if (character.hasModalOpen()) {
            return
        }

        val idle = character.events.suspend == null
        if (interactedWithoutRangeUpdate() && !persistent) {
            clearMovement()
            if (idle) {
                clear()
            }
        }

        val outOfRange = !arrived(approachRange ?: -1)
        val frozenOutOfRange = outOfRange && character.hasEffect("frozen")
        if (!frozenOutOfRange && (moved || steps.isNotEmpty()) || interacted) {
            return
        }

        if (!persistent && (idle || outOfRange || !character.moving)) {
            (character as? Player)?.message("I can't reach that!", ChatType.Engine)
            clear()
        }
    }

    fun clear(resetFace: Boolean = false) {
        character.events.emit(StopInteraction)
        if (resetFace && startTime == GameLoop.tick) {
            character.start("face_lock", 1)
        }
        cancelTime = GameLoop.tick
        approachRange = null
        updateRange = false
        character.mode = EmptyMode
    }

    private fun delayed(): Boolean {
        return false
    }

    private fun Character.hasModalOpen() = (this as? Player)?.hasScreenOpen() ?: false
}