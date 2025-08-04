package content.social.trade.exchange

/**
 * Aggregated price and volume history for an item
 */
class ItemHistory(
    val day: MutableMap<Long, Aggregate> = mutableMapOf(),
    val week: MutableMap<Long, Aggregate> = mutableMapOf(),
    val month: MutableMap<Long, Aggregate> = mutableMapOf(),
    val year: MutableMap<Long, Aggregate> = mutableMapOf(),
) {
    fun record(price: Int, amount: Int) {
        val timestamp = System.currentTimeMillis()
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

    fun clean(yearAgoMillis: Long) {
        day.entries.removeIf { it.key < yearAgoMillis }
        week.entries.removeIf { it.key < yearAgoMillis }
        month.entries.removeIf { it.key < yearAgoMillis }
        year.entries.removeIf { it.key < yearAgoMillis }
    }
}