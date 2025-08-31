package world.gregs.voidps.engine.entity.character.mode.interact

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.entity.Entity
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.cantReach
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.resumeSuspension
import kotlin.coroutines.cancellation.CancellationException

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
    interaction: Interaction<*>? = null,
    strategy: TargetStrategy = TargetStrategy(target),
    private var approachRange: Int? = null,
    private val faceTarget: Boolean = true,
    shape: Int? = null,
    private var has: (Boolean) -> Boolean = { false },
    private var interact: (suspend (Boolean) -> Unit)? = null,
) : Movement(character, strategy, shape) {

    private var approach: Interaction<*>? = interaction?.copy(true)
    private var operate: Interaction<*>? = interaction?.copy(false)
    private var clearInteracted = false

    fun updateInteraction(interaction: Interaction<*>) {
        approach = interaction.copy(true)
        operate = interaction.copy(false)
        clearInteracted = true
    }

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
        if (character.contains("delay") || character.hasMenuOpen()) {
            super.tick()
            return
        }
        updateRange = false
        calculate()
        val interacted = processInteraction()
        if (interacted && interactionFinished()) {
            clear()
            return
        }
        if (character.hasClock("movement_delay") || character.visuals.moved || arrived(approachRange ?: -1) || character.suspension != null || character.delay != null) {
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
     * Checks interactions before [Movement] and afterward when
     * target changed or failed to interact previously.
     */
    private fun processInteraction(): Boolean {
        clearInteracted = false
        var interacted = interact(afterMovement = false)
        if (interacted && !updateRange && arrived(approachRange ?: -1)) {
            clearSteps()
        }
        if (clearInteracted) {
            interacted = false
            clearInteracted = false
        }
        if (!character.hasMenuOpen()) {
            super.tick()
        }
        if (!interacted || updateRange) {
            val interact = interact(afterMovement = true)
            interacted = interacted or interact
            if (clearInteracted) {
                interacted = false
                clearInteracted = false
            }
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
            withinMelee && has.invoke(false) -> if (launch(false) && afterMovement) updateRange = false
            withinRange && has.invoke(true) -> if (launch(true) && afterMovement) updateRange = false
            withinMelee && operate != null && Events.events.contains(character, operate!!) -> if (launch(operate!!) && afterMovement) updateRange = false
            withinRange && approach != null && Events.events.contains(character, approach!!) -> if (launch(approach!!) && afterMovement) updateRange = false
            withinMelee -> {
                character.noInterest()
                clear()
            }
            else -> return false
        }
        return true
    }

    private fun launch(interact: Boolean): Boolean {
        if (character.resumeSuspension()) {
            return true
        }
        val interaction = this.interact
        if (interaction != null) {
            this.interact = null
            this.has = { false }
            launch {
                interaction.invoke(interact)
            }
            return true
        }
        return false
    }

    fun launch(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(errorHandler, block = block)
    }

    /**
     * Continue any suspended, clear any finished or start a new interaction
     */
    private fun launch(event: Interaction<*>): Boolean {
        if (character.resumeSuspension()) {
            return true
        }
        if (!event.launched && character.emit(event)) {
            event.launched = true
            return true
        }
        return false
    }

    private fun interactionFinished() = character.suspension == null && !character.contains("delay")

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

    companion object {
        private val logger = InlineLogger()
        private val errorHandler = CoroutineExceptionHandler { _, throwable ->
            if (throwable !is CancellationException) {
                logger.warn(throwable) { "Error in event." }
            }
        }
        private val scope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
    }
}
