package world.gregs.voidps.engine.data.definition.data

/**
 * @param onceLit the item that is given once the player lights a light source
 * @param onceExtinguish the item that is given once the player extinguishes the light source
 * @param level the firemaking level required to light the light source
 */
data class LightSources(
    val onceLit: String = "",
    val onceExtinguish: String = "",
    val level: Int = -1
) {
    companion object {

        operator fun invoke(map: Map<String, Any>) = LightSources(
            onceLit = map["once_lit"] as? String ?: EMPTY.onceLit,
            onceExtinguish = map["once_extinguish"] as? String ?: EMPTY.onceExtinguish,
            level = map["level"] as? Int ?: EMPTY.level,
        )

        val EMPTY = LightSources()
    }
}