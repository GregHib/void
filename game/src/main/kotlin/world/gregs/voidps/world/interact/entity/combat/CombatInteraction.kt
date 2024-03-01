package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.event.EventDispatcher

/**
 * Replaces the current [Interaction] when combat is triggered via [Interact] to
 * allow the first [CombatSwing] to occur on the same tick.
 * After [Interact] is complete it is switched to [CombatMovement]
 */
data class CombatInteraction(
    override val character: Character,
    val target: Character
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override fun size() = 1

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> "combat_interact"
        else -> null
    }
}