package content.social.trade.exchange.offer

import world.gregs.voidps.engine.data.exchange.ExchangeOffer
import world.gregs.voidps.engine.entity.character.player.Player

data class PendingOffer(val account: String, val offer: ExchangeOffer)