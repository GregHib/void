package content.social.trade.exchange.offer

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair

/*
    Id, Account, Item id, Amount, Price, LastActive

    Id, Completed, Price, Excess

    Id, Item id, Amount, Price, State, Completed, Excess
 */
data class Offer(
    val id: Int = 0,
    val item: String = "",
    val amount: Int = 0,
    val price: Int = 0,
    val sell: Boolean = false,
    var state: OfferState = OfferState.Pending,
    var lastActive: Long = System.currentTimeMillis(),
    var completed: Int = 0,
    var excess: Int = 0,
    var account: String = "",
) {

    companion object {

        fun ConfigReader.readOffer(id: Int, item: String, sell: Boolean): Offer {
            var amount = 0
            var price = 0
            var state: OfferState = OfferState.Pending
            var lastActive: Long = System.currentTimeMillis()
            var completed = 0
            var excess = 0
            var account = ""
            while (nextPair()) {
                when (val key = key()) {
                    "amount" -> amount = int()
                    "price" -> price = int()
                    "state" -> state = OfferState.valueOf(string())
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
                lastActive = lastActive,
                completed = completed,
                excess = excess,
                account = account
            )
        }

        fun ConfigWriter.write(offer: Offer) {
            writePair("amount", offer.amount)
            writePair("price", offer.price)
            writePair("state", offer.state.name)
            writePair("last_active", offer.lastActive)
            if (offer.completed != 0) {
                writePair("completed", offer.completed)
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