package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc

on<NPCOption>({ operate && target.id == "kjell" && option == "Talk-to" }) { player: Player ->
    when (player.quest("gunnars_ground")) {
        "gunnars_ground", "completed" -> {
            completed()
        }
        "started" -> {
        }
        else -> unstarted()
    }
}

suspend fun CharacterContext.completed() {
    when (random.nextInt(0, 8)) {
        0 -> {
            npc<Talk>("...there's a place for us...")
        }
        1 -> {
            npc<Talk>("...but I'd do anything for you...")
        }
        2 -> {
            npc<Talk>("...you exploded into my heart...")
        }
        3 -> {
            npc<Talk>("...love you like the stars above...")
        }
        4 -> {
            npc<Talk>("...I dreamed your dream for you...")
        }
       5 -> {
            npc<Talk>("...there's a place for us...")
        }
        5 -> {
            npc<Talk>("...fall for chains of gold...")
        }
        6 -> {
            npc<Talk>("...when you gonna realise...")
        }
        7 -> {
            npc<Talk>("...fall for pretty strangers...")
        }
    }
    npc<Furious>("Blast!")
    choice {
        option<Talking>("Having trouble there?") {
            npc<Furious>("I don't need the advice of an outerlander.")
        }
        option<Talking>("I'll leave you in peace.") {
        }
    }
}

suspend fun CharacterContext.advice() {
    choice {
        option<Talking>("This music isn't very restful.") {
            npc<Furious>("Get out of here!")
        }
        option<Talking>("Maybe you should take some lessons.") {
            npc<Furious>("Get out of here!")
        }
        option<Talking>("I'll leave you in peace.") {
            npc<Furious>("Get out of here!")
        }
    }
}

suspend fun CharacterContext.unstarted() {
    npc<Angry>("Get out of here, outerlander!")
    choice {
        option<Talking>("What is this place?") {
            npc<Angry>("The barbarian village. Go away.")
        }
        option<Talking>("Who are you?") {
            npc<Angry>("My name is Kjell. Go away.")
        }
        option<Talking>("What's in this hut you're guarding?") {
            npc<Angry>("Nothing yet. Once there is, no one will get in or out! Now, Go away!")
        }
        option<Talking>("Goodbye then.") {
        }
    }
}
