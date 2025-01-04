package world.gregs.voidps.world.map.ardougne

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetNPCContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.npc.shop.buy
import world.gregs.voidps.world.activity.quest.mini.barCrawlDrink
import world.gregs.voidps.world.activity.quest.mini.barCrawlFilter
import world.gregs.voidps.world.interact.entity.combat.hit.damage

npcOperate("Talk-to", "bartender_flying_horse_inn") {
    npc<Quiz>("Would you like to buy a drink?")
    player<Quiz>("What do you serve?")
    npc<Talk>("Beer!")
    choice {
        option<Talk>("I'll have a beer then.") {
            npc<Talk>("Ok, that'll be two coins.")
            if (buy("beer", 2)) {
                player.message("You buy a pint of beer.")
            }
        }
        option<Talk>("I'll not have anything then.")
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
    }
}

itemOnNPCOperate("barcrawl_card", "bartender_flying_horse_inn") {
    if (player.containsVarbit("barcrawl_signatures", "heart_stopper")) {
        player.noInterest()// TODO proper message
        return@itemOnNPCOperate
    }
    barCrawl()
}

suspend fun TargetNPCContext.barCrawl() = barCrawlDrink(
    effects = {
        player.message("signing your barcrawl card")
        player.damage(player.levels.get(Skill.Constitution) / 4)
    }
)