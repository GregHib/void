package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.world.activity.skill.mining.ore.Granite

object GraniteRock : Rock {
    override val ores: List<Granite> = Granite.values().reversed()
    override val level = 45
    override val respawnDelay: Int = 8
    override val id: String = "granite"
}