package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import kotlin.random.Random

on<NPCOption>({ npc.id == "zahwa" && option == "Talk-to" }) { player: Player ->
    when (Random.nextInt(0, 6)) {
        0 -> {
            player("cheerful", "Hi!")
            player("cheerful", "Hi!")
        }
        1 -> {
            player("uncertain", "Are you alright?")
            npc<Angry>("Do I look alright?!")
        }
        2 -> {
            player("uncertain", "Are you alright?")
            npc<Cheerful>("Yeh. The nurses here are...wonderful!")
        }
        3 -> {
            player("cheerful", "Hi!")
            npc<Angry>("I could've 'ad 'im!")
            player("surprised", "Er...")
            npc<Angry>("I was robbed!")
            player("roll_eyes", "Right.")
            npc<Angry>("It was rigged I tell you!")
            player("roll_eyes", "Uh huh.")
            npc<Angry>("Leave me alone!")
        }
        4-> {
            player("uncertain", "Are you alright?")
            npc<Surprised>("NURSE!")
        }
        5-> {
            player("uncertain", "Are you alright?")
            npc<Angry>("It's just a flesh wound!")
        }
        6-> {
            player("uncertain", "Are you alright?")
            npc<Sad>("Can't....go....on!")
            npc<Sad>("Leave me behind!")
            player("uncertain", "I'll leave you here, OK?")
            npc<Uncertain>("Oh. OK.")
        }
        7 -> {
            player("cheerful", "Hi!")
            player("sad", "Ughhhh....")
        }
    }
}