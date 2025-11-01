package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Jaraah : Script {

    init {
        npcOperate("Talk-to", "jaraah") { (target) ->
            player<Happy>("Hi!")
            npc<Frustrated>("What? Can't you see I'm busy?!")
            choice {
                option<Uncertain>("Can you heal me?") {
                    heal(target)
                }
                option<Uncertain>("You must see some gruesome things?") {
                    npc<Frustrated>("It's a gruesome business and with the tools they give me it gets more gruesome before it gets better!")
                    player<Chuckle>("Really?")
                    npc<Chuckle>("It beats being stuck in the monastery!")
                }
                option<Uncertain>("Why do they call you 'The Butcher'?") {
                    npc<Chuckle>("'The Butcher'?")
                    npc<Frustrated>("Ha!")
                    npc<Frustrated>("Would you like me to demonstrate?")
                    player<Surprised>("Er...I'll give it a miss, thanks.")
                }
            }
        }

        npcOperate("Heal", "jaraah") { (target) ->
            heal(target)
        }
    }
}
