package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class SurgeonGeneralTafani : Script {

    init {
        npcOperate("Talk-to", "surgeon_general_tafani") { (target) ->
            player<Happy>("Hi!")
            npc<Happy>("Hi. How can I help?")
            menu(target)
        }

        npcOperate("Heal", "surgeon_general_tafani") { (target) ->
            heal(target)
        }
    }

    suspend fun Player.menu(target: NPC) {
        choice {
            option<Confused>("Can you heal me?") {
                heal(target)
            }
            fighters()
            often()
            option<Quiz>("Can you tell me about your cape?") {
                npc<Happy>("Certainly! Skillcapes are a symbol of achievement. Only people who have mastered a skill and reached level 99 can get their hands on them and gain the benefits they carry.")
                npc<Idle>("The Cape of Constitution doubles the speed of your constitution replenishing when worn. Is there anything else I can help you with?")
                menu(target)
            }
        }
    }
}
