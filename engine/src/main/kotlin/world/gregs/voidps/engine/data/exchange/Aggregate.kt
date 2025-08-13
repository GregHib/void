package world.gregs.voidps.engine.data.exchange
/**
 * An aggregation of information of offer's over a certain [TimeFrame]
 * @param open price at the start of the timeframe
 * @param high highest price during the timeframe
 * @param low lowest price during the timeframe
 * @param close final price at the end of the timeframe
 * @param volume total number of items traded during the timeframe
 * @param count total number of trades during the timeframe
 * @param averageHigh running average of all the highest prices during the timeframe
 * @param averageLow running average of all the lowest prices during the timeframe
 * @param volumeHigh running total of items sold at highest prices during the timeframe
 * @param volumeLow running total of items sold at lowest prices during the timeframe
 */
data class Aggregate(
    var open: Int = 0,
    var high: Int = 0,
    var low: Int = Int.MAX_VALUE,
    var close: Int = 0,
    var volume: Long = 0,
    var count: Int = 0,
    var averageHigh: Double = 0.0,
    var averageLow: Double = 0.0,
    var volumeHigh: Long = 0,
    var volumeLow: Long = 0,
) {
    fun update(price: Int, amount: Int) {
        high = maxOf(high, price)
        low = minOf(low, price)
        close = price
        volume += amount
        count++

        val midpoint = (high + low) / 2

        if (price >= midpoint) {
            // Update running average for high prices
            averageHigh = ((averageHigh * volumeHigh) + (price * amount)) / (volumeHigh + amount)
            volumeHigh += amount
        } else {
            // Update running average for low prices
            averageLow = ((averageLow * volumeLow) + (price * amount)) / (volumeLow + amount)
            volumeLow += amount
        }
    }
}