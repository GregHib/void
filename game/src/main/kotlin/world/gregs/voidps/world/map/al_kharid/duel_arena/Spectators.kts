package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import kotlin.random.Random

fun isSpectator(id: String) = id == "afrah" || id == "dalal" || id == "jadid" || id == "jeed" || id == "ima" || id == "sabeil"

on<NPCOption>({ isSpectator(npc.id) && option == "Talk-to" }) { player: Player ->
    player("cheerful", "Hi!")
    when (Random.nextInt(0, 14)) {
        0 -> {
            npc<Cheerful>("Knock knock!")
            player("talking", "Who's there?")
            npc<Cheerful>("Boo.")
            player("uncertain", "Boo who?")
            npc<Laugh>("Don't cry, it's just me!")
        }
        1 -> {
            npc<Sad>("""
                I wouldn't want to be the poor guy that has to clean up
                after the duels.
            """)
            player("surprised", "Me neither.")
        }
        2 -> npc<Sad>("Hmph.")
        3 -> {
            npc<Cheerful>("My son just won his first duel!")
            player("cheerful", "Congratulations!")
            npc<Cheerful>("He ripped his opponent in half!")
            player("surprised", "That's gotta hurt!")
            npc<Cheerful>("He's only 10 as well!")
            player("cheerful", "You gotta start 'em young!")
        }
        4 -> {
            npc<Cheerful>("Hi! I'm here to watch the duels!")
            player("cheerful", "Me too!")
        }
        5 -> {
            npc<Cheerful>("Why did the skeleton burp?")
            player("uncertain", "I don't know?")
            npc<Laugh>("'Cause it didn't have the guts to fart!")
        }
        6 -> {
            npc<Talking>("""
                Did you know they think this place dates back to the
                second age?!
            """)
            player("talking", "Really?")
            npc<Talking>("Yeah. The guy at the information kiosk was telling me.")
        }
        7 -> {
            npc<Cheerful>("What did the skeleton say before it ate?")
            player("uncertain", "I don't know?")
            npc<Laugh>("Bone-appetit.")
        }
        8 -> {
            npc<Cheerful>("Ooh. This is exciting!")
            player("cheerful", "Yup!")
        }
        9 -> npc<Cheerful>("Well. This beats doing the shopping!")
        10 -> {
            npc<Angry>("Can't you see I'm watching the duels?")
            player("surprised", "I'm sorry!")
        }
        11 -> npc<Cheerful>("Hi!")
        12 -> {
            npc<Cheerful>("My favourite fighter is Mubariz!")
            player("uncertain", "The guy at the information kiosk?")
            npc<Cheerful>("Yeah! He rocks!")
            player("roll_eyes", "Takes all sorts, I guess.")
        }
        13 -> npc<Cheerful>("Waaaaassssssuuuuupp?!")
    }
}