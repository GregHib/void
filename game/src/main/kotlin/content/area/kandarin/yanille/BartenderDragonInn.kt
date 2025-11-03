package content.area.kandarin.yanille

import content.entity.npc.shop.buy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill

class BartenderDragonInn : Script {

    init {
        npcOperate("Talk-to", "bartender_dragon_inn") { (target) ->
            npc<Quiz>("What can I get you?")
            player<Quiz>("What's on the menu?")
            npc<Talk>("Dragon Bitter and Greenman's Ale, oh and some cheap beer.")
            choice {
                option<Talk>("I'll try the Dragon Bitter.") {
                    npc<Talk>("Ok, that'll be two coins.")
                    if (buy("dragon_bitter", 2)) {
                        message("You buy a pint of Dragon Bitter.")
                    }
                }
                option<Talk>("Can I have some Greenman's Ale?") {
                    npc<Talk>("Ok, that'll be ten coins.")
                    if (buy("greenmans_ale", 10)) {
                        player<Talk>("Ok, here you go.")
                        message("You buy a pint of Greenman's Ale.")
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
                if (onBarCrawl(target)) {
                    option("I'm doing Alfred Grimhand's barcrawl.") {
                        barCrawl(target)
                    }
                }
            }
        }

        itemOnNPCOperate("barcrawl_card", "bartender_dragon_inn") { (target) ->
            if (containsVarbit("barcrawl_signatures", "fire_brandy")) {
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
        },
    )
}
