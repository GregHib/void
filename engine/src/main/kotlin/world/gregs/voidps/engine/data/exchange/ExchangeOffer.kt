package world.gregs.voidps.engine.data.exchange

/**
 * Grand Exchange Offer
 * @param id unique
 * @param item id
 * @param amount to buy or sell
 * @param price sold or buying at
 * @param state type of offer
 * @param completed number purchased or sold so far
 * @param coins total received or refunded if bought below offer price
 */
data class ExchangeOffer(
    val id: Int = 0,
    val item: String = "",
    val amount: Int = 0,
    val price: Int = 0,
    var state: OfferState = OfferState.PendingBuy,
    var completed: Int = 0,
    var coins: Int = 0,
) {

    fun isEmpty(): Boolean = id == 0

    val sell: Boolean
        get() = state.sell

    fun open() {
    }

    fun open(account: String): OpenOffer {
        val remaining = amount - completed
        state = if (state.sell) OfferState.OpenSell else OfferState.OpenBuy
        return OpenOffer(id, remaining, coins, account)
    }

    fun cancel() {
        state = if (state.sell) OfferState.CompletedSell else OfferState.CompletedBuy
    }

    companion object {
        val EMPTY = ExchangeOffer()
    }
}
