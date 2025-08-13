package content.social.trade.exchange

import content.social.trade.exchange.history.ItemHistory
import content.social.trade.exchange.offer.ClaimableOffers
import content.social.trade.exchange.offer.Offers

interface GrandExchangeStorage {
    fun offers(): Offers

    fun save(offers: Offers)

    fun claims(): ClaimableOffers

    fun save(offers: ClaimableOffers)

    fun history(): MutableMap<String, ItemHistory>

    fun save(history: Map<String, ItemHistory>)
}