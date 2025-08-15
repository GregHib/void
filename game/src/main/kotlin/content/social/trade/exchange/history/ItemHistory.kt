package content.social.trade.exchange.history

import world.gregs.voidps.engine.data.exchange.Aggregate
import world.gregs.voidps.engine.data.exchange.PriceHistory
import java.util.concurrent.TimeUnit

/**
 * Aggregated price and volume history for an item
 * ~68 bytes per aggregate
 */
fun PriceHistory.record(timestamp: Long, price: Int, amount: Int) {
    record(timestamp, TimeFrame.Day, day, price, amount)
    record(timestamp, TimeFrame.Week, week, price, amount)
    record(timestamp, TimeFrame.Month, month, price, amount)
    record(timestamp, TimeFrame.Year, year, price, amount)
}

private fun record(timestamp: Long, frame: TimeFrame, map: MutableMap<Long, Aggregate>, price: Int, amount: Int) {
    val start = frame.start(timestamp)
    val current = map.getOrPut(start) { Aggregate(open = price) }
    current.update(price, amount)
}

/**
 * Keeps history limited to 288 [TimeFrame.Day], 168 [TimeFrame.Week], 120 [TimeFrame.Month] and 365 [TimeFrame.Year] aggregates
 */
fun PriceHistory.clean(timestamp: Long) {
    val dayAgo = TimeFrame.Day.start(timestamp - TimeUnit.DAYS.toMillis(1))
    day.entries.removeIf { it.key < dayAgo }
    val weekAgo = TimeFrame.Week.start(timestamp - TimeUnit.DAYS.toMillis(7))
    week.entries.removeIf { it.key < weekAgo }
    val monthAgo = TimeFrame.Month.start(timestamp - TimeUnit.DAYS.toMillis(30))
    month.entries.removeIf { it.key < monthAgo }
    val yearAgo = TimeFrame.Year.start(timestamp - TimeUnit.DAYS.toMillis(365))
    year.entries.removeIf { it.key < yearAgo }
}
