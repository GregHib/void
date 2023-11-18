package world.gregs.voidps.world.interact.entity.combat

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction

data class CombatInteraction(
    override val character: Character,
    val target: Character
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}