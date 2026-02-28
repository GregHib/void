package content.area.misthalin.varrock.palace

import content.area.misthalin.draynor_village.wise_old_man.OldMansMessage
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.*
import content.quest.quest
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.carriesItem

class Reldo : Script {

    init {
        npcOperate("Talk-to", "reldo*") {
            npc<Idle>("Hello stranger.")
            choice {
                if (get("wise_old_man_npc", "") == "reldo" && carriesItem("old_mans_message")) {
                    option<Happy>("The Wise Old Man of Draynor Village sent you this message.") {
                        wiseOldManLetter()
                    }
                }
                anythingToTrade()
                whatDoYouDo()
                val stage = quest("the_knights_sword")
                if (stage == "started" || stage == "find_thurgo") {
                    aboutImcandoDwarves()
                }
            }
        }
    }

    private suspend fun Player.wiseOldManLetter() {
        npc<Happy>("Ah, I am always delighted to hear from him. You would not imagine the depths of his wisdom!")
        val reward = OldMansMessage.rewardLetter(this) ?: return
        when (reward) {
            "runes" -> {
                items("nature_rune", "water_rune", "Reldo gives you some runes.")
                npc<Happy>("My old friend Aubury sent me some runes in return for some books he wanted. Perhaps you'd like them?")
            }
            "herbs" -> {
                item("grimy_tarromin", 400, "<navy>Reldo gives you some herbs.") // TODO proper message
            }
            "seeds" -> {
                item("potato_seed", 400, "<navy>Reldo gives you some seeds.")
                npc<Happy>("These little things seem to be everywhere these days! Perhaps you'd like them?")
            }
            "prayer" -> {
                item(167, "<navy>Reldo blesses you.<br>You gain some Prayer xp.") // TODO proper message
            }
            "coins" -> {
                item("coins_8", 400, "<navy>Reldo gives you some coins.")
                npc<Happy>("King Roald has been very generous with my salary, so I can spare you some coins for your trouble.")
            }
            else -> {
                item(reward, 400, "Reldo gives you an ${reward.toSentenceCase()}!") // TODO proper message
            }
        }
    }

    fun ChoiceOption.anythingToTrade() = option<Quiz>("Do you have anything to trade?") {
        npc<Idle>("Only knowledge.")
        player<Quiz>("How much do you want for that then?")
        npc<Laugh>("No, sorry, that was just my little joke. I'm not the trading type.")
        player<Idle>("Ah well.")
    }

    fun ChoiceOption.whatDoYouDo() = option<Quiz>("What do you do?") {
        npc<Idle>("I am the palace librarian.")
        player<Idle>("Ah. That's why you're in the library then.")
        npc<Idle>("Yes.")
        npc<Idle>("Although I would probably be in here even if I didn't work here. I like reading. Someday I hope to catalogue all of the information stored in these books so all may read it.")
    }

    fun ChoiceOption.aboutImcandoDwarves() = option<Quiz>("What do you know about the Imcando dwarves?") {
        npc<Idle>("The Imcando dwarves, you say?")
        npc<Idle>("Ah yes... for many hundreds of years they were the world's most skilled smiths. They used secret smithing knowledge passed down from generation to generation.")
        npc<Idle>("Unfortunately, about a century ago, the once thriving race was wiped out during the barbarian invasions of that time.")
        player<Quiz>("So are there any Imcando left at all?")
        npc<Idle>("I believe a few of them survived, but with the bulk of their population destroyed their numbers have dwindled even further.")
        npc<Idle>("They tend to keep to themselves, and they tend not to tell people they're descendants of the Imcando, which is why people think the tribe is extinct. However...")
        if (quest("the_knights_sword") == "started") {
            set("the_knights_sword", "find_thurgo")
        }
        npc<Shifty>("... you could try taking them some redberry pie. They REALLY like redberry pie. I believe I remember a couple living in Asgarnia near the cliffs on the Asgarnian southern peninsula.")
    }
}
