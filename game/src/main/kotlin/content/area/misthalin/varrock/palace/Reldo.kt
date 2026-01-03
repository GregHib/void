package content.area.misthalin.varrock.palace

import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script

class Reldo : Script {

    init {
        npcOperate("Talk-to", "reldo*") {
            npc<Idle>("Hello stranger.")
            choice {
                anythingToTrade()
                whatDoYouDo()
                aboutImcandoDwarves()
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

    fun ChoiceOption.aboutImcandoDwarves() = option<Quiz>(
        "What do you know about the Imcando dwarves?",
        {
            val stage = quest("the_knights_sword")
            stage == "started" || stage == "find_thurgo"
        },
    ) {
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
