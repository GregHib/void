package world.gregs.voidps.engine.data.definition.data

data class Pottery(
    val list: Map<String, Ceramic> = emptyMap()
) {

    data class Ceramic(
        val level: Int = 1,
        val xp: Double = 0.0
    ) {
        companion object {
            operator fun invoke(map: Map<String, Any>) = Ceramic(
                level = map["level"] as? Int ?: EMPTY.level,
                xp = map["xp"] as? Double ?: EMPTY.xp,
            )

            val EMPTY = Ceramic()
        }
    }

    companion object {
        val EMPTY = Pottery()
    }
}