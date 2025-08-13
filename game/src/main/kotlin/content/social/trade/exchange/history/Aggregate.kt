package content.social.trade.exchange.history

import world.gregs.config.*

/**
 * An aggregation of information of offer's over a certain [TimeFrame]
 * @param open price at the start of the timeframe
 * @param high highest price during the timeframe
 * @param low lowest price during the timeframe
 * @param close final price at the end of the timeframe
 * @param volume total number of items traded during the timeframe
 * @param count total number of trades during the timeframe
 * @param highAverage running average of all the highest prices during the timeframe
 * @param lowAverage running average of all the lowest prices during the timeframe
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
    var highAverage: Double = 0.0,
    var lowAverage: Double = 0.0,
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
            highAverage = ((highAverage * volumeHigh) + (price * amount)) / (volumeHigh + amount)
            volumeHigh += amount
        } else {
            // Update running average for low prices
            lowAverage = ((lowAverage * volumeLow) + (price * amount)) / (volumeLow + amount)
            volumeLow += amount
        }
    }

    companion object {
        fun ConfigReader.readAggregate(): Aggregate {
            var open = 0
            var high = 0
            var low = Int.MAX_VALUE
            var close = 0
            var volume = 0L
            var count = 0
            var highAverage = 0.0
            var lowAverage = 0.0
            var volumeHigh = 0L
            var volumeLow = 0L
            var index = 0
            while (nextElement()) {
                when (index++) {
                    0 -> open = int()
                    1 -> high = int()
                    2 -> low = int()
                    3 -> close = int()
                    4 -> volume = long()
                    5 -> count = int()
                    6 -> highAverage = double()
                    7 -> lowAverage = double()
                    8 -> volumeHigh = long()
                    9 -> volumeLow = long()
                }
            }
            return Aggregate(
                open = open,
                high = high,
                low = low,
                close = close,
                volume = volume,
                count = count,
                highAverage = highAverage,
                lowAverage = lowAverage,
                volumeHigh = volumeHigh,
                volumeLow = volumeLow
            )
        }

        fun ConfigWriter.write(aggregate: Aggregate) {
            list(10) { index ->
                when (index) {
                    0 -> writeValue(aggregate.open)
                    1 -> writeValue(aggregate.high)
                    2 -> writeValue(aggregate.low)
                    3 -> writeValue(aggregate.close)
                    4 -> writeValue(aggregate.volume)
                    5 -> writeValue(aggregate.count)
                    6 -> writeValue(aggregate.highAverage)
                    7 -> writeValue(aggregate.lowAverage)
                    8 -> writeValue(aggregate.volumeHigh)
                    9 -> writeValue(aggregate.volumeLow)
                }
            }
        }
    }
}