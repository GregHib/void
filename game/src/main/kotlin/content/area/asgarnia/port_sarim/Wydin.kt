package content.area.asgarnia.port_sarim

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Wydin : Script {
    init {
        npcOperate("Talk-to", "wydin") { (target) ->
            npc<Happy>("Welcome to my food store! Would you like to buy anything?")
            choice {
                option<Happy>("Yes please.") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No, thank you.")
                option<Quiz>("What can you recommend?") {
                    npc<Happy>("We have this really exotic fruit all the way from Karamja. It's called a banana.")
                    choice {
                        option<Neutral>("Hmm, I think I'll try one.") {
                            npc<Neutral>("Great. You might as well take a look at the rest of my wares as well.")
                            openShop(target.def["shop"])
                        }
                        option<Idle>("I don't like the sound of that.") {
                            npc<Neutral>("Well, it's your choice, but I do recommend them.")
                        }
                    }
                }
            }
        }
    }
}