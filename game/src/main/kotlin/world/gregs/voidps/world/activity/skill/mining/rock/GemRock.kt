package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.world.activity.skill.mining.ore.Gemstone

object GemRock : Rock {
    override val ores: List<Gemstone> = Gemstone.values().toList()
    override val level = 40
    override val respawnDelay: Int = 99
    override val id: String = "gem"
}