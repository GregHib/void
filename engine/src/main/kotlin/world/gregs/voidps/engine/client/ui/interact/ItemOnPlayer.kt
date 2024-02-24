package world.gregs.voidps.engine.client.ui.interact

import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.mode.interact.TargetPlayerContext
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
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

fun itemOnPlayerApproach(item: String, id: String = "*", block: suspend ItemOnPlayer.() -> Unit) {
    on<ItemOnPlayer>({ approach && wildcardEquals(item, this.item.id) && wildcardEquals(id, this.id) }) {
        block.invoke(this)
    }
}

fun itemOnPlayerOperate(item: String, id: String = "*", block: suspend ItemOnPlayer.() -> Unit) {
    on<ItemOnPlayer>({ operate && wildcardEquals(item, this.item.id) && wildcardEquals(id, this.id) }) {
        block.invoke(this)
    }
}

fun spellOnPlayerApproach(id: String, component: String, block: suspend ItemOnPlayer.() -> Unit) {
    on<ItemOnPlayer>({ approach && wildcardEquals(id, this.id) && wildcardEquals(component, this.component) }) {
        block.invoke(this)
    }
}

fun spellOnPlayerOperate(id: String, component: String, block: suspend ItemOnPlayer.() -> Unit) {
    on<ItemOnPlayer>({ operate && wildcardEquals(id, this.id) && wildcardEquals(component, this.component) }) {
        block.invoke(this)
    }
}