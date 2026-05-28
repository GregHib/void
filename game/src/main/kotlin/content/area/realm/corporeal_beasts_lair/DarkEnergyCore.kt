package content.area.realm.corporeal_beasts_lair

import content.entity.proj.shoot
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.CLIENT_TICKS

class DarkEnergyCore : Script {
    init {
        npcCombatAttack("dark_energy_core") {
            val corp = NPCs.findOrNull(tile.regionLevel, "corporeal_beast") ?: return@npcCombatAttack
            corp.levels.restore(Skill.Constitution, it.damage)
        }

        npcAttack("dark_energy_core", "hop") { target ->
            val tile = target.tile
            face(target)
            anim("dark_core_take_off")
            queue("hop", 1) {
                hide = true
                val time = this@npcAttack.tile.shoot("dark_energy_core_travel", tile)
                delay(CLIENT_TICKS.toTicks(time))
                tele(tile)
                hide = false
            }
        }

        npcVariableSet("poison", "dark_energy_core") { _, _, to ->
            set("attack_speed", if (to == 0) 2 else 12)
        }
    }
}
