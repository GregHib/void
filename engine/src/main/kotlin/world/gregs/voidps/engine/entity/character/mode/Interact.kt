package world.gregs.voidps.engine.entity.character.mode

import org.rsmod.pathfinder.LineValidator
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
import world.gregs.voidps.engine.event.SuspendableEvent
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
) : Movement(character, strategy, forceMovement, shape) {

    private var cancelTime: Long = 0
    private val startTime = GameLoop.tick
    private var updateRange: Boolean = false
    private var interacted = false
    var approachRange: Int? = approachRange
        private set
    private var event: SuspendableEvent? = null

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
        } else if (cancelCheck()) {
            clear()
            character.start("face_lock")
        }*/
        updateRange = false
        interacted = interact(afterMovement = false)
        val before = character.tile
        if (canMove()) {
            super.tick()
        }
        if (character.tile != before) {
            character.start("last_movement", ticks = 1)
        }
        interacted = interacted or interact(afterMovement = true)
        reset()
    }

    private fun canMove(): Boolean {
        if (delayed() || character.hasModalOpen()) {
            return false
        }
        return true
    }

    private fun interactedWithoutRangeUpdate() = interacted && !updateRange

    private fun interact(afterMovement: Boolean): Boolean {
        if (delayed() || character.hasModalOpen()) {
            return false
        }
        // Only process the second block if no interaction occurred or the approach range was changed
        if (afterMovement && interactedWithoutRangeUpdate()) {
            return false
        }
        val withinMelee = arrived()
        val withinRange = arrived(approachRange ?: 10)
        when {
            withinMelee && launch(Operate(target, option, partial)) -> {}
            withinRange && launch(Approach(target, option, partial)) -> {}
            withinMelee || withinRange -> (character as? Player)?.message("Nothing interesting happens.", ChatType.Engine)
            else -> return false
        }
        return !updateRange
    }

    private fun launch(event: SuspendableEvent): Boolean {
        val suspend = this.event?.suspend
        if (suspend == null) {
            this.event = event
            return character.events.emit(event)
        } else {
            if (suspend.finished()) {
                this.event = null
                return false
            }
            if (suspend.ready()) {
                suspend.resume()
            }
            return true
        }
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

    private fun idle(): Boolean {
        return event?.suspend?.finished() ?: false
    }

    private fun reset() {
        if (character.hasModalOpen() || persistent) {
            return
        }
        if (interactedWithoutRangeUpdate() && idle()) {
            clear()
            return
        }
        if (updateRange) {
            return
        }
        if (!character.hasEffect("frozen") && (character.hasEffect("last_movement") || steps.isNotEmpty() || character.moving)) {
            return
        }
        (character as? Player)?.message("I can't reach that!", ChatType.Engine)
        clear()
    }

    fun clear(resetFace: Boolean = false) {
        character.events.emit(StopInteraction)
        if (resetFace && startTime == GameLoop.tick) {
            character.start("face_lock", 1)
        }
        cancelTime = GameLoop.tick
        approachRange = null
        updateRange = false
        interacted = false
        character.mode = EmptyMode
    }

    private fun delayed(): Boolean {
        return false
    }

    private fun Character.hasModalOpen() = (this as? Player)?.hasScreenOpen() ?: false
}