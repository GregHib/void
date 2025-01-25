package content.area.karamja

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
import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.npc.shop.buy

npcOperate("Talk-to", "bartender_dead_mans_chest") {
    npc<Chuckle>("Yohoho me hearty what would you like to drink?")
    choice {
        option<Talk>("Nothing, thank you.")
        option<Talk>("A pint of Grog please.") {
            npc<Talk>("One grog coming right up, that'll be three coins.")
            if (buy("grog", 3, "Oh dear. I don't seem to have enough money.")) {
                player.message("You buy a pint of grog.")
            }
        }
        option<Talk>("A bottle of rum please.") {
            npc<Talk>("That'll be 27 coins.")
            if (buy("bottle_of_rum", 27, "Oh dear. I don't seem to have enough money.")) {
                player.message("You buy a bottle of rum.")
            }
        }
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
    }
}

itemOnNPCOperate("barcrawl_card", "bartender_dead_mans_chest") {
    if (player.containsVarbit("barcrawl_signatures", "supergrog")) {
        player.noInterest() // TODO proper message
        return@itemOnNPCOperate
    }
    barCrawl()
}

suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
    start = { npc<Happy>("Haha time to be breaking out the old Supergrog. That'll be 15 coins please.") },
    effects = {
        player.levels.drain(Skill.Attack, 7)
        player.levels.drain(Skill.Defence, 6)
        player.levels.drain(Skill.Herblore, 5)
        player.levels.drain(Skill.Cooking, 6)
        player.levels.drain(Skill.Prayer, 5)
    }
)