package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.suspend.resumeSuspension

/**
 * Moves a player within interact distance of [target]
 *
 * Operate interactions require the [character] to be standing next-to but not under [target]
 * Approach interactions require the [character] within [approachRange] and line of sight of [target]
 *
 * [operate] or [approach] is called once when within range. Once [launched] the target is no longer
 * re-evaluated, [Character.suspension] is resumed every subsequent tick until the interaction is completed.
 * Interactions are only processed while the [character] isn't delayed or has menu interface open.
 */

open class Interact(
    character: Character,
    open val target: Entity,
    strategy: TargetStrategy = TargetStrategy(character, target),
    private var approachRange: Int? = null,
    private val faceTarget: Boolean = true,
    shape: Int? = null,
) : Movement(character, strategy, shape) {
    var launched = false

    open fun hasOperate(): Boolean = false

    open fun hasApproach(): Boolean = false

    open fun operate() {}

    open fun approach() {}

    private var replacement: (() -> Unit)? = null

    /**
     * Replace this interaction by calling [block] once after this tick's movement (e.g. to start combat)
     */
    fun replace(block: () -> Unit) {
        this.replacement = block
    }

    private var updateRange: Boolean = false

    fun updateRange(approachRange: Int?, update: Boolean = true) {
        updateRange = update && approachRange != null
        this.approachRange = approachRange
        launched = false
    }

    override fun start() {
        super.start()
        if (faceTarget) {
            val target = target
            if (target is Character) {
                character.watch(target)
            }
            character["face_entity"] = target
        }
        (character as? Player)?.closeDialogue()
        character.queue.clearWeak()
        character.suspension = null
    }

    /**
     * Processes interactions when not delayed or busy
     * Clearing when finished or unable to reach the target
     */
    override fun tick() {
        if (!validTarget()) {
            return
        }
        if (character.delayed || character.hasMenuOpen()) {
            super.tick()
            return
        }
        // Continue started script
        if (launched && replacement == null) {
            character.resumeSuspension()
            if (character.mode == this && interactionFinished()) {
                clear()
            }
            return
        }
        updateRange = false
        if (stepOut()) {
            super.tick()
            return
        }
        calculate()
        character.walkTrigger()
        val interacted = processInteraction()
        if (character.mode != this) {
            return
        }
        // Not launched means the script requested a re-launch (e.g. updated approach range)
        if (interacted && launched && interactionFinished()) {
            clear()
            return
        }
        if (character.hasClock("movement_delay") || character.visuals.moved || arrived(approachRange ?: -1) || character.suspension != null) {
            return
        }
        character.cantReach()
        clear()
    }

    /**
     * Target exists and is interact-able.
     */
    private fun validTarget(): Boolean {
        val target = target
        if (target is Character && target["dead", false]) {
            clear()
            return false
        }
        if (!launched && !exists(target)) {
            clear()
            return false
        }
        return true
    }

    /**
     * Target hasn't been removed or replaced before interacting
     */
    private fun exists(target: Entity): Boolean {
        return when (target) {
            is NPC -> !target.hide
            is GameObject -> GameObjects.contains(target)
            else -> true
        }
    }

    /**
     * Checks interactions before [Movement] and afterward when
     * target changed or failed to interact previously.
     */
    private fun processInteraction(): Boolean {
        var interacted = interact(afterMovement = false)
        if (interacted && !updateRange && arrived(approachRange ?: -1)) {
            clearSteps()
        }
        if (replacement != null) {
            interacted = false
        }
        if (!character.hasMenuOpen()) {
            super.tick()
        }
        if (replacement == null && (!interacted || updateRange)) {
            val interact = interact(afterMovement = true)
            if (character.mode != this) {
                return true
            }
            if (interact && !updateRange && arrived(approachRange ?: -1)) {
                clearSteps()
            }
            interacted = interacted || interact
        }
        val replace = replacement
        if (replace != null) {
            this.replacement = null
            replace.invoke()
            return true
        }
        return interacted
    }

    /**
     * Checks when [character] is within operate or approach distance
     * that an interaction or [noInterest] occurs.
     */
    private fun interact(afterMovement: Boolean): Boolean {
        val withinMelee = arrived()
        val withinRange = arrived(approachRange ?: 10)
        when {
            withinMelee && hasOperate() -> if (launch(true) && afterMovement) updateRange = false
            withinRange && hasApproach() -> if (launch(false) && afterMovement) updateRange = false
            withinMelee -> {
                character.noInterest()
                clear()
            }
            else -> return false
        }
        return true
    }

    /**
     * Continue any suspended, clear any finished or start a new interaction
     */
    private fun launch(operate: Boolean): Boolean {
        if (character.resumeSuspension()) {
            return true
        }
        if (!launched) {
            launched = true
            if (operate) {
                operate()
            } else {
                approach()
            }
            return true
        }
        return false
    }

    private fun interactionFinished() = character.suspension == null && !character.delayed

    private fun clear() {
        if (character.suspension != null) {
            clearSteps()
        }
        approachRange = null
        updateRange = false
        if (character.mode == this) {
            character.mode = EmptyMode
        }
    }

    override fun onCompletion() {
    }
}
