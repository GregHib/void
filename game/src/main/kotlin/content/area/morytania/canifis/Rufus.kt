package content.area.morytania.canifis

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Rufus : Script {
    init {
        npcOperate("Talk-to", "rufus") { (target) ->
            player<Happy>("Hi!")
            npc<Neutral>("Grrreetings frrriend! Welcome to my worrrld famous food emporrrium! All my meats are so frrresh you'd swear you killed them yourrrself!")
            choice {
                option<Quiz>("Why do you only sell meats?") {
                    npc<Confused>("What? Why, what else would you want to eat? What kind of lycanthrrrope are you anyway?")
                    player<Quiz>("...A vegetarian one?")
                    npc<Confused>("Vegetarrrian...?")
                    player<Sad>("Never mind.")
                }
                option<Quiz>("Do you sell cooked food?") {
                    npc<Confused>("Cooked food? Who would want that? You lose all the flavourrr of the meat when you can't taste the blood!")
                }
                option<Quiz>("Can I buy some food?") {
                    npc<Happy>("Cerrrtainly!")
                    openShop(target.def["shop"])
                }
            }
        }
    }
}
