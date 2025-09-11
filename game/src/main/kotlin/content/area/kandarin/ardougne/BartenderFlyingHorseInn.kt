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
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlFilter
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script

@Script
class BartenderFlyingHorseInn {

    init {
        npcOperate("Talk-to", "bartender_flying_horse_inn") {
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
                option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
                    barCrawl()
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_flying_horse_inn") {
            if (player.containsVarbit("barcrawl_signatures", "heart_stopper")) {
                player.noInterest() // TODO proper message
                return@itemOnNPCOperate
            }
            barCrawl()
        }
    }

    suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
        effects = {
            player.message("signing your barcrawl card")
            player.damage(player.levels.get(Skill.Constitution) / 4)
        },
    )
}
