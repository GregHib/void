package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetPlayerContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class ItemOnPlayer(
    override val character: Character,
    override val target: Player,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction(), TargetPlayerContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun itemOnPlayerApproach(filter: ItemOnPlayer.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend ItemOnPlayer.(Player) -> Unit) {
    on<ItemOnPlayer>({ approach && filter(this, it) }, priority, block)
}

fun itemOnPlayerOperate(filter: ItemOnPlayer.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend ItemOnPlayer.(Player) -> Unit) {
    on<ItemOnPlayer>({ operate && filter(this, it) }, priority, block)
}

fun itemOnPlayerApproach(item: String, inventory: String = "inventory", block: suspend ItemOnPlayer.() -> Unit) {
    on<ItemOnPlayer>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
        block.invoke(this)
    }
}

fun itemOnPlayerOperate(item: String, inventory: String = "inventory", block: suspend ItemOnPlayer.() -> Unit) {
    on<ItemOnPlayer>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
        block.invoke(this)
    }
}