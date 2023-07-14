package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction

data class FloorItemOption(
    override val character: Character,
    val item: FloorItem,
    val option: String
) : Interaction() {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}