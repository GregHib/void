package world.gregs.voidps.engine.entity.character

import org.rsmod.pathfinder.LineValidator
import org.rsmod.pathfinder.reach.DefaultReachStrategy
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.move.moving
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.target.TargetStrategies
import world.gregs.voidps.engine.entity.character.target.TargetStrategy
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.get

class Interaction(
    private var character: Character
) {
    var target: Entity? = null
        private set
    var strategy: TargetStrategy? = null
    private var option: String? = null
    private var updateRange: Boolean = false
    var approachRange: Int? = null
        private set
    private var cancelTime: Long = 0
    private var startTime: Long = 0

    private var faceTarget = false
    var persistent = false
        private set
    private var interacted = false
    private var moved = false

    fun setApproachRange(range: Int) {
        updateRange = true
        this.approachRange = range
    }

    fun <T : Entity> with(entity: T, option: String, strategy: TargetStrategy = TargetStrategies.get(entity), range: Int? = null, persist: Boolean = false, faceTarget: Boolean = true) {
        clear()
        println("Interact $entity $persist")
        this.target = entity
        this.strategy = strategy
        this.option = option
        approachRange = range
        persistent = persist
        startTime = GameLoop.tick
        this.faceTarget = faceTarget
    }

    fun before() {
        val target = target ?: return
        if (faceTarget) {
            character.face(target)
        }
        /*if (!target.available) {
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
                character.movement.steps.clear()
            } else {
                character.movement.clear()
            }
        }
    }

    fun reached(): Boolean {
        return !updateRange || arrived(approachRange ?: -1)
    }

    fun after(moved: Boolean) {
        target ?: return
        this.moved = moved
        if (moved) {
            character.start("last_movement", ticks = 1)
        }
        interacted = interacted or interact(after = true)
        reset()
    }

    private fun interactedWithoutRangeUpdate() = interacted && !updateRange

    private fun interact(after: Boolean): Boolean {
        if (delayed() || containsModalInterface()) {
            return false
        }
        // Only process the second block if no interaction occurred or the approach range was changed
        if (after && interactedWithoutRangeUpdate()) {
            return false
        }
        val target = target ?: return false
        val option = option ?: return false
        val withinMelee = arrived()
        val withinRange = arrived(approachRange ?: 10)
        when {
            withinMelee && character.events.emit(Operate(target, option, character.movement.partial)) -> {}
            withinRange && character.events.emit(Approach(target, option, character.movement.partial)) -> if (after) updateRange = false
            withinMelee || withinRange -> (character as? Player)?.message("Nothing interesting happens.", ChatType.Engine)
            else -> return false
        }
        return true
    }

    private fun arrived(distance: Int = -1): Boolean {
        val strategy = strategy ?: return false
        if (distance == -1) {
            return DefaultReachStrategy.reached(
                flags = get<Collisions>().data,
                x = character.tile.x,
                y = character.tile.y,
                z = character.tile.plane,
                srcSize = character.size.width,
                destX = strategy.tile.x,
                destY = strategy.tile.y,
                destWidth = strategy.size.width,
                destHeight = strategy.size.height,
                rotation = strategy.rotation,
                shape = strategy.exitStrategy,
                accessBitMask = strategy.bitMask
            )
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
        if (containsModalInterface()) {
            return
        }
        if (target == null) {
            return
        }
        val idle = target != null && character.events.suspend == null
        if (interactedWithoutRangeUpdate() && !persistent) {
            character.movement.clear()
            if (idle) {
                clear()
            }
        }

        val outOfRange = !arrived(approachRange ?: -1)
        val frozenOutOfRange = outOfRange && character.hasEffect("frozen")
        if (!frozenOutOfRange && (moved || character.movement.steps.isNotEmpty()) || interacted) {
            return
        }

        if (!persistent && (idle || outOfRange || !character.moving)) {
            (character as? Player)?.message("I can't reach that!", ChatType.Engine)
            clear()
        }
    }

    fun clear(resetFace: Boolean = false, resetRoute: Boolean = true) {
        if (target != null) {
            character.events.emit(StopInteraction)
            if (resetFace && startTime == GameLoop.tick) {
                character.start("face_lock", 1)
            }
            cancelTime = GameLoop.tick
            if (resetRoute) {
                character.movement.clear()
            }
        }
        approachRange = null
        updateRange = false
        target = null
        strategy = null
        option = null
        persistent = false
    }

    fun delayed(): Boolean {
        // has script delay
        return false
    }

    fun containsModalInterface(): Boolean {
        return (character as? Player)?.hasScreenOpen() ?: false
    }

}