package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.Angry
import world.gregs.voidps.world.interact.dialogue.Frustrated
import world.gregs.voidps.world.interact.dialogue.Neutral
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

npcOperate("Talk-to", "kjell") {
    when (player.quest("gunnars_ground")) {
        "gunnars_ground", "completed" -> completed()
        "started" -> {
        }
        else -> unstarted()
    }
}

suspend fun CharacterContext.completed() {
    npc<Talk>(when (random.nextInt(0, 9)) {
        0 -> "...there's a place for us..."
        1 -> "...but I'd do anything for you..."
        2 -> "...you exploded into my heart..."
        3 -> "...love you like the stars above..."
        4 -> "...I dreamed your dream for you..."
        5 -> "...there's a place for us..."
        6 -> "...fall for chains of gold..."
        7 -> "...when you gonna realise..."
        else -> "...fall for pretty strangers..."
    })
    npc<Angry>("Blast!")
    choice {
        option<Neutral>("Having trouble there?") {
            npc<Angry>("I don't need the advice of an outerlander.")
            advice()
        }
        option<Neutral>("I'll leave you in peace.") {
        }
    }
}

suspend fun CharacterContext.advice() {
    choice {
        option<Neutral>("This music isn't very restful.") {
            npc<Angry>("Get out of here!")
        }
        option<Neutral>("Maybe you should take some lessons.") {
            npc<Angry>("Get out of here!")
        }
        option<Neutral>("I'll leave you in peace.") {
            npc<Angry>("Get out of here!")
        }
    }
}

suspend fun CharacterContext.unstarted() {
    npc<Frustrated>("Get out of here, outerlander!")
    choice {
        option<Neutral>("What is this place?") {
            npc<Frustrated>("The barbarian village. Go away.")
        }
        option<Neutral>("Who are you?") {
            npc<Frustrated>("My name is Kjell. Go away.")
        }
        option<Neutral>("What's in this hut you're guarding?") {
            npc<Frustrated>("Nothing yet. Once there is, no one will get in or out! Now, Go away!")
        }
        option<Neutral>("Goodbye then.") {
        }
    }
}
