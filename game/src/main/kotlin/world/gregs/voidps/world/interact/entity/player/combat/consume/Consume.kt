package world.gregs.voidps.world.interact.entity.player.combat.consume

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.CancellableEvent
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.event.wildcardEquals

data class Consume(val item: Item, val slot: Int) : CancellableEvent()

fun consume(vararg items: String, block: Consume.(Player) -> Unit) {
    for (item in items) {
        on<Consume>({ wildcardEquals(item, this.item.id) }) { player: Player ->
            block.invoke(this, player)
        }
    }
}