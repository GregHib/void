package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.ObjectTargetContext
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject

data class ItemOnObject(
    override val character: Character,
    override val target: GameObject,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction(), ObjectTargetContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}