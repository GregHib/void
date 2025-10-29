package content.area.karamja

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.dialogue.Dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.event.Script

@Script
class BartenderZambo : Api {

    init {
        npcOperateDialogue("Talk-to", "bartender_zambo") {
            npc<Talk>("Hey, are you wanting to try some of my fine wines and spirits? All brewed locally on Karamja.")
            choice {
                option("Yes, please.") {
                    player.openShop("karamja_wines_spirits_and_beers")
                }
                option<Talk>("No, thank you.")
                if (onBarCrawl()) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl()
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_zambo") {
            if (player.containsVarbit("barcrawl_signatures", "ape_bite_liqueur")) {
                return@itemOnNPCOperate
            }
            player.talkWith(target) {
                barCrawl()
            }
        }
    }

    suspend fun Dialogue.barCrawl() = barCrawlDrink(
        effects = {
            player.say("Mmmmm, dat was luverly...")
        },
    )
}
