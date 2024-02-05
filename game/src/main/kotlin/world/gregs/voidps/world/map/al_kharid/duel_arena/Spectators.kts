package world.gregs.voidps.world.map.al_kharid.duel_arena

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player

fun isSpectator(id: String) = id == "afrah" || id == "dalal" || id == "jadid" || id == "jeed" || id == "ima" || id == "sabeil"

npcOperate({ isSpectator(target.id) && option == "Talk-to" }) { player: Player ->
    player<Cheerful>("Hi!")
    when (random.nextInt(0, 14)) {
        0 -> {
            npc<Cheerful>("Knock knock!")
            player<Talking>("Who's there?")
            npc<Cheerful>("Boo.")
            player<Uncertain>("Boo who?")
            npc<Laugh>("Don't cry, it's just me!")
        }
        1 -> {
            npc<Sad>("I wouldn't want to be the poor guy that has to clean up after the duels.")
            player<Surprised>("Me neither.")
        }
        2 -> npc<Sad>("Hmph.")
        3 -> {
            npc<Cheerful>("My son just won his first duel!")
            player<Cheerful>("Congratulations!")
            npc<Cheerful>("He ripped his opponent in half!")
            player<Surprised>("That's gotta hurt!")
            npc<Cheerful>("He's only 10 as well!")
            player<Cheerful>("You gotta start 'em young!")
        }
        4 -> {
            npc<Cheerful>("Hi! I'm here to watch the duels!")
            player<Cheerful>("Me too!")
        }
        5 -> {
            npc<Cheerful>("Why did the skeleton burp?")
            player<Uncertain>("I don't know?")
            npc<Laugh>("'Cause it didn't have the guts to fart!")
        }
        6 -> {
            npc<Talking>("Did you know they think this place dates back to the second age?!")
            player<Talking>("Really?")
            npc<Talking>("Yeah. The guy at the information kiosk was telling me.")
        }
        7 -> {
            npc<Cheerful>("What did the skeleton say before it ate?")
            player<Uncertain>("I don't know?")
            npc<Laugh>("Bone-appetit.")
        }
        8 -> {
            npc<Cheerful>("Ooh. This is exciting!")
            player<Cheerful>("Yup!")
        }
        9 -> npc<Cheerful>("Well. This beats doing the shopping!")
        10 -> {
            npc<Angry>("Can't you see I'm watching the duels?")
            player<Surprised>("I'm sorry!")
        }
        11 -> npc<Cheerful>("Hi!")
        12 -> {
            npc<Cheerful>("My favourite fighter is Mubariz!")
            player<Uncertain>("The guy at the information kiosk?")
            npc<Cheerful>("Yeah! He rocks!")
            player<RollEyes>("Takes all sorts, I guess.")
        }
        13 -> npc<Cheerful>("Waaaaassssssuuuuupp?!")
    }
}