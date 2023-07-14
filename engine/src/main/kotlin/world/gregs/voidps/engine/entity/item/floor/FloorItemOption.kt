package world.gregs.voidps.engine.entity.item.floor

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.TargetFloorItemContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction

data class FloorItemOption(
    override val character: Character,
    override val target: FloorItem,
    val option: String
) : Interaction(), TargetFloorItemContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}