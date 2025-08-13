package world.gregs.voidps.engine.data.exchange

/**
 * @param item id
 * @param price > 0 sold at, < 0 bought at
 * @param amount completed
 */
data class ExchangeHistory(
    val item: String = "",
    val price: Int = 0,
    val amount: Int = 0,
)