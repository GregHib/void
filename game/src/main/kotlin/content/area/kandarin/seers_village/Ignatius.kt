package content.area.kandarin.seers_village

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.event.Script

@Script
class Ignatius {
    init {
        npcOperate("Trade", "armour_salesman") {
            player.openShop(
                "ignatiuss_hot_deals${
                    when {
                        Skill.all.any { it != Skill.Firemaking && player.levels.getMax(it) == Level.MAX_LEVEL } -> "_trimmed"
                        player.levels.getMax(Skill.Firemaking) == Level.MAX_LEVEL -> "_skillcape"
                        else -> ""
                    }
                }",
            )
        }
    }
}
