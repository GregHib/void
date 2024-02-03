package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetNPCContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class ItemOnNPC(
    override val character: Character,
    override val target: NPC,
    val id: String,
    val component: String,
    val item: Item,
    val itemSlot: Int,
    val inventory: String
) : Interaction(), TargetNPCContext {
    override fun copy(approach: Boolean) = copy().apply { this.approach = approach }
}

fun itemOnNPCApproach(filter: ItemOnNPC.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend ItemOnNPC.(Player) -> Unit) {
    on<ItemOnNPC>({ approach && filter(this, it) }, priority, block)
}

fun itemOnNPCOperate(filter: ItemOnNPC.(Player) -> Boolean, priority: Priority = Priority.MEDIUM, block: suspend ItemOnNPC.(Player) -> Unit) {
    on<ItemOnNPC>({ operate && filter(this, it) }, priority, block)
}

fun itemOnNPCApproach(item: String, npc: String, inventory: String = "inventory", block: suspend ItemOnNPC.() -> Unit) {
    on<ItemOnNPC>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(npc, this.target.id) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
        block.invoke(this)
    }
}

fun itemOnNPCOperate(item: String, npc: String, inventory: String = "inventory", block: suspend ItemOnNPC.() -> Unit) {
    on<ItemOnNPC>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(npc, this.target.id) && wildcardEquals(inventory, this.inventory) }) { _: Player ->
        block.invoke(this)
    }
}