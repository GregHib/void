package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.path.PathResult
import world.gregs.voidps.engine.path.PathType
import world.gregs.voidps.engine.path.strat.SingleTileTargetStrategy

class Interaction(
    private var character: Character
) {
    var target: InteractiveEntity? = null
        private set
    private var option: String? = null
    private var updateRange: Boolean = false
    private var approachRange: Int? = null
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
        val tileStrategy = if (entity is FloorItem) SingleTileTargetStrategy(entity.tile) else entity.interactTarget
        character.movement.set(tileStrategy, if (option == "Follow" && target is Player) PathType.Follow else if (character is Player) PathType.Smart else PathType.Dumb)
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
        val partial = character.movement.path.result is PathResult.Partial
        when {
            withinMelee && character.events.emit(Operated(target, option, partial)) -> {}
            withinRange && character.events.emit(Approached(target, option, partial)) -> if (after) updateRange = false
            withinMelee || withinRange -> (character as? Player)?.message("Nothing interesting happens.", ChatType.Engine)
            else -> return false
        }
        return true
    }

    private fun arrived(distance: Int = -1): Boolean {
        val target = target ?: return false
        if (distance == -1) {
            return target.interactTarget.reached(character.tile, character.size)
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
        if (!frozenOutOfRange && (moved || character.movement.path.steps.isNotEmpty()) || interacted) {
            return
        }

        val result = character.movement.path.result
        if (persistent || idle || outOfRange || (result is PathResult.Success && result.last != Tile.EMPTY)) {
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