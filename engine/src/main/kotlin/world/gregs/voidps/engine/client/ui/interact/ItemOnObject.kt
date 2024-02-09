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

fun itemOnObjectApproach(item: String, obj: String, block: suspend ItemOnObject.() -> Unit) {
    on<ItemOnObject>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(obj, this.target.id) }) { _: Player ->
        block.invoke(this)
    }
}

fun itemOnObjectOperate(item: String = "*", obj: String = "*", def: String = "*", inventory: String = "*", priority: Priority = Priority.MEDIUM, block: suspend ItemOnObject.() -> Unit) {
    on<ItemOnObject>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(obj, this.target.id) && (def == "*" || this.item.def.contains(def)) && wildcardEquals(inventory, this.inventory) }, priority) { _: Player ->
        block.invoke(this)
    }
}

fun itemOnObjectOperate(items: Set<String> = setOf("*"), objects: Set<String> = setOf("*"), def: String = "*", inventory: String = "*", block: suspend ItemOnObject.() -> Unit) {
    for (obj in objects) {
        for (item in items) {
            on<ItemOnObject>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(obj, this.target.id) && (def == "*" || this.item.def.contains(def)) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
                block.invoke(this)
            }
        }
    }
}