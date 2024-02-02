package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetObjectContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class ItemOnObject(
    override val character: Character,
    override val target: GameObject,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction(), TargetObjectContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun itemOnObjectApproach(item: String, id: String, inventory: String = "inventory", block: suspend ItemOnObject.() -> Unit) {
    on<ItemOnObject>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(id, this.target.id) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
        block.invoke(this)
    }
}

fun itemOnObjectOperate(item: String, id: String, inventory: String = "inventory", block: suspend ItemOnObject.() -> Unit) {
    on<ItemOnObject>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(id, this.target.id) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
        block.invoke(this)
    }
}