package content.social.trade.exchange

data class BuyLimit(
    var amount: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
)