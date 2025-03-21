package world.gregs.voidps.engine.data.definition.data

import world.gregs.config.ConfigReader

data class Weaving(
    val amount: Int = 1,
    val to: String = "",
    val level: Int = 1,
    val xp: Double = 0.0
) {

    companion object {
        operator fun invoke(reader: ConfigReader): Weaving {
            var amount = 1
            var to = ""
            var level = 1
            var xp = 0.0
            while (reader.nextEntry()) {
                when (val key = reader.key()) {
                    "amount" -> amount = reader.int()
                    "to" -> to = reader.string()
                    "level" -> level = reader.int()
                    "xp" -> xp = reader.double()
                    else -> throw IllegalArgumentException("Unexpected key: '$key' ${reader.exception()}")
                }
            }
            return Weaving(amount = amount, to = to, level = level, xp = xp)
        }

        val EMPTY = Weaving()
    }
}