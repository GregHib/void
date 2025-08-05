package content.social.trade.exchange.history

import content.social.trade.exchange.history.Aggregate.Companion.readAggregate
import content.social.trade.exchange.history.Aggregate.Companion.write
import world.gregs.config.*
import java.util.concurrent.TimeUnit

/**
 * Aggregated price and volume history for an item
 * ~68 bytes per aggregate
 */
class ItemHistory(
    val day: MutableMap<Long, Aggregate> = mutableMapOf(),
    val week: MutableMap<Long, Aggregate> = mutableMapOf(),
    val month: MutableMap<Long, Aggregate> = mutableMapOf(),
    val year: MutableMap<Long, Aggregate> = mutableMapOf(),
) {
    fun record(timestamp: Long, price: Int, amount: Int) {
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
     * Keeps history limited to 288 [day], 168 [week], 120 [month] and 365 [year] aggregates
     */
    fun clean(timestamp: Long) {
        val dayAgo = TimeFrame.Day.start(timestamp - TimeUnit.DAYS.toMillis(1))
        day.entries.removeIf { it.key < dayAgo }
        val weekAgo = TimeFrame.Week.start(timestamp - TimeUnit.DAYS.toMillis(7))
        week.entries.removeIf { it.key < weekAgo }
        val monthAgo = TimeFrame.Month.start(timestamp - TimeUnit.DAYS.toMillis(30))
        month.entries.removeIf { it.key < monthAgo }
        val yearAgo = TimeFrame.Year.start(timestamp - TimeUnit.DAYS.toMillis(365))
        year.entries.removeIf { it.key < yearAgo }
    }

    companion object {

        fun ConfigReader.readHistory(): ItemHistory {
            val history = ItemHistory()
            while (nextSection()) {
                val section = section()
                val aggregates: MutableMap<Long, Aggregate> = when (section) {
                    "day" -> history.day
                    "week" -> history.week
                    "month" -> history.month
                    "year" -> history.year
                    else -> return history
                }
                while (nextPair()) {
                    val timestamp = key().toLong()
                    aggregates[timestamp] = readAggregate()
                }
            }
            return history
        }

        fun ConfigWriter.write(history: ItemHistory) {
            writeSection("day")
            write(history.day)
            writeSection("week")
            write(history.week)
            writeSection("month")
            write(history.month)
            writeSection("year")
            write(history.year)
        }

        private fun ConfigWriter.write(history: MutableMap<Long, Aggregate>) {
            for ((timestamp, aggregate) in history) {
                writeKey(timestamp.toString())
                write(aggregate)
            }
        }
    }
}