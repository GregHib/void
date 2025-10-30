package content.area.misthalin.lumbridge.combat_hall

import content.entity.combat.attackers
import content.entity.combat.combatPrepare
import content.skill.melee.weapon.fightStyle
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class CombatDummy : Script {

    init {
        npcLevelChanged(Skill.Constitution, "melee_dummy,magic_dummy") { _, _, to ->
            if (to <= 10) {
                levels.clear()
                for (attacker in attackers) {
                    attacker.mode = EmptyMode
                }
            }
        }

        combatPrepare { player ->
            if (target is NPC && target.id == "magic_dummy" && player.fightStyle != "magic") {
                player.message("You can only use Magic against this dummy.")
                cancel()
            } else if (target is NPC && target.id == "melee_dummy" && player.fightStyle != "melee") {
                player.message("You can only use Melee against this dummy.")
                cancel()
            }
        }
    }
}
