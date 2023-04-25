package world.gregs.voidps.engine.data.definition.data

data class Jewellery(
    val level: Int = 1,
    val xp: Double = 0.0
) {

    companion object {

        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>) = Jewellery(
            level = map["level"] as? Int ?: Spinning.EMPTY.level,
            xp = map["xp"] as? Double ?: Spinning.EMPTY.xp,
        )

        val EMPTY = Jewellery()
    }
}