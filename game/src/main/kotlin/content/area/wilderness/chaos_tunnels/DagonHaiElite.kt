package content.area.wilderness.chaos_tunnels

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC

class DagonHaiElite : Script {
    init {
        combatAttack { (target) ->
            if (target is NPC && (target.id == "surok_magis_attack" || target.id == "dagonhai_elite_attack")) {
                gfx("surok_shield")
            }
        }
    }
}
