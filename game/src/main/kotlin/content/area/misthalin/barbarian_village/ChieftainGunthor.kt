package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.suspend.SuspendableContext

@Script
class ChieftainGunthor {

    init {
        npcOperate("Talk-to", "chieftain_gunthor_*") {
            when (player.quest("gunnars_ground")) {
                "completed" -> {
                }
                "tell_gudrun", "tell_dororan", "write_poem", "more_poem", "one_more_poem", "poem_done", "poem", "recital", "gunnars_ground" -> {
                    npc<Frustrated>("Run back to Gudrun and tell her to remember her forefathers!")
                    npc<Angry>("Tell her to think of Gunnar and what he would think of this insult! Now go before I have Haakon dismember you.")
                    seeHimTry()
                }
                "meet_chieftain" -> meetChieftain()
                else -> unstarted()
            }
        }
    }

    suspend fun SuspendableContext<Player>.meetChieftain() {
        npc<Angry>("Begone, outerlander! Your kind are not welcome here!")
        choice {
            option<Neutral>("I need to speak with you, chieftain.") {
                makeItShort()
            }
            option<Neutral>("Be quiet and listen.") {
                makeItShort()
            }
        }
    }

    suspend fun SuspendableContext<Player>.makeItShort() {
        npc<Frustrated>("Make it short.")
        player<Talk>("Your daughter seeks permission to court an outerlander.")
        npc<Mad>("WHAT??")
        choice {
            option<Neutral>("Your daughter seeks permission to court an outerlander.") {
                barbarians()
            }
            option<Neutral>("Are you deaf?") {
                barbarians()
            }
        }
    }

    suspend fun SuspendableContext<Player>.barbarians() {
        npc<Frustrated>("Do you have ANY idea who we are?")
        choice {
            option<Neutral>("You're barbarians.") {
                waitAMoment()
            }
            option<Neutral>("You're a tribe of primitives.") {
                waitAMoment()
            }
        }
    }

    suspend fun SuspendableContext<Player>.waitAMoment() {
        npc<Angry>("We are storm that sweeps from the mountains! We are the scourge of these soft lands!")
        choice {
            option<Neutral>("Please wait a moment.") {
                campOfWar()
            }
            option<Neutral>("Are you finished?") {
                campOfWar()
            }
        }
    }

    suspend fun SuspendableContext<Player>.campOfWar() {
        npc<Frustrated>("We are the freemen of the ice. You think this a settlement, but it is a camp of war!")
        npc<Frustrated>("haakon_the_champion", "Chieftain! May I interrupt?")
        npc<Frustrated>("What is it, Haakon?")
        npc<Frustrated>("haakon_the_champion", "We have lived here since before the time of my father. Perhaps we are no longer a camp.")
        npc<Quiz>("Your father? Do you honour him, Haakon?")
        npc<Angry>("haakon_the_champion", "Of course!")
        npc<Quiz>("And do you honour Warloard Gunnar?")
        npc<Angry>("haakon_the_champion", "Of course, Chieftain!")
        npc<Frustrated>("Then why do you dishonour his name by abandoning what he fought for?")
        npc<Angry>("We will honour our fathers and we will honour Gunnar!")
        npc<Frustrated>("haakon_the_champion", "Yes, Chieftain. You are wise. I am sorry.")
        npc<Mad>("You! Outerlander!")
        player<Quiz>("What?")
        npc<Mad>("We are not friends, you and I! We are not allies!")
        npc<Frustrated>("Run back to Gudrun and tell her to remember her forefathers!")
        player["gunnars_ground"] = "tell_gudrun"
        npc<Frustrated>("Tell her to think of Gunnar and what he would think of this insult! Now go before I have Haakon dismember you.")
        seeHimTry()
    }

    suspend fun SuspendableContext<Player>.seeHimTry() {
        choice {
            option<Talk>("I'm going!") {
            }
            option<Talk>("I'd like to see him try.") {
                npc<Angry>("haakon_the_champion", "Come here and say that to my face, outerlander!")
            }
            option<Talk>("I'm going to challenge him right now!") {
                npc<Angry>("haakon_the_champion", "Come here and say that to my face, outerlander!")
            }
        }
    }

    suspend fun SuspendableContext<Player>.unstarted() {
        npc<Frustrated>("Begone, outerlander! Your kind are not welcome here!")
    }
}
