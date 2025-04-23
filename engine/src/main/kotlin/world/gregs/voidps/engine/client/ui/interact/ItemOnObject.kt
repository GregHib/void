package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events

data class ItemOnObject(
    override val character: Player,
    override val target: GameObject,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : TargetInteraction<Player, GameObject>() {

    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 3

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "item_on_approach_object" else "item_on_operate_object"
        1 -> item.id
        2 -> target.id
        else -> null
    }
}

fun itemOnObjectOperate(item: String = "*", obj: String = "*", arrive: Boolean = true, handler: suspend ItemOnObject.() -> Unit) {
    Events.handle<ItemOnObject>("item_on_operate_object", item, obj) {
        if (arrive) {
            arriveDelay()
        }
        handler.invoke(this)
    }
}

fun itemOnObjectApproach(item: String = "*", obj: String = "*", handler: suspend ItemOnObject.() -> Unit) {
    Events.handle<ItemOnObject>("item_on_approach_object", item, obj) {
        handler.invoke(this)
    }
}

fun itemOnObjectOperate(objects: Set<String> = setOf("*"), arrive: Boolean = true, block: suspend ItemOnObject.() -> Unit) {
    val handler: suspend ItemOnObject.(Player) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    for (obj in objects) {
        Events.handle("item_on_operate_object", "*", obj, handler = handler)
    }
}