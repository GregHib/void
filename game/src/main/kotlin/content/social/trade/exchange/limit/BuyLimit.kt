package content.social.trade.exchange.limit

/**
 * [amount] of an item a player has bought, last updated [timestamp], to track [BuyLimits].
 */
data class BuyLimit(
    var amount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
)
