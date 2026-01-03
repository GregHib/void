package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World

class Fadli : Script {

    init {
        npcOperate("Talk-to", "fadli") {
            player<Happy>("Hi.")
            npc<Bored>("What?")
            choice {
                option<Idle>("What do you do?") {
                    npc<Bored>("You can store your stuff here if you want. You can dump anything you don't want to carry whilst you're fighting duels and then pick it up again on the way out.")
                    npc<Bored>("To be honest I'm wasted here.")
                    npc<Frustrated>("I should be winning duels in an arena! I'm the best warrior in Al Kharid!")
                    player<Confused>("Easy, tiger!")
                }
                option<Confused>("What is this place?") {
                    npc<Frustrated>("Isn't it obvious?")
                    npc<Idle>("This is the Duel Arena...duh!")
                }
                option<Idle>("I'd like to access my bank, please.") {
                    npc<Bored>("Sure.")
                    open("bank")
                }
                option<Happy>("I'd like to collect items.") {
                    npc<Bored>("Yeah, okay.")
                    open("collection_box")
                }
                option<Idle>("Do you watch any matches?") {
                    npc<Idle>("When I can.")
                    npc<Happy>("Most aren't any good so I throw rotten fruit at them!")
                    player<Happy>("Heh. Can I buy some?")
                    if (World.members) {
                        npc<Laugh>("Sure.")
                        openShop("shop_of_distaste")
                        return@option
                    }
                    npc<Bored>("Nope.")
                    message("You need to be on a members world to use this feature.")
                }
            }
        }

        npcOperate("Bank", "fadli") {
            open("bank")
        }

        npcOperate("Collect", "fadli") {
            open("collection_box")
        }

        npcOperate("Buy", "fadli") {
            if (World.members) {
                openShop("shop_of_distaste")
                return@npcOperate
            }
            npc<Bored>("Sorry, I'm not interested.")
            message("You need to be on a members world to use this feature.")
        }
    }
}
