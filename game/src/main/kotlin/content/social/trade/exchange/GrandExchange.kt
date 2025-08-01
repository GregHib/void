package content.social.trade.exchange

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item

class GrandExchange {
    fun offer(slot: Int): Offer {
        return Offer.EMPTY
    }

    fun sell(player: Player, item: Item) {
    }
}