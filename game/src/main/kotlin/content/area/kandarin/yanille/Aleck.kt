package content.area.kandarin.yanille

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Aleck : Script {
    init {
        npcOperate("Talk-to", "aleck") { (target) ->
            player<Neutral>("Hello.")
            npc<Happy>("Hello, hello, and a most warm welcome to my Hunter Emporium. We have everything the discerning Hunter could need.")
            npc<Happy>("Would you like me to show you our range of equipment? Or was there something specific you were after?")
            choice {
                option<Quiz>("Ok, let's see what you've got!") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("I'm not interested, thanks.") {
                    npc<Happy>("Well, if you do ever find yourself in need of the finest Hunter equipment available, then you know where to come.")
                }
                option<Quiz>("Who's that guy over there?") {
                    npc<Quiz>("Him? I think he might be crazy. Either that or he's seeking attention.")
                    npc<Confused>("He keeps trying to sell me these barmy looking weapons he's invented. I can't see them working, personally.")
                }
            }
        }
    }
}
