package content.area.karamja

import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.world.activity.quest.mini.barCrawlDrink
import world.gregs.voidps.world.activity.quest.mini.barCrawlFilter
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.openShop

npcOperate("Talk-to", "bartender_zambo") {
    npc<Talk>("Hey, are you wanting to try some of my fine wines and spirits? All brewed locally on Karamja.")
    choice {
        option("Yes, please.") {
            player.openShop("karamja_wines_spirits_and_beers")
        }
        option<Talk>("No, thank you.")
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
    }
}

itemOnNPCOperate("barcrawl_card", "bartender_zambo") {
    if (player.containsVarbit("barcrawl_signatures", "ape_bite_liqueur")) {
        player.noInterest() // TODO proper message
        return@itemOnNPCOperate
    }
    barCrawl()
}

suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
    effects = {
        player.say("Mmmmm, dat was luverly...")
    }
)