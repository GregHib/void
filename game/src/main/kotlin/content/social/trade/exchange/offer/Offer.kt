package content.social.trade.exchange.offer

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair

data class Offer(
    val item: String = "",
    val amount: Int = 0,
    val price: Int = 0,
    val sell: Boolean = false,
    var state: OfferState = OfferState.Pending,
    var lastUpdated: Long = System.currentTimeMillis(),
    var lastActive: Long = System.currentTimeMillis(),
    var remaining: Int = amount,
    var excess: Int = 0,
    var account: String = "",
) {

    companion object {

        fun ConfigReader.readOffer(): Offer {
            var item = ""
            var amount = 0
            var price = 0
            var sell = false
            var state: OfferState = OfferState.Pending
            var lastUpdated: Long = System.currentTimeMillis()
            var lastActive: Long = System.currentTimeMillis()
            var remaining = 0
            var excess = 0
            var account = ""
            while (nextPair()) {
                when (val key = key()) {
                    "item" -> item = string()
                    "amount" -> amount = int()
                    "price" -> price = int()
                    "sell" -> sell = boolean()
                    "state" -> state = OfferState.valueOf(string())
                    "last_updated" -> lastUpdated = long()
                    "last_active" -> lastActive = long()
                    "remaining" -> remaining = int()
                    "excess" -> excess = int()
                    "account" -> account = string()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                }
            }
            return Offer(
                item = item,
                amount = amount,
                price = price,
                sell = sell,
                state = state,
                lastUpdated = lastUpdated,
                lastActive = lastActive,
                remaining = remaining,
                excess = excess,
                account = account
            )
        }

        fun ConfigWriter.write(offer: Offer) {
            writePair("item", offer.item)
            writePair("amount", offer.amount)
            writePair("price", offer.price)
            writePair("state", offer.state.name)
            writePair("sell", offer.sell)
            writePair("last_updated", offer.lastUpdated)
            writePair("last_active", offer.lastActive)
            writePair("remaining", offer.remaining)
            writePair("excess", offer.excess)
            writePair("account", offer.account)
        }

        val EMPTY = Offer()
    }
}