package world.gregs.voidps.world.activity.skill.mining.ore

enum class Gemstone(
    override val xp: Double,
    override val chance: IntRange
) : Ore {
    Uncut_opal(65.0, 120..120),
    Uncut_jade(65.0, 60..60),
    Uncut_red_topaz(65.0, 30..30),
    Uncut_sapphire(65.0, 18..18),
    Uncut_emerald(65.0, 10..10),
    Uncut_ruby(65.0, 10..10),
    Uncut_diamond(65.0, 8..8);

    override val id: String = name.toLowerCase()

}