package content.area.kharidian_desert.al_kharid

import content.area.kandarin.feldip_hills.JungleStrykewyrm
import content.skill.slayer.slayerTask
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class DesertStrykewyrm : Script {
    init {
        npcOperate("Investigate", "mound_desert_strykewyrm") { (target) ->
            JungleStrykewyrm.investigate(this, target, "desert_strykewyrm")
        }

        npcAttack("desert_strykewyrm", "dig") { target ->
            JungleStrykewyrm.burrow(this, target)
        }

        canAttack("mound_desert_strykewyrm") {
            if (slayerTask != "desert_strykewyrm") {
                message("You need to have strykewyrm assigned as a task in order to fight them.")
                false
            } else {
                true
            }
        }
    }
}
