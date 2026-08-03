package content.area.kandarin.seers_village

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.skillcapeShop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class Ignatius : Script {

    init {
        npcOperate("Trade", "ignatius") {
            openShop(skillcapeShop(Skill.Firemaking, "ignatiuss_hot_deals"))
        }
    }
}
