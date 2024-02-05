package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetObjectContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.Priority
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

fun itemOnObjectApproach(filter: ItemOnObject.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend ItemOnObject.(Player) -> Unit) {
    on<ItemOnObject>({ approach && filter(this, it) }, priority, block)
}

fun itemOnObjectOperate(filter: ItemOnObject.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend ItemOnObject.(Player) -> Unit) {
    on<ItemOnObject>({ operate && filter(this, it) }, priority, block)
}

fun itemOnObjectApproach(item: String, id: String, block: suspend ItemOnObject.() -> Unit) {
    on<ItemOnObject>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(id, this.target.id) }) { _: Player ->
        block.invoke(this)
    }
}

fun itemOnObjectOperate(item: String, id: String, priority: Priority = Priority.MEDIUM, block: suspend ItemOnObject.() -> Unit) {
    on<ItemOnObject>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(id, this.target.id) }, priority) { _: Player ->
        block.invoke(this)
    }
}