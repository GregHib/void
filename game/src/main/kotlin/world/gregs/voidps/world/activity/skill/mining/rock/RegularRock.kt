package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.world.activity.skill.mining.ore.Gems
import world.gregs.voidps.world.activity.skill.mining.ore.RegularOre

enum class RegularRock(
    ore: RegularOre,
    override val level: Int,
    override val respawnDelay: Int
) : Rock {
    Clay(RegularOre.Clay, 1, 2),
    Copper(RegularOre.CopperOre, 1, 4),
    Tin(RegularOre.TinOre, 1, 4),
    Blurite(RegularOre.BluriteOre, 10, 42),
    Iron(RegularOre.IronOre, 15, 9),
    Silver(RegularOre.SilverOre, 20, 100),
    Coal(RegularOre.Coal, 30, 50),
    Gold(RegularOre.GoldOre, 40, 100),
    Mithril(RegularOre.MithrilOre, 55, 200),
    Adamantite(RegularOre.AdamantiteOre, 70, 400),
    Runite(RegularOre.RuniteOre, 85, 1200),
    ;

    override val ores = listOf(Gems, ore)
    override val id: String = name.toLowerCase()

    companion object {
        fun get(gameObject: GameObject): RegularRock? {
            val id = gameObject.stringId
            return values().firstOrNull { id.startsWith("${it.name}_rocks", true) }
        }
    }
}