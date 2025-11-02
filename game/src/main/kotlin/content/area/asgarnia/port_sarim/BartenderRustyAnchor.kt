package content.area.asgarnia.port_sarim

import content.entity.combat.target
import content.entity.npc.shop.buy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class BartenderRustyAnchor : Script {

    init {
        playerSpawn {
            set("void_dance_bartender", 19)
        }

        npcOperate("Talk-to", "bartender_rusty_anchor_inn*") { (target) ->
            choice {
                option<Quiz>("Could I buy a beer please?") {
                    npc<Talk>("Sure, that will be 2 gold coins please.")
                    if (buy("beer", 2, "I don't have enough coins.")) {
                        message("You buy a pint of beer!")
                    }
                }
                option<Talk>("Have you heard any rumours here?") {
                    npc<Talk>("Well, there was a guy in here earlier saying the goblins up by the mountain are arguing again, about the colour of their armour of all things.")
                }
                if (onBarCrawl(target)) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl(target)
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_rusty_anchor_inn*") { (target) ->
            if (containsVarbit("barcrawl_signatures", "black_skull_ale")) {
                return@itemOnNPCOperate
            }
            barCrawl(target)
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        start = {
            npc<Quiz>("Are you sure? You look a bit skinny for that.")
            player<Talk>("Just give me whatever I need to drink here.")
            npc<Talk>("Ok one Black Skull Ale coming up, 8 coins please.")
        },
        effects = {
            say("Hiccup!")
        },
    )
}
