package world.gregs.voidps.engine.data.definition.data

import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.config.ConfigReader
import world.gregs.voidps.engine.entity.item.Item

/**
 * @param level mining level required to smelt
 * @param xp experience for successful smelting
 * @param chance of success out of 255
 * @param items required items to smelt
 */
data class Smelting(
    val level: Int = 0,
    val xp: Double = 0.0,
    val chance: Int = 255,
    val items: List<Item> = emptyList(),
    val message: String = "",
) {
    companion object {
        operator fun invoke(reader: ConfigReader): Smelting {
            var level = 0
            var xp = 0.0
            var chance = 255
            val items = ObjectArrayList<Item>(0)
            var message = ""
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    "chance" -> chance = reader.int()
                    "items" -> {
                        while (reader.nextElement()) {
                            var item = ""
                            var amount = 1
                            while (reader.nextEntry()) {
                                when (val itemKey = reader.key()) {
                                    "id" -> item = reader.string()
                                    "amount" -> amount = reader.int()
                                    else -> throw IllegalArgumentException("Unexpected key: '$itemKey' ${reader.exception()}")
                                }
                            }
                            items.add(Item(item, amount))
                        }
                    }
                    "message" -> message = reader.string()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Smelting(level = level, xp = xp, chance = chance, items = items, message = message)
        }

        val EMPTY = Smelting()
    }
}
