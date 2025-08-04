package content.social.trade.exchange

import java.util.concurrent.TimeUnit

/**
 * Historical aggregated data on every item
 */
class ExchangeHistory(
    val history: MutableMap<String, ItemHistory> = mutableMapOf(),
) {
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
}