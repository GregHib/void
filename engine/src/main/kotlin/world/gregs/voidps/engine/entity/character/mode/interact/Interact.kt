package world.gregs.voidps.engine.entity.character.mode.interact

import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Movement
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.target.TargetStrategy
import world.gregs.voidps.engine.entity.clear
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.SuspendableEvent
import world.gregs.voidps.engine.utility.get

class Interact(
    character: Character,
    val target: Entity,
    private val option: SuspendableEvent,
    private val strategy: TargetStrategy = TargetStrategy(target),
    shape: Int? = null,
    approachRange: Int? = null,
    private val faceTarget: Boolean = true,
    forceMovement: Boolean = false
) : Movement(character, strategy, forceMovement, shape) {

    private val startTime = GameLoop.tick
    private var updateRange: Boolean = false
    private var interacted = false
    var approachRange: Int? = approachRange
        set(value) {
            updateRange = true
            field = value
        }
    var event: SuspendableEvent? = null
        private set
    var onStop: (() -> Unit)? = null
        set(value) {
            println("Set on stop")
            field = value
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
        return !character.hasModalOpen()
    }

    private fun interact(afterMovement: Boolean): Boolean {
        if (delayed() || character.hasModalOpen()) {
            return false
        }
        // Only process the second block if no interaction occurred or the approach range was changed
        if (afterMovement && interacted && !updateRange) {
            return false
        }
        val withinMelee = arrived()
        val withinRange = arrived(approachRange ?: 10)
        when {
            withinMelee && launch(option) -> if (afterMovement) updateRange = false
            withinRange && launch(Approach(option)) -> if (afterMovement) updateRange = false
            withinMelee || withinRange -> (character as? Player)?.message("Nothing interesting happens.", ChatType.Engine)
            else -> return false
        }
        return true
    }

    private fun launch(event: SuspendableEvent): Boolean {
        val suspend = this.event?.suspend
        if (suspend == null) {
            if (character.events.emit(event)) {
                this.event = event
                return true
            }
            return false
        }
        if (!suspend.finished() && suspend.ready()) {
            suspend.resume()
        }
        return true
    }

    private fun arrived(distance: Int = -1): Boolean {
        if (distance == -1) {
            return strategy.reached(this)
        }
        if (!character.tile.within(strategy.tile, distance)) {
            return false
        }
        return get<LineValidator>().hasLineOfSight(
            srcX = character.tile.x,
            srcY = character.tile.y,
            level = character.tile.plane,
            srcSize = character.size.width,
            destX = strategy.tile.x,
            destY = strategy.tile.y,
            destWidth = strategy.size.width,
            destHeight = strategy.size.height
        )
    }

    private fun idle(): Boolean {
        return interacted && event?.suspended != false && event?.suspend?.finished() != false
    }

    private fun reset() {
        if (idle() && !updateRange) {
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
        if (resetFace && startTime == GameLoop.tick) {
            character.start("face_lock", 1)
        }
        approachRange = null
        updateRange = false
        interacted = false
        this.event = null
        character.mode = EmptyMode
        character.clear("dialogue_target")
    }

    override fun stop() {
        super.stop()
        onStop?.invoke()
    }

    private fun delayed(): Boolean {
        return character.hasEffect("delay")
    }

    private fun Character.hasModalOpen() = (this as? Player)?.hasScreenOpen() ?: false
}

val Character.interact: Interact
    get() = mode as Interact