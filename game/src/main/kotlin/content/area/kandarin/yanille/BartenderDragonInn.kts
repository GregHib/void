package content.area.kandarin.yanille

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
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.npc.shop.buy

npcOperate("Talk-to", "bartender_dragon_inn") {
    npc<Quiz>("What can I get you?")
    player<Quiz>("What's on the menu?")
    npc<Talk>("Dragon Bitter and Greenman's Ale, oh and some cheap beer.")
    choice {
        option<Talk>("I'll try the Dragon Bitter.") {
            npc<Talk>("Ok, that'll be two coins.")
            if (buy("dragon_bitter", 2)) {
                player.message("You buy a pint of Dragon Bitter.")
            }
        }
        option<Talk>("Can I have some Greenman's Ale?") {
            npc<Talk>("Ok, that'll be ten coins.")
            if (buy("greenmans_ale", 10)) {
                player<Talk>("Ok, here you go.")
                player.message("You buy a pint of Greenman's Ale.")
            }
        }
        option<Talk>("One cheap beer please!") {
            npc<Talk>("That'll be 2 gold coins please!")
            if (buy("beer", 2)) {
                item("beer", 325, "You buy a pint of cheap beer.")
                npc<Talk>("Have a super day!")
            }
        }
        option<Talk>("I'll give it a miss I think.") {
            npc<Talk>("Come back when you're a little thirstier.")
        }
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
    }
}

itemOnNPCOperate("barcrawl_card", "bartender_dragon_inn") {
    if (player.containsVarbit("barcrawl_signatures", "fire_brandy")) {
        player.noInterest() // TODO proper message
        return@itemOnNPCOperate
    }
    barCrawl()
}

suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
    effects = {
        player.levels.drain(Skill.Attack, 6)
        player.levels.drain(Skill.Defence, 6)
    }
)