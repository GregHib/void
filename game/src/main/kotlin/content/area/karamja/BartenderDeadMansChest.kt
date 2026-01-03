package content.area.karamja

import content.entity.npc.shop.buy
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BartenderDeadMansChest : Script {

    init {
        npcOperate("Talk-to", "bartender_dead_mans_chest") { (target) ->
            npc<Laugh>("Yohoho me hearty what would you like to drink?")
            choice {
                option<Neutral>("Nothing, thank you.")
                option<Neutral>("A pint of Grog please.") {
                    npc<Neutral>("One grog coming right up, that'll be three coins.")
                    if (buy("grog", 3, "Oh dear. I don't seem to have enough money.")) {
                        message("You buy a pint of grog.")
                    }
                }
                option<Neutral>("A bottle of rum please.") {
                    npc<Neutral>("That'll be 27 coins.")
                    if (buy("bottle_of_rum", 27, "Oh dear. I don't seem to have enough money.")) {
                        message("You buy a bottle of rum.")
                    }
                }
                if (onBarCrawl(target)) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl(target)
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_dead_mans_chest") { (target) ->
            if (containsVarbit("barcrawl_signatures", "supergrog")) {
                return@itemOnNPCOperate
            }
            barCrawl(target)
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        start = { npc<Happy>("Haha time to be breaking out the old Supergrog. That'll be 15 coins please.") },
        effects = {
            levels.drain(Skill.Attack, 7)
            levels.drain(Skill.Defence, 6)
            levels.drain(Skill.Herblore, 5)
            levels.drain(Skill.Cooking, 6)
            levels.drain(Skill.Prayer, 5)
        },
    )
}
