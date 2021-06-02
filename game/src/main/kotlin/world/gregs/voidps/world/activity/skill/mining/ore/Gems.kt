package world.gregs.voidps.world.activity.skill.mining.ore

object Gems : Ore {

    override val xp: Double = 0.0
    override val chance: IntRange = 1..1
    override val id: String
        get() = gems.random()

    private val gems = setOf(
        "uncut_sapphire",
        "uncut_emerald",
        "uncut_ruby",
        "uncut_diamond"
    )

}