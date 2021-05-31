package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.world.activity.skill.mining.ore.Sandstone

object SandstoneRock : Rock {
    override val ores: List<Sandstone> = listOf(
        Sandstone.Sandstone_10kg,
        Sandstone.Sandstone_5kg,
        Sandstone.Sandstone_2kg,
        Sandstone.Sandstone_1kg
    )
    override val level = 35
    override val respawnDelay: Int = 7
    override val id: String = "sandstone_rocks"
}