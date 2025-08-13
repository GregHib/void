package world.gregs.voidps.engine.data.exchange

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
)