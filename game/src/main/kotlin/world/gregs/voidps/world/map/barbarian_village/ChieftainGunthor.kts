package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

npcOperate("Talk-to", "chieftain_gunthor") {
    when (player.quest("gunnars_ground")) {
        "completed" -> {
        }
        "tell_gudrun", "tell_dororan", "write_poem", "more_poem", "one_more_poem", "poem_done", "poem", "recital", "gunnars_ground" -> {
            npc<Angry>("Run back to Gudrun and tell her to remember her forefathers!")
            npc<Furious>("Tell her to think of Gunnar and what he would think of this insult! Now go before I have Haakon dismember you.")
            seeHimTry()
        }
        "meet_chieftain" -> meetChieftain()
        else -> unstarted()
    }
}

suspend fun CharacterContext.meetChieftain() {
    npc<Furious>("Begone, outerlander! Your kind are not welcome here!")
    choice {
        option<Talking>("I need to speak with you, chieftain.") {
            makeItShort()
        }
        option<Talking>("Be quiet and listen.") {
            makeItShort()
        }
    }
}

suspend fun CharacterContext.makeItShort() {
    npc<Angry>("Make it short.")
    player<Talk>("Your daughter seeks permission to court an outerlander.")
    npc<Mad>("WHAT??")
    choice {
        option<Talking>("Your daughter seeks permission to court an outerlander.") {
            barbarians()
        }
        option<Talking>("Are you deaf?") {
            barbarians()
        }
    }
}

suspend fun CharacterContext.barbarians() {
    npc<Angry>("Do you have ANY idea who we are?")
    choice {
        option<Talking>("You're barbarians.") {
            waitAMoment()
        }
        option<Talking>("You're a tribe of primitives.") {
            waitAMoment()
        }
    }
}

suspend fun CharacterContext.waitAMoment() {
    npc<Furious>("We are storm that sweeps from the mountains! We are the scourge of these soft lands!")
    choice {
        option<Talking>("Please wait a moment.") {
            campOfWar()
        }
        option<Talking>("Are you finished?") {
            campOfWar()
        }
    }
}

suspend fun CharacterContext.campOfWar() {
    npc<Angry>("We are the freemen of the ice. You think this a settlement, but it is a camp of war!")
    npc<Angry>("haakon_the_champion", "Chieftain! May I interrupt?")
    npc<Angry>("What is it, Haakon?")
    npc<Angry>("haakon_the_champion", "We have lived here since before the time of my father. Perhaps we are no longer a camp.")
    npc<Unsure>("Your father? Do you honour him, Haakon?")
    npc<Furious>("haakon_the_champion", "Of course!")
    npc<Unsure>("And do you honour Warloard Gunnar?")
    npc<Furious>("haakon_the_champion", "Of course, Chieftain!")
    npc<Angry>("Then why do you dishonour his name by abandoning what he fought for?")
    npc<Furious>("We will honour our fathers and we will honour Gunnar!")
    npc<Angry>("haakon_the_champion", "Yes, Chieftain. You are wise. I am sorry.")
    npc<Mad>("You! Outerlander!")
    player<Unsure>("What?")
    npc<Mad>("We are not friends, you and I! We are not allies!")
    npc<Angry>("Run back to Gudrun and tell her to remember her forefathers!")
    player["gunnars_ground"] = "tell_gudrun"
    npc<Angry>("Tell her to think of Gunnar and what he would think of this insult! Now go before I have Haakon dismember you.")
    seeHimTry()
}

suspend fun CharacterContext.seeHimTry() {
    choice {
        option<Talk>("I'm going!") {
        }
        option<Talk>("I'd like to see him try.") {
            npc<Furious>("haakon_the_champion", "Come here and say that to my face, outerlander!")
        }
        option<Talk>("I'm going to challenge him right now!") {
            npc<Furious>("haakon_the_champion", "Come here and say that to my face, outerlander!")
        }
    }
}

suspend fun CharacterContext.unstarted() {
    npc<Angry>("Begone, outerlander! Your kind are not welcome here!")
}