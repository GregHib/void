package content.area.karamja

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

class BartenderZambo : Script {

    init {
        npcOperate("Talk-to", "bartender_zambo") { (target) ->
            npc<Talk>("Hey, are you wanting to try some of my fine wines and spirits? All brewed locally on Karamja.")
            choice {
                option("Yes, please.") {
                    openShop("karamja_wines_spirits_and_beers")
                }
                option<Talk>("No, thank you.")
                if (onBarCrawl(target)) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl(target)
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_zambo") {
            if (player.containsVarbit("barcrawl_signatures", "ape_bite_liqueur")) {
                return@itemOnNPCOperate
            }
            player.barCrawl(target)
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        effects = {
            say("Mmmmm, dat was luverly...")
        },
    )
}
