package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetNPCContext
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Priority
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.onCharacter
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

fun itemOnNPCApproach(item: String, npc: String, block: suspend ItemOnNPC.() -> Unit) {
    onCharacter<ItemOnNPC>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(npc, this.target.id) }) { _: Character ->
        block.invoke(this)
    }
}

fun itemOnNPCOperate(item: String, npc: String, block: suspend ItemOnNPC.() -> Unit) {
    on<ItemOnNPC>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(npc, this.target.id) }) { _: Player ->
        block.invoke(this)
    }
}

fun spellOnNPCApproach(id: String, component: String = "*", priority: Priority = Priority.MEDIUM, block: suspend ItemOnNPC.() -> Unit) {
    on<ItemOnNPC>({ approach && wildcardEquals(component, this.component) && wildcardEquals(id, this.id) }, priority) { _: Player ->
        block.invoke(this)
    }
}

fun spellOnNPCOperate(id: String, component: String = "*", block: suspend ItemOnNPC.() -> Unit) {
    on<ItemOnNPC>({ operate && wildcardEquals(component, this.component) && wildcardEquals(id, this.id) }) { _: Player ->
        block.invoke(this)
    }
}