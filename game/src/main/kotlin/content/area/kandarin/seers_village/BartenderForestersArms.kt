package content.area.kandarin.seers_village

import content.entity.npc.shop.buy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BartenderForestersArms : Script {

    init {
        npcOperate("Talk-to", "bartender_foresters_arms") { (target) ->
            npc<Quiz>("Good morning, what would you like?")
            choice {
                option<Quiz>("What do you have?") {
                    npc<Talk>("Well we have beer, or if you want some food, we have our home made stew and meat pies.")
                    choice {
                        option<Talk>("Beer please.") {
                            npc<Talk>("One beer coming up. Ok, that'll be two coins.")
                            if (buy("beer", 2)) {
                                message("You buy a pint of beer.")
                            }
                        }
                        option<Talk>("I'll try the meat pie.") {
                            npc<Talk>("Ok, that'll be 16 coins.")
                            if (buy("meat_pie", 16)) {
                                message("You buy a nice hot meat pie.")
                            }
                        }
                        option<Quiz>("Could I have some stew please?") {
                            npc<Talk>("A bowl of stew, that'll be 20 coins please.")
                            if (buy("stew", 20)) {
                                message("You buy a bowl of home made stew.")
                            }
                        }
                        option<Talk>("I don't really want anything thanks.")
                    }
                }
                option<Talk>("I'll have a beer then.") {
                    npc<Talk>("Ok, that'll be two coins.")
                    if (buy("beer", 20)) {
                        message("You buy a pint of beer.")
                    }
                }
                if (onBarCrawl(target)) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl(target)
                    }
                }
                option<Talk>("I don't really want anything thanks.")
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_foresters_arms") { (target) ->
            if (containsVarbit("barcrawl_signatures", "liverbane_ale")) {
                return@itemOnNPCOperate
            }
            barCrawl(target)
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        effects = {
            levels.drain(Skill.Attack, 6)
            levels.drain(Skill.Defence, 6)
            levels.drain(Skill.Firemaking, 6)
            levels.drain(Skill.Fletching, 5)
            levels.drain(Skill.Woodcutting, 5)
        },
    )
}
