package content.area.misthalin.draynor_village

import content.entity.combat.attackers
import content.entity.combat.damageDealers
import content.entity.effect.clearTransform
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.queue.queue

class ShadyStranger : Script {
    private val strangers = "shady_stranger,shady_stranger_2,suspicious_outsider"
    private val escapeHealth = 100

    init {
        npcOperate("Talk-to", strangers) { (target) ->
            player<Quiz>("Hello, what are you doing here?")
            npc<Shifty>("Err, nothing much, I'm just here on business. Heard Draynor's a nice place to visit.")
        }

        npcCombatDamage(strangers) { attack ->
            if (queue.contains("shady_escape") || attack.damage <= 0) {
                return@npcCombatDamage
            }
            val currentHealth = levels.get(Skill.Constitution)
            if (currentHealth > escapeHealth && currentHealth - attack.damage <= escapeHealth) {
                set("shady_escape_pending", true)
                escape(this)
            }
        }

        npcCanDie(strangers) {
            !get("shady_escape_pending", false)
        }
    }

    private fun escape(npc: NPC) {
        for (attacker in npc.attackers) {
            attacker.mode = EmptyMode
        }
        npc.attackers.clear()
        npc.damageDealers.clear()
        npc.mode = PauseMode
        npc.steps.clear()
        npc.clearWatch()
        npc.queue("shady_escape") {
            npc.say("Uh oh, time to go!")
            npc.anim("teleport_modern")
            npc.gfx("teleport_modern")
            npc.sound("teleport")
            npc.delay(3)
            npc.queue.clear()
            npc.attackers.clear()
            npc.damageDealers.clear()
            npc.softTimers.stopAll()
            npc.clearTransform()
            npc.hide = true
            clear("shady_escape_pending")
            respawn(get("respawn_delay", 50))
        }
    }
}
