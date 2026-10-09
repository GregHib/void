package content.area.asgarnia.port_sarim

import content.entity.npc.shop.buy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player

class BartenderRustyAnchor : Script {

    init {
        npcOperate("Talk-to", "bartender_rusty_anchor_inn*") { (target) ->
            choice {
                option<Quiz>("Could I buy a beer please?") {
                    npc<Neutral>("Sure, that will be 2 gold coins please.")
                    if (buy("beer", 2, "I don't have enough coins.")) {
                        message("You buy a pint of beer!")
                    }
                }
                option<Neutral>("Have you heard any rumours here?") {
                    if (!questCompleted("goblin_diplomacy")) {
                        npc<Neutral>("Well, there was a guy in here earlier saying the goblins up by the mountain are arguing again, about the colour of their armour of all things.")
                        npc<Neutral>("Knowing the goblins it could easily turn into a full blown war, which wouldn't be good. Goblin wars make such a mess of the countryside.")
                        player<Neutral>("Well if I have the time I'll go and see if I can knock some sense into them.")
                    } else {
                        npc<Neutral>("No, it hasn't been very busy lately.")
                    }
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

        objectOperate("Enter", "port_sarim_bar_trapdoor") {
            talkWith(NPCs.find(tile.region.toLevel(tile.level), "bartender_rusty_anchor_inn_3"))
            npc<Neutral>("You're not getting in there, mate, it's locked up.")
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        start = {
            npc<Quiz>("Are you sure? You look a bit skinny for that.")
            player<Neutral>("Just give me whatever I need to drink here.")
            npc<Neutral>("Ok one Black Skull Ale coming up, 8 coins please.")
        },
        effects = {
            say("Hiccup!")
        },
    )
}
