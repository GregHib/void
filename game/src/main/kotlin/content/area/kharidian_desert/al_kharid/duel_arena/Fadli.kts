package content.area.kharidian_desert.al_kharid.duel_arena

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

npcOperate("Talk-to", "fadli") {
    player<Happy>("Hi.")
    npc<RollEyes>("What?")
    choice {
        option<Neutral>("What do you do?") {
            npc<RollEyes>("You can store your stuff here if you want. You can dump anything you don't want to carry whilst you're fighting duels and then pick it up again on the way out.")
            npc<RollEyes>("To be honest I'm wasted here.")
            npc<Frustrated>("I should be winning duels in an arena! I'm the best warrior in Al Kharid!")
            player<Uncertain>("Easy, tiger!")
        }
        option<Uncertain>("What is this place?") {
            npc<Frustrated>("Isn't it obvious?")
            npc<Neutral>("This is the Duel Arena...duh!")
        }
        option<Neutral>("I'd like to access my bank, please.") {
            npc<RollEyes>("Sure.")
            player.open("bank")
        }
        option<Happy>("I'd like to collect items.") {
            npc<RollEyes>("Yeah, okay.")
            player.open("collection_box")
        }
        option<Neutral>("Do you watch any matches?") {
            npc<Neutral>("When I can.")
            npc<Happy>("Most aren't any good so I throw rotten fruit at them!")
            player<Happy>("Heh. Can I buy some?")
            if (World.members) {
                npc<Chuckle>("Sure.")
                player.openShop("shop_of_distaste")
                return@option
            }
            npc<RollEyes>("Nope.")
            player.message("You need to be on a members world to use this feature.")
        }
    }
}

npcOperate("Bank", "fadli") {
    player.open("bank")
}

npcOperate("Collect", "fadli") {
    player.open("collection_box")
}

npcOperate("Buy", "fadli") {
    if (World.members) {
        player.openShop("shop_of_distaste")
        return@npcOperate
    }
    npc<RollEyes>("Sorry, I'm not interested.")
    player.message("You need to be on a members world to use this feature.")
}
