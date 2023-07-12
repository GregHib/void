package world.gregs.voidps.engine.entity.character.mode.interact

import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.variable.get
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.suspend.resumeSuspension

/**
 * Moves a player within interact distance of [target]
 *
 * Operate interactions require the [character] to be standing next-to but not under [target]
 * Approach interactions require the [character] within [approachRange] and line of sight of [target]
 *
 * [Interaction] event is emitted when within range and will continue to
 * resume [Character.suspension] every subsequent tick until the interaction is completed.
 * Interactions are only processed while the [character] isn't delayed or has menu interface open.
 */
class Interact(
    character: Character,
    val target: Entity,
    interaction: Interaction,
    strategy: TargetStrategy = TargetStrategy(target),
    private var approachRange: Int? = null,
    private val faceTarget: Boolean = true,
    shape: Int? = null
) : Movement(character, strategy, shape) {

    private val approach: Interaction = interaction.copy(true)
    private val operate: Interaction = interaction.copy(false)
    private var updateRange: Boolean = false

    fun updateRange(approachRange: Int?, update: Boolean = true) {
        updateRange = update && approachRange != null
        this.approachRange = approachRange
    }

    override fun start() {
        super.start()
        if (faceTarget) {
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
        if (character.hasClock("delay") || character.hasMenuOpen()) {
            super.tick()
            return
        }
        updateRange = false
        val interacted = processInteraction()
        if (interacted && interactionFinished()) {
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
        if (target is Character && target["dead", false]) {
            clear()
            return false
        }
        return true
    }

    /**
     * Checks interactions before [Movement] and afterwards when
     * target changed or failed to interact previously.
     */
    private fun processInteraction(): Boolean {
        var interacted = interact(afterMovement = false)
        if (interacted && !updateRange && arrived(approachRange ?: -1)) {
            character.steps.clear()
        }
        if (!character.hasMenuOpen()) {
            super.tick()
        }
        if (!interacted || updateRange) {
            interacted = interacted or interact(afterMovement = true)
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
            withinMelee && character.events.contains(operate) -> if (launch(operate) && afterMovement) updateRange = false
            withinRange && character.events.contains(approach) -> if (launch(approach) && afterMovement) updateRange = false
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
    private fun launch(event: Interaction): Boolean {
        if (character.suspension != null) {
            character.resumeSuspension()
            return true
        }
        if (character.events.emit(event)) {
            return true
        }
        return false
    }

    private fun interactionFinished() = character.suspension == null

    private fun clear() {
        if (character.suspension != null) {
            character.steps.clear()
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

