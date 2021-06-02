package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.world.activity.skill.mining.ore.Sandstone

object SandstoneRock : Rock {
    override val ores: List<Sandstone> = Sandstone.values().reversed()
    override val level = 35
    override val respawnDelay: Int = 7
    override val id: String = "sandstone"
}