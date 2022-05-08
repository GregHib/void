package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import kotlin.random.Random

on<NPCOption>({  npc.id == "zahwa" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        when (Random.nextInt(0, 6)) {
            0 -> {
                player("cheerful", "Hi!")
                player("cheerful", "Hi!")
            }
            1 -> {
                player("uncertain", "Are you alright?")
                npc("angry", "Do I look alright?!")
            }
            2 -> {
                player("uncertain", "Are you alright?")
                npc("cheerful", "Yeh. The nurses here are...wonderful!")
            }
            3 -> {
                player("cheerful", "Hi!")
                npc("angry", "I could've 'ad 'im!")
                player("surprised", "Er...")
                npc("angry", "I was robbed!")
                player("roll_eyes", "Right.")
                npc("angry", "It was rigged I tell you!")
                player("roll_eyes", "Uh huh.")
                npc("angry", "Leave me alone!")
            }
            4-> {
                player("uncertain", "Are you alright?")
                npc("surprised", "NURSE!")
            }
            5-> {
                player("uncertain", "Are you alright?")
                npc("angry", "It's just a flesh wound!")
            }
            6-> {
                player("uncertain", "Are you alright?")
                npc("sad", "Can't....go....on!")
                npc("sad", "Leave me behind!")
                player("uncertain", "I'll leave you here, OK?")
                npc("uncertain", "Oh. OK.")
            }
            7 -> {
                player("cheerful", "Hi!")
                player("sad", "Ughhhh....")
            }
        }
    }
}