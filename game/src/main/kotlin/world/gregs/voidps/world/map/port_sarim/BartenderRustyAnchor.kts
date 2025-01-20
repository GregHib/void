package world.gregs.voidps.world.map.port_sarim

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.forceChat
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.world.activity.quest.mini.barCrawlDrink
import world.gregs.voidps.world.activity.quest.mini.barCrawlFilter
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.buy

npcOperate("Talk-to", "bartender_rusty_anchor") {
    choice {
        option<Quiz>("Could I buy a beer please?") {
            npc<Talk>("Sure, that will be 2 gold coins please.")
            if (buy("beer", 2, "I don't have enough coins.")) {
                player.message("You buy a pint of beer!")
            }
        }
        option<Talk>("Have you heard any rumours here?") {
            npc<Talk>("Well, there was a guy in here earlier saying the goblins up by the mountain are arguing again, about the colour of their armour of all things.")
        }
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
    }
}

itemOnNPCOperate("barcrawl_card", "bartender_rusty_anchor") {
    if (player.containsVarbit("barcrawl_signatures", "black_skull_ale")) {
        player.noInterest() // TODO proper message
        return@itemOnNPCOperate
    }
    barCrawl()
}

suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
    start = {
        npc<Quiz>("Are you sure? You look a bit skinny for that.")
        player<Talk>("Just give me whatever I need to drink here.")
        npc<Talk>("Ok one Black Skull Ale coming up, 8 coins please.")
    },
    effects = {
        player.forceChat = "Hiccup!"
    }
)