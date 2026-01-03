package content.area.kandarin.ardougne

import content.entity.combat.hit.damage
import content.entity.npc.shop.buy
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BartenderFlyingHorseInn : Script {

    init {
        npcOperate("Talk-to", "bartender_flying_horse_inn") { (target) ->
            npc<Quiz>("Would you like to buy a drink?")
            player<Quiz>("What do you serve?")
            npc<Happy>("Beer!")
            choice {
                option<Neutral>("I'll have a beer then.") {
                    npc<Neutral>("Ok, that'll be two coins.")
                    if (buy("beer", 2)) {
                        message("You buy a pint of beer.")
                    }
                }
                option<Neutral>("I'll not have anything then.")
                if (onBarCrawl(target)) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl(target)
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_flying_horse_inn") { (target) ->
            if (containsVarbit("barcrawl_signatures", "heart_stopper")) {
                return@itemOnNPCOperate
            }
            barCrawl(target)
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        effects = {
            message("signing your barcrawl card")
            damage(levels.get(Skill.Constitution) / 4)
        },
    )
}
