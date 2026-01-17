package content.area.kandarin.seers_village

import content.entity.npc.shop.openShop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level

class Ignatius : Script {

    init {
        npcOperate("Trade", "armour_salesman") {
            openShop(
                "ignatiuss_hot_deals${
                    when {
                        Skill.all.any { it != Skill.Firemaking && levels.getMax(it) == Level.MAX_LEVEL } -> "_trimmed"
                        levels.getMax(Skill.Firemaking) == Level.MAX_LEVEL -> "_skillcape"
                        else -> ""
                    }
                }",
            )
        }
    }
}
