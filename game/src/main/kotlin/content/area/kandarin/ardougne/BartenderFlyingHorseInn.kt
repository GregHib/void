package content.area.kandarin.ardougne

import content.entity.combat.hit.damage
import content.entity.npc.shop.buy
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.Dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class BartenderFlyingHorseInn : Api {

    init {
        npcOperateDialogue("Talk-to", "bartender_flying_horse_inn") {
            npc<Quiz>("Would you like to buy a drink?")
            player<Quiz>("What do you serve?")
            npc<Happy>("Beer!")
            choice {
                option<Talk>("I'll have a beer then.") {
                    npc<Talk>("Ok, that'll be two coins.")
                    if (buy("beer", 2)) {
                        player.message("You buy a pint of beer.")
                    }
                }
                option<Talk>("I'll not have anything then.")
                if (onBarCrawl()) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl()
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_flying_horse_inn") {
            if (player.containsVarbit("barcrawl_signatures", "heart_stopper")) {
                return@itemOnNPCOperate
            }
            player.talkWith(target) {
                barCrawl()
            }
        }
    }

    suspend fun Dialogue.barCrawl() = barCrawlDrink(
        effects = {
            player.message("signing your barcrawl card")
            player.damage(player.levels.get(Skill.Constitution) / 4)
        },
    )
}
