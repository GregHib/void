package world.gregs.voidps.world.activity.skill.mining.ore

enum class Sandstone(
    override val xp: Double,
    override val chance: IntRange
) : Ore {
    Sandstone_1kg(30.0, 25..200),
    Sandstone_2kg(40.0, 16..100),
    Sandstone_5kg(50.0, 8..75),
    Sandstone_10kg(60.0, 4..50);

    override val id: String = name.toLowerCase()

}