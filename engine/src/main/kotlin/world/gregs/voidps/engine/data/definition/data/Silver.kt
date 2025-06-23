package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

/**
 * @param name interface override
 * @param item the silver item to craft
 * @param level required to attempt to craft
 * @param xp experience from successfully crafting
 * @param quest quest required to display this item
 */
data class Silver(
    val name: String? = null,
    val item: String = "",
    val amount: Int = 1,
    val xp: Double = 0.0,
    val level: Int = 1,
    val quest: String? = null,
) {
    companion object {

        operator fun invoke(reader: ConfigReader): Silver {
            var name: String? = null
            var item = ""
            var amount = 1
            var xp = 0.0
            var level = 1
            var quest: String? = null
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "name" -> name = reader.string()
                    "item" -> item = reader.string()
                    "amount" -> amount = reader.int()
                    "xp" -> xp = reader.double()
                    "level" -> level = reader.int()
                    "quest" -> quest = reader.string()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Silver(name = name, item = item, amount = amount, xp = xp, level = level, quest = quest)
        }

        val EMPTY = Silver()
    }
}
