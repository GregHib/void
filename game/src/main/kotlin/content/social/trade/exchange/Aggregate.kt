package content.social.trade.exchange

/**
 * An aggregation of information of [Offer]'s over a certain [TimeFrame]
 */
data class Aggregate(
    val open: Int = 0,
    var high: Int = 0,
    var low: Int = Int.MAX_VALUE,
    var close: Int = 0,
    var volume: Long = 0,
    var count: Int = 1
) {
    fun update(price: Int, amount: Int) {
        high = maxOf(high, price)
        low = minOf(low, price)
        close = price
        volume += amount
        count++
    }
}