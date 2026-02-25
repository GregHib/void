package content.social.trade.exchange.history

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.exchange.PriceHistory
import world.gregs.voidps.engine.timer.epochMilliseconds
import java.util.concurrent.TimeUnit
import kotlin.collections.MutableMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.getOrPut
import kotlin.collections.iterator
import kotlin.collections.maxByOrNull
import kotlin.collections.mutableMapOf
import kotlin.collections.set

/**
 * Historical aggregated data on every item
 * https://web.archive.org/web/20210430192551/https://secure.runescape.com/m%3Dforum/sl%3D0/forums?98,99,806,63785618
 */
class ExchangeHistory(val history: MutableMap<String, PriceHistory> = mutableMapOf()) {
    private val marketPrices = mutableMapOf<String, Int>()

    fun record(item: String, amount: Int, price: Int) {
        val history = history.getOrPut(item) { PriceHistory() }
        val timestamp = epochMilliseconds()
        history.record(timestamp, price, amount)
    }

    fun clean() {
        val timestamp = epochMilliseconds()
        for (item in history.values) {
            item.clean(timestamp)
        }
    }

    fun marketPrice(item: String): Int {
        val price = marketPrices[item]
        if (price == null) {
            val definition = ItemDefinitions.get(item)
            return definition["price", definition.cost]
        }
        return price
    }

    fun calculatePrices() {
        val timestamp = epochMilliseconds()
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

    fun clear() {
        history.clear()
        marketPrices.clear()
    }
}
