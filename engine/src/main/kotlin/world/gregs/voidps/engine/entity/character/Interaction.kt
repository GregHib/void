package world.gregs.voidps.engine.entity.character

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasScreenOpen
import world.gregs.voidps.engine.entity.InteractiveEntity
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.map.Distance
import world.gregs.voidps.engine.path.algorithm.BresenhamsLine

class Interaction(
    private var character: Character,
    private val los: BresenhamsLine
) {
    var target: InteractiveEntity? = null
        private set
    var approachRangeCalled: Boolean = false
    private var currentApproachRange: Int = 10

    private var persistent = false
    private var interacted = false
    private var moved = false

    fun use(entity: InteractiveEntity) {
        resetTarget()
        target = entity
    }

    fun approach(entity: InteractiveEntity, range: Int = 10) {
        resetTarget()
        target = entity
        currentApproachRange = range
    }

    /**
     * Check if the character is in "melee distance" of the target, and has line of walk to them.
     */
    fun inOperableDistance(): Boolean {
        if (target !is Player && target !is NPC && moved) {
            return false
        }
        val target = target ?: return false
        return target.interactTarget.reached(character.tile, character.size)
    }

    /**
     * Check if the character is within [currentApproachRange] of the target, and has line of sight to them.
     */
    fun inApproachDistance(): Boolean {
        val target = target ?: return false
        if (character.tile.distanceTo(target.tile, target.size) > currentApproachRange) {
            return false
        }
        return los.withinSight(character.tile, Distance.getNearest(target.tile, target.size, character.tile), walls = false, ignore = true)
    }

    fun resetTarget() {
        currentApproachRange = 10
        approachRangeCalled = false
        target = null
        persistent = false
    }

    fun before() {
        approachRangeCalled = false
        interacted = false
        moved = false
        interacted = interact(second = false)
    }

    fun after(moved: Boolean) {
        this.moved = moved
        if (moved) {
            character.start("last_movement", ticks = 1)
        }
        interacted = interact(second = true)
        reset()
    }

    private fun interact(second: Boolean): Boolean {
        if (delayed() || containsModalInterface()) {
            return false
        }
        if (second && interactedButNotCalled()) {
            return false
        }
        when {
            // If the interacted boolean wasn't set to true, or if a script that executed above called aprange(n), process the second block.
            inOperableDistance() && character.events.emit(Operated(target!!)) -> {
                return true
            }
            inApproachDistance() && character.events.emit(Approached(target!!)) -> {
                if (second) {
                    approachRangeCalled = false
                }
                return true
            }
            inApproachDistance() -> {
                if (second) {
                    (character as? Player)?.message("Nothing interesting happens.", ChatType.Engine)
                    return true
                }
                return false
            }
            inOperableDistance() -> {
                (character as? Player)?.message("Nothing interesting happens.", ChatType.Engine)
                return true
            }
            else -> return false
        }
    }

    private fun reset() {
        if (delayed() || containsModalInterface()) {
            return
        }
        if (!interacted && !moved && !hasSteps()) {
//            (character as? Player)?.message("I can't reach that!", ChatType.Engine)
            resetTarget()
        } else if (interactedButNotCalled() && !persistent) {
            resetTarget()
        }
    }

    private fun interactedButNotCalled() = interacted && !approachRangeCalled

    fun delayed(): Boolean {
        // has script delay
        return false
    }

    fun hasSteps(): Boolean {
        return character.movement.path.steps.isNotEmpty()
    }

    fun containsModalInterface(): Boolean {
        return (character as? Player)?.hasScreenOpen() ?: false
    }

}