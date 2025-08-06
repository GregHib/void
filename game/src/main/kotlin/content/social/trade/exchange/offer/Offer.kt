package content.social.trade.exchange.offer

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair

data class Offer(
    val id: Int = 0,
    val item: String = "",
    val amount: Int = 0,
    val price: Int = 0,
    val sell: Boolean = false,
    var state: OfferState = OfferState.Pending,
    var lastUpdated: Long = System.currentTimeMillis(),
    var lastActive: Long = System.currentTimeMillis(),
    var remaining: Int = 0,
    var excess: Int = 0,
    var account: String = "",
) {

    companion object {

        fun ConfigReader.readOffer(id: Int, item: String, sell: Boolean): Offer {
            var amount = 0
            var price = 0
            var state: OfferState = OfferState.Pending
            var lastUpdated: Long = System.currentTimeMillis()
            var lastActive: Long = System.currentTimeMillis()
            var completed = 0
            var excess = 0
            var account = ""
            while (nextPair()) {
                when (val key = key()) {
                    "amount" -> amount = int()
                    "price" -> price = int()
                    "state" -> state = OfferState.valueOf(string())
                    "last_updated" -> lastUpdated = long()
                    "last_active" -> lastActive = long()
                    "completed" -> completed = int()
                    "excess" -> excess = int()
                    "account" -> account = string()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                }
            }
            return Offer(
                id = id,
                item = item,
                amount = amount,
                price = price,
                sell = sell,
                state = state,
                lastUpdated = lastUpdated,
                lastActive = lastActive,
                remaining = completed,
                excess = excess,
                account = account
            )
        }

        fun ConfigWriter.write(offer: Offer) {
            writePair("amount", offer.amount)
            writePair("price", offer.price)
            writePair("state", offer.state.name)
            writePair("last_updated", offer.lastUpdated)
            writePair("last_active", offer.lastActive)
            if (offer.remaining != 0) {
                writePair("completed", offer.remaining)
            }
            if (offer.excess != 0) {
                writePair("excess", offer.excess)
            }
            if (offer.account != "") {
                writePair("account", offer.account)
            }
        }

        val EMPTY = Offer()
    }
}