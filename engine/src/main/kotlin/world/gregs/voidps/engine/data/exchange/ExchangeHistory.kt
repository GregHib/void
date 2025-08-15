package world.gregs.voidps.engine.data.exchange

/**
 * @param item id
 * @param amount completed
 * @param coins > 0 bought at, < 0 sold at
 */
data class ExchangeHistory(
    val item: String = "",
    val amount: Int = 0,
    val coins: Int = 0,
) {
    constructor(offer: ExchangeOffer) : this(offer.item, offer.completed, if (offer.sell) -offer.coins else offer.coins)
}
