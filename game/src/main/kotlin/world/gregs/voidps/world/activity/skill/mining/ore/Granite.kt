package world.gregs.voidps.world.activity.skill.mining.ore

enum class Granite(
    override val xp: Double,
    override val chance: IntRange
) : Ore {
    Granite_500g(50.0, 16..100),
    Granite_2kg(60.0, 8..75),
    Granite_5kg(75.0, 6..64);

    override val id: String = name.toLowerCase()

}