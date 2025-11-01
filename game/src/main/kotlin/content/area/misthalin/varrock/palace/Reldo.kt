package content.area.misthalin.varrock.palace

import content.entity.player.dialogue.Chuckle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.*
import content.quest.quest
import world.gregs.voidps.engine.Script

class Reldo : Script {

    init {
        npcOperate("Talk-to", "reldo*") {
            npc<Neutral>("Hello stranger.")
            choice {
                anythingToTrade()
                whatDoYouDo()
                aboutImcandoDwarves()
            }
        }
    }

    fun ChoiceOption.anythingToTrade() = option<Quiz>("Do you have anything to trade?") {
        npc<Neutral>("Only knowledge.")
        player<Quiz>("How much do you want for that then?")
        npc<Chuckle>("No, sorry, that was just my little joke. I'm not the trading type.")
        player<Neutral>("Ah well.")
    }

    fun ChoiceOption.whatDoYouDo() = option<Quiz>("What do you do?") {
        npc<Neutral>("I am the palace librarian.")
        player<Neutral>("Ah. That's why you're in the library then.")
        npc<Neutral>("Yes.")
        npc<Neutral>("Although I would probably be in here even if I didn't work here. I like reading. Someday I hope to catalogue all of the information stored in these books so all may read it.")
    }

    fun ChoiceOption.aboutImcandoDwarves() = option<Quiz>(
        "What do you know about the Imcando dwarves?",
        {
            val stage = quest("the_knights_sword")
            stage == "started" || stage == "find_thurgo"
        },
    ) {
        npc<Neutral>("The Imcando dwarves, you say?")
        npc<Neutral>("Ah yes... for many hundreds of years they were the world's most skilled smiths. They used secret smithing knowledge passed down from generation to generation.")
        npc<Neutral>("Unfortunately, about a century ago, the once thriving race was wiped out during the barbarian invasions of that time.")
        player<Quiz>("So are there any Imcando left at all?")
        npc<Neutral>("I believe a few of them survived, but with the bulk of their population destroyed their numbers have dwindled even further.")
        npc<Neutral>("They tend to keep to themselves, and they tend not to tell people they're descendants of the Imcando, which is why people think the tribe is extinct. However...")
        if (quest("the_knights_sword") == "started") {
            set("the_knights_sword", "find_thurgo")
        }
        npc<Shifty>("... you could try taking them some redberry pie. They REALLY like redberry pie. I believe I remember a couple living in Asgarnia near the cliffs on the Asgarnian southern peninsula.")
    }
}
