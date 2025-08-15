package world.gregs.voidps.engine.data.exchange

class PriceHistory(
    val day: MutableMap<Long, Aggregate> = mutableMapOf(),
    val week: MutableMap<Long, Aggregate> = mutableMapOf(),
    val month: MutableMap<Long, Aggregate> = mutableMapOf(),
    val year: MutableMap<Long, Aggregate> = mutableMapOf(),
)
