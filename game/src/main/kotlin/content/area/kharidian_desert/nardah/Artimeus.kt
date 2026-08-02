package content.area.kharidian_desert.nardah

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Artimeus : Script {
    init {
        npcOperate("Talk-to", "artimeus") { (target) ->
            npc<Happy>("Greetings, friend; my business here deals with Hunter related items. Is there anything in which I can interest you?")
            choice {
                option<Quiz>("What kinds of items do you stock?") {
                    npc<Happy>("Take a look for yourself.")
                    openShop(target.def["shop"])
                }
                option("I'm not in the market for Hunter equipment right now, thanks.") {
                    player<Neutral>("I'm not in the market for Hunter equipment right now, thanks.")
                    npc<Neutral>("Maybe another time, then.")
                }
            }
        }
    }
}
