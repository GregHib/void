package content.area.kharidian_desert.al_kharid

import content.area.kandarin.feldip_hills.JungleStrykewyrm
import world.gregs.voidps.engine.Script

class DesertStrykewyrm : Script {
    init {
        npcOperate("Investigate", "mound_desert_strykewyrm") { (target) ->
            JungleStrykewyrm.investigate(this, target, "desert_strykewyrm")
        }

        npcAttack("desert_strykewyrm", "dig") { target ->
            JungleStrykewyrm.burrow(this, target)
        }
    }
}
