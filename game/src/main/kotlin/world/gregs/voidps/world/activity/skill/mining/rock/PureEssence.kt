package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.world.activity.skill.mining.ore.RegularOre

object PureEssence : Rock {
    override val ores: List<RegularOre> = listOf(
        RegularOre.PureEssence
    )
    override val level = 30
    override val respawnDelay: Int = -1
    override val id: String = "rune_essence"
}