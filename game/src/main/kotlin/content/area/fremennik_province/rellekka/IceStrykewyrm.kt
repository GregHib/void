package content.area.fremennik_province.rellekka

import content.area.kandarin.feldip_hills.JungleStrykewyrm
import content.entity.combat.hit.directHit
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message

class IceStrykewyrm : Script {
    init {
        npcOperate("Investigate", "mound_ice_strykewyrm") { (target) ->
            JungleStrykewyrm.investigate(this, target, "ice_strykewyrm")
        }

        npcAttack("ice_strykewyrm", "dig") { target ->
            JungleStrykewyrm.burrow(this, target)
        }

        npcCombatDamage("ice_strykewyrm") {
            if (it.spell.startsWith("ice_")) {
                directHit(it.damage, "healed")
            }
        }

        objTeleportTakeOff("Enter", "ice_strykewyrm_cave_entrance") { _, _ ->
            message("You follow the cave down deeper.")
            0
        }
    }
}
