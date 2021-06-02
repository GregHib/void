package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.world.activity.skill.mining.ore.RegularOre

object RuneEssence : Rock {
    override val ores: List<RegularOre> = listOf(
        RegularOre.Rune_Essence
    )
    override val level = 1
    override val respawnDelay: Int = -1
    override val id: String = "rune_essence"
}