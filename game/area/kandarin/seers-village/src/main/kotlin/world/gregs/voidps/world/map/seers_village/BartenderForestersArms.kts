package world.gregs.voidps.world.map.seers_village

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.world.activity.quest.mini.barCrawlDrink
import world.gregs.voidps.world.activity.quest.mini.barCrawlFilter
import world.gregs.voidps.world.interact.dialogue.Quiz
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.buy

npcOperate("Talk-to", "bartender_foresters_arms") {
    npc<Quiz>("Good morning, what would you like?")
    choice {
        option<Quiz>("What do you have?") {
            npc<Talk>("Well we have beer, or if you want some food, we have our home made stew and meat pies.")
            choice {
                option<Talk>("Beer please.") {
                    npc<Talk>("One beer coming up. Ok, that'll be two coins.")
                    if (buy("beer", 2)) {
                        player.message("You buy a pint of beer.")
                    }
                }
                option<Talk>("I'll try the meat pie.") {
                    npc<Talk>("Ok, that'll be 16 coins.")
                    if (buy("meat_pie", 16)) {
                        player.message("You buy a nice hot meat pie.")
                    }
                }
                option<Quiz>("Could I have some stew please?") {
                    npc<Talk>("A bowl of stew, that'll be 20 coins please.")
                    if (buy("stew", 20)) {
                        player.message("You buy a bowl of home made stew.")
                    }
                }
                option<Talk>("I don't really want anything thanks.")
            }
        }
        option<Talk>("I'll have a beer then.") {
            npc<Talk>("Ok, that'll be two coins.")
            if (buy("beer", 20)) {
                player.message("You buy a pint of beer.")
            }
        }
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
        option<Talk>("I don't really want anything thanks.")
    }
}

itemOnNPCOperate("barcrawl_card", "bartender_foresters_arms") {
    if (player.containsVarbit("barcrawl_signatures", "liverbane_ale")) {
        player.noInterest()// TODO proper message
        return@itemOnNPCOperate
    }
    barCrawl()
}

suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
    effects = {
        player.levels.drain(Skill.Attack, 6)
        player.levels.drain(Skill.Defence, 6)
        player.levels.drain(Skill.Firemaking, 6)
        player.levels.drain(Skill.Fletching, 5)
        player.levels.drain(Skill.Woodcutting, 5)
    }
)