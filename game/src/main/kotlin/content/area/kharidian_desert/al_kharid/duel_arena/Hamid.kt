package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Hamid : Script {

    init {
        npcOperate("Talk-to", "hamid") {
            player<Happy>("Hi!")
            npc<Idle>("Hello traveller. How can I be of assistance?")
            choice {
                option<Confused>("Can you heal me?") {
                    npc<Idle>("You'd be better off speaking to one of the nurses.")
                    npc<Happy>("They are so... nice... afterall!")
                }
                option<Confused>("What's a Monk doing in a place such as this?") {
                    npc<Shifty>("Well don't tell anyone but I came here because of the nurses!")
                    player<Laugh>("Really?")
                    npc<Laugh>("It beats being stuck in the monastery!")
                }
                option<Confused>("Which monastery do you come from?") {
                    npc<Idle>("I belong to the monastery north of Falador.")
                    player<Confused>("You're a long way from home?")
                    npc<Disheartened>("Yeh. I miss the guys.")
                }
            }
        }
    }
}
