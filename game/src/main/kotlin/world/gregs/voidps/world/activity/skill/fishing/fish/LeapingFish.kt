package world.gregs.voidps.world.activity.skill.fishing.fish

import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.engine.utility.toUnderscoreCase

enum class LeapingFish(
    override val level: Int,
    override val xp: Double,
    override val chance: IntRange,
) : Catch {
    LeapingTrout(48, 50.0, 32..192),
    LeapingSalmon(58, 70.0, 16..96),
    LeapingSturgeon(70, 80.0, 8..64);

    override val id: String = name.toTitleCase().toUnderscoreCase()
}