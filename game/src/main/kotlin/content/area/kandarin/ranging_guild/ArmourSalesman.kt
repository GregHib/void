package content.area.kandarin.ranging_guild

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.skillcapeShop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class ArmourSalesman : Script {

    init {
        npcOperate("Trade", "armour_salesman") {
            openShop(skillcapeShop(Skill.Ranged, "aarons_archery_appendages"))
        }
    }
}
