package content.social.trade.exchange.limit

data class BuyLimit(
    var amount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
)