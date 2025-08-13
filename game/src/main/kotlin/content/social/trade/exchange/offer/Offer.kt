package content.social.trade.exchange.offer

import world.gregs.config.ConfigReader
import world.gregs.config.ConfigWriter
import world.gregs.config.writePair
import world.gregs.voidps.engine.data.exchange.OfferState


/**
 * Grand Exchange Offer
 * @param id unique
 * @param item id
 * @param amount to buy or sell
 * @param completed number purchased or sold so far
 * @param coins total received or refunded if bought below offer price
 * @param account account name of the player
 */

data class Offer(
    val id: Int = 0,
    val item: String = "",
    val amount: Int = 0,
    val price: Int = 0,
    var state: OfferState = OfferState.PendingBuy,
    var lastActive: Long = System.currentTimeMillis(),
    var completed: Int = 0,
    var coins: Int = 0,
    val account: String = "",
) {

    val sell: Boolean
        get() = state.sell

    fun open() {
        state = if (state.sell) OfferState.OpenSell else OfferState.OpenBuy
    }

    fun cancel() {
        state = if (state.sell) OfferState.CompletedSell else OfferState.CompletedBuy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Offer

        if (id != other.id) return false
        if (amount != other.amount) return false
        if (price != other.price) return false
        if (completed != other.completed) return false
        if (coins != other.coins) return false
        if (item != other.item) return false
        if (state != other.state) return false
        if (account != other.account) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + amount
        result = 31 * result + price
        result = 31 * result + completed
        result = 31 * result + coins
        result = 31 * result + item.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + account.hashCode()
        return result
    }

    companion object {

        fun ConfigReader.readOffer(id: Int, item: String, sell: Boolean): Offer {
            var amount = 0
            var price = 0
            var state: OfferState = if (sell) OfferState.PendingSell else OfferState.PendingBuy
            var lastActive: Long = System.currentTimeMillis()
            var completed = 0
            var coins = 0
            var account = ""
            while (nextPair()) {
                when (val key = key()) {
                    "amount" -> amount = int()
                    "price" -> price = int()
                    "state" -> state = OfferState.valueOf(string())
                    "last_active" -> lastActive = long()
                    "completed" -> completed = int()
                    "coins" -> coins = int()
                    "account" -> account = string()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                }
            }
            return Offer(
                id = id,
                item = item,
                amount = amount,
                price = price,
                state = state,
                lastActive = lastActive,
                completed = completed,
                coins = coins,
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
            if (offer.coins != 0) {
                writePair("coins", offer.coins)
            }
            if (offer.account != "") {
                writePair("account", offer.account)
            }
        }

        val EMPTY = Offer()
    }
}