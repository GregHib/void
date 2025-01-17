package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.EventDispatcher
import world.gregs.voidps.engine.event.Events
import world.gregs.voidps.engine.suspend.arriveDelay

data class ItemOnObject(
    override val character: Player,
    override val target: GameObject,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : TargetInteraction<Player, GameObject>() {

    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }

    override val size = 5

    override fun parameter(dispatcher: EventDispatcher, index: Int) = when (index) {
        0 -> if (approach) "item_on_approach_object" else "item_on_operate_object"
        1 -> item.id
        2 -> target.id
        3 -> id
        4 -> component
        else -> null
    }
}

fun itemOnObjectOperate(
    item: String = "*",
    obj: String = "*",
    id: String = "*",
    component: String = "*",
    itemDef: String = "*",
    arrive: Boolean = true,
    override: Boolean = true,
    handler: suspend ItemOnObject.() -> Unit
) {
    if (itemDef != "*") {
        Events.handle<ItemOnObject>("item_on_operate_object", "*", obj, id, component, override = override) {
            if (this.item.def.contains(itemDef)) {
                if (arrive) {
                    arriveDelay()
                }
                handler.invoke(this)
            }
        }
    } else {
        Events.handle<ItemOnObject>("item_on_operate_object", item, obj, id, component, override = override){
            if (arrive) {
                arriveDelay()
            }
            handler.invoke(this)
        }
    }
}

fun itemOnObjectApproach(item: String = "*", obj: String = "*", id: String = "*", component: String = "*", override: Boolean = true, handler: suspend ItemOnObject.() -> Unit) {
    Events.handle<ItemOnObject>("item_on_approach_object", item, obj, id, component, override = override) {
        handler.invoke(this)
    }
}

fun itemOnObjectOperate(
    items: Set<String> = setOf("*"),
    objects: Set<String> = setOf("*"),
    def: String = "*",
    id: String = "*",
    component: String = "*",
    arrive: Boolean = true,
    override: Boolean = true,
    block: suspend ItemOnObject.() -> Unit
) {
    val handler: suspend ItemOnObject.(Player) -> Unit = {
        if (arrive) {
            arriveDelay()
        }
        block.invoke(this)
    }
    if (def != "*") {
        for (obj in objects) {
            Events.handle("item_on_operate_object", "*", obj, id, component, handler = handler, override = override)
        }
    } else {
        for (obj in objects) {
            for (item in items) {
                Events.handle("item_on_approach_object", item, obj, id, component, handler = handler, override = override)
            }
        }
    }
}