package content.area.kandarin.ardougne.west_ardougne

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.npcOperate



val stages = setOf("freed_elena", "completed", "completed_with_spell")
//todo find out what they say mid quest
npcOperate("Talk-to", "ted_rehnison") {
    if (stages.contains(player.quest("plague_city"))) {//todo check dialogue for freed_elena
        npc<Quiz>("Any luck finding Elena yet?")
        player<Happy>("Yes, she is safe at home now.")
        npc<Happy>("That's good to hear, she helped us a lot.")
    } else {
        player<Happy>("Hi, I hear a woman called Elena is staying here.")
        npc<Sad>("Yes she was staying here, but slightly over a week ago she was getting ready to go back. However she never managed to leave.")
        if (player.quest("plague_city") == "returned_book") {
            player["plague_city"] = "spoken_to_ted"
        }
        npc<Neutral>("My daughter Milli was playing near the west wall when she saw some shadowy figures jump out and grab her. Milli is upstairs if you wish to speak to her.")
        //todo talk to him again at this point
    }
}

npcOperate("Talk-to", "billy_rehnison") {
    player.message("Billy isn't interested in talking.")
}

npcOperate("Talk-to", "martha_rehnison") {//todo may also set the varbit looks like the same dialogue as ted_rehnison
    player<Happy>("Hi, I hear a woman called Elena is staying here.")
    npc<Sad>("Yes she was staying here, but slightly over a week ago she was getting ready to go back. However she never managed to leave.")
    npc<Neutral>("My daughter Milli was playing near the west wall when she saw some shadowy figures jump out and grab her. Milli is upstairs if you wish to speak to her.")
}

npcOperate("Talk-to", "milli_rehnison") {//todo check what happens if you talk to her before talking to ted
    if (stages.contains(player.quest("plague_city"))) {//todo check dialogue for freed_elena
        npc<Quiz>("Have you found Elena yet?")
        player<Happy>("Yes, she's safe at home.")
        npc<Neutral>("I hope she comes and visits sometime.")
        player<Neutral>("Maybe.")
    } else {
        player<Neutral>("Hello. Your parents say you saw what happened to Elena...")
        npc<Sad>("*sniff* Yes I was near the south east corner when I saw Elena walking by. I was about to run to greet her when some men jumped out. They shoved a sack over her head and dragged her into a building.")
        player<Quiz>("Which building?")
        if (player.quest("plague_city") == "spoken_to_ted") {
            player["plague_city"] = "spoken_to_milli"
        }
        npc<Sad>("It was the boarded up building with no windows in the south east corner of West Ardougne.")
    }
}