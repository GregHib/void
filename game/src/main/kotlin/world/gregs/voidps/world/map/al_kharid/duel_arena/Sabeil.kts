package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import kotlin.random.Random

on<NPCOption>({ npc.id == "sabeil" && option == "Talk-to" }) { player: Player ->
    player.talkWith(npc) {
        player("cheerful", "Hi!")
        when (Random.nextInt(0, 14)) {
            0 -> {
                npc("cheerful", "Knock knock!")
                player("talking", "Who's there?")
                npc("cheerful", "Boo.")
                player("uncertain", "Boo who?")
                npc("laugh", "Don't cry, it's just me!")
            }
            1 -> {
                npc("sad", """
                    I wouldn't want to be the poor guy that has to clean up
                    after the duels.
                """)
                player("surprised", "Me neither.")
            }
            2 -> {
                npc("sad", "Hmph.")
            }
            3 -> {
                npc("cheerful", "My son just won his first duel!")
                player("cheerful", "Congratulations!")
                npc("cheerful", "He ripped his opponent in half!")
                player("surprised", "That's gotta hurt!")
                npc("cheerful", "He's only 10 as well!")
                player("cheerful", "You gotta start 'em young!")
            }
            4-> {
                npc("cheerful", "Hi! I'm here to watch the duels!")
                player("cheerful", "Me too!")
            }
            5-> {
                npc("cheerful", "Why did the skeleton burp?")
                player("uncertain", "I don't know?")
                npc("laugh", "'Cause it didn't have the guts to fart!")
            }
            6-> {
                npc("talking", """
                Did you know they think this place dates back to the
                second age?!
                """)
                player("talking", "Really?")
                npc("talking", "Yeah. The guy at the information kiosk was telling me.")
            }
            7-> {
                npc("cheerful", "What did the skeleton say before it ate?")
                player("uncertain", "I don't know?")
                npc("laugh", "Bone-appetit.")
            }
            8-> {
                npc("cheerful", "Ooh. This is exciting!")
                player("cheerful", "Yup!")
            }
            9-> {
                npc("cheerful", "Well. This beats doing the shopping!")
            }
            10-> {
                npc("angry", "Can't you see I'm watching the duels?")
                player("surprised", "I'm sorry!")
            }
            11-> {
                npc("cheerful", "Hi!")
            }
            12-> {
                npc("cheerful", "My favourite fighter is Mubariz!")
                player("uncertain", "The guy at the information kiosk?")
                npc("cheerful", "Yeah! He rocks!")
                player("roll_eyes", "Takes all sorts, I guess.")
            }
            13-> {
                npc("cheerful", "Waaaaassssssuuuuupp?!")
            }
        }
    }
}