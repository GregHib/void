package world.gregs.voidps.world.activity.skill.mining.ore

enum class RegularOre(
    override val xp: Double,
    override val chance: IntRange
) : Ore {
    Clay(5.0, 75..300),
    Rune_Essence(5.0, 200..400),
    Copper_Ore(17.5, 40..500),
    Tin_Ore(17.5, 40..500),
    Limestone(26.5, 25..200),
    Blurite_Ore(17.5, 30..375),
    Iron_Ore(35.0, 110..350),
    Silver_Ore(40.0, 25..200),
    Pure_Essence(5.0, 150..350),
    Coal(50.0, 20..150),
    Gold_Ore(65.0, 7..75),
    Mithril_Ore(80.0, 10..75),
    Adamantite_Ore(95.0, 5..55),
    Runite_Ore(125.0, 1..18);

    override val id: String = name.toLowerCase()

}