package world.gregs.voidps.world.activity.skill.fishing.fish

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

enum class Junk(
    override val level: Int,
    override val xp: Double,
    override val chance: IntRange
) : Catch {
    Oyster(16, 10.0, 3..7),
    Casket(16, 0.0, 1..2),
    Seaweed(16, 1.0, 10..10),
    LeatherGloves(16, 1.0, 10..10),
    LeatherBoots(16, 1.0, 10..10);

    override val id: String = name.toTitleCase().toUnderscoreCase()
}