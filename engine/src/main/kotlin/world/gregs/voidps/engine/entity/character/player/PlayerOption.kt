package world.gregs.voidps.engine.entity.character.player

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction

data class PlayerOption(
    override val character: Character,
    val target: Player,
    val option: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}