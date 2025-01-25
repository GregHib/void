package content.area.misthalin.varrock

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlFilter
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.npc.shop.buy

npcOperate("Talk-to", "bartender_jolly_boar_inn") {
    npc<Quiz>("Can I help you?")
    choice {
        option("I'll have a beer please.") {
            player<Talk>("I'll have a pint of beer please.")
            npc<Talk>("Ok, that'll be two coins please.")
            if (buy("beer", 2)) {
                player<Talk>("Ok, here you go.")
                player.message("You buy a pint of beer!")
            }
        }
        option<Talk>("Any hints where I can go adventuring?") {
            npc<Talk>("Ooh, now. Let me see...")
            npc<Talk>("Well there is the Varrock sewers. There are tales of untold horrors coming out at night and stealing babies from houses.")
            player<Quiz>("Sounds perfect! Where's the entrance?")
            npc<Talk>("It's just to the east of the palace.")
        }
        option<Talk>("Heard any good gossip?") {
            npc<Talk>("I'm not that well up on the gossip out here. I've heard that the bartender in the Blue Moon Inn has gone a little crazy, he keeps claiming he is part of something called an online game.")
            player<Talk>("What that means, I don't know. That's probably old news by now though.")
        }
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
    }
}

itemOnNPCOperate("barcrawl_card", "bartender_jolly_boar_inn") {
    if (player.containsVarbit("barcrawl_signatures", "olde_suspiciouse")) {
        player.noInterest() // TODO proper message
        return@itemOnNPCOperate
    }
    barCrawl()
}

suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
    effects = {
        player.levels.drain(Skill.Attack, 6)
        player.levels.drain(Skill.Defence, 6)
        player.levels.drain(Skill.Mining, 5)
        player.levels.drain(Skill.Crafting, 6)
        player.levels.drain(Skill.Magic, 6)
        player<Drunk>("Thanksh very mush...")
    }
)