package world.gregs.voidps.engine.data.definition.data

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.ConfigReader

data class Tanning(
    val prices: List<TanPrice> = emptyList()
) {
    data class TanPrice(val id: String, val price: Int)

    companion object {
        val EMPTY = Tanning()
        operator fun invoke(reader: ConfigReader): Tanning {
            val prices = ObjectArrayList<TanPrice>(1)
            while (reader.nextElement()) {
                var id = ""
                var price = 0
                while (reader.nextEntry()) {
                    when (val key = reader.key()) {
                        "id" -> id = reader.string()
                        "price" -> price = reader.int()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                    }
                }
                prices.add(TanPrice(id, price))
            }
            return Tanning(prices)
        }
    }
}