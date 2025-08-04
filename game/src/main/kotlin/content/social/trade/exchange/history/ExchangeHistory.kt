package content.social.trade.exchange.history

import java.util.concurrent.TimeUnit

/**
 * Historical aggregated data on every item
 * https://web.archive.org/web/20210430192551/https://secure.runescape.com/m%3Dforum/sl%3D0/forums?98,99,806,63785618
 */
class ExchangeHistory(
    private val history: MutableMap<String, ItemHistory> = mutableMapOf(),
) {
    private val marketPrices = mutableMapOf<String, Int>()

    fun record(item: String, amount: Int, price: Int) {
        val history = history.getOrPut(item) { ItemHistory() }
        history.record(price, amount)
    }

    fun clean() {
        val timestamp = System.currentTimeMillis()
        val yearAgo = timestamp - TimeUnit.MILLISECONDS.toDays(366)
        for (item in history.values) {
            item.clean(yearAgo)
        }
    }

    fun marketPrice(item: String): Int? = marketPrices[item]

    fun calculatePrices() {
        val timestamp = System.currentTimeMillis()
        for ((item, history) in history) {
            val previous = marketPrices[item]
            val (time, newest) = history.day.maxByOrNull { it.key } ?: continue
            val age = timestamp - time
            if (TimeUnit.MILLISECONDS.toDays(age) <= 1) {
                continue
            }
            var next = previous ?: newest.high
            if (previous != null) {
                next += (newest.high - previous).coerceIn(-(previous / 20), previous / 20) // 5 percent max change
            }
            marketPrices[item] = next
        }
    }

}