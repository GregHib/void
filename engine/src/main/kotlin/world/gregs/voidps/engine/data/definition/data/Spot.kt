package world.gregs.voidps.engine.data.definition.data

data class Spot(
    val tackle: List<String> = emptyList(),
    val bait: Map<String, List<String>> = emptyMap()
) {

    companion object {
        @Suppress("UNCHECKED_CAST")
        operator fun invoke(map: Map<String, Any>): Spot {
            return Spot(
                tackle = map["items"] as List<String>,
                bait = map["bait"] as Map<String, List<String>>
            )
        }

        val EMPTY = Spot()
    }
}