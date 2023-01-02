package world.gregs.voidps.engine.entity.character

import org.rsmod.pathfinder.reach.DefaultReachStrategy
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.get

class Interaction(
    private var character: Character
) {
    var target: InteractiveEntity? = null
        private set
    private var option: String? = null
    private var updateRange: Boolean = false
    var approachRange: Int? = null
        private set
    private var cancelTime: Long = 0
    private var startTime: Long = 0

    private var faceTarget = false
    private var persistent = false
    private var interacted = false
    private var moved = false

    fun setApproachRange(range: Int) {
        updateRange = true
        this.approachRange = range
    }

    fun with(entity: InteractiveEntity, option: String, range: Int? = null, persist: Boolean = false, faceTarget: Boolean = true) {
        clear()
        target = entity
        this.option = option
        approachRange = range
        persistent = persist
        startTime = GameLoop.tick
        this.faceTarget = faceTarget
        (character as? Player)?.walkTo(entity.tile)
        (character as? NPC)?.walkTo(entity.tile)
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
        val partial = character.movement.route?.alternative ?: false
        when {
            withinMelee && character.events.emit(Operate(target, option, partial)) -> {}
            withinRange && character.events.emit(Approach(target, option, partial)) -> if (after) updateRange = false
            withinMelee || withinRange -> (character as? Player)?.message("Nothing interesting happens.", ChatType.Engine)
            else -> return false
        }
        return true
    }

    private fun arrived(distance: Int = -1): Boolean {
        val target = target ?: return false
        if (distance == -1) {
            return DefaultReachStrategy.reached(get<Collisions>().data,
                target.tile.x,
                target.tile.y,
                target.tile.plane,
                character.tile.x,
                character.tile.y,
                character.size.width,
                character.size.height,
                target.size.width,
                0,
                0,
                0)
        }
        return character.withinDistance(target, distance) && character.withinSight(target, walls = true, ignore = true)
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

        if (persistent || idle || outOfRange || character.movement.route == null) {
            (character as? Player)?.message("I can't reach that!", ChatType.Engine)
            clear()
        }
    }

    fun clear(resetFace: Boolean = false, resetRoute: Boolean = true) {
        if (target != null) {
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