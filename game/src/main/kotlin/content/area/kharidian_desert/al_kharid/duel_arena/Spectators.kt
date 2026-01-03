package content.area.kharidian_desert.al_kharid.duel_arena

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.random

class Spectators : Script {

    init {
        npcOperate("Talk-to", "afrah,dalal,jadid,jeed,ima,sabeil") {
            player<Happy>("Hi!")
            when (random.nextInt(0, 14)) {
                0 -> {
                    npc<Happy>("Knock knock!")
                    player<Idle>("Who's there?")
                    npc<Happy>("Boo.")
                    player<Confused>("Boo who?")
                    npc<Laugh>("Don't cry, it's just me!")
                }
                1 -> {
                    npc<Disheartened>("I wouldn't want to be the poor guy that has to clean up after the duels.")
                    player<Shock>("Me neither.")
                }
                2 -> npc<Disheartened>("Hmph.")
                3 -> {
                    npc<Happy>("My son just won his first duel!")
                    player<Happy>("Congratulations!")
                    npc<Happy>("He ripped his opponent in half!")
                    player<Shock>("That's gotta hurt!")
                    npc<Happy>("He's only 10 as well!")
                    player<Happy>("You gotta start 'em young!")
                }
                4 -> {
                    npc<Happy>("Hi! I'm here to watch the duels!")
                    player<Happy>("Me too!")
                }
                5 -> {
                    npc<Happy>("Why did the skeleton burp?")
                    player<Confused>("I don't know?")
                    npc<Laugh>("'Cause it didn't have the guts to fart!")
                }
                6 -> {
                    npc<Idle>("Did you know they think this place dates back to the second age?!")
                    player<Idle>("Really?")
                    npc<Idle>("Yeah. The guy at the information kiosk was telling me.")
                }
                7 -> {
                    npc<Happy>("What did the skeleton say before it ate?")
                    player<Confused>("I don't know?")
                    npc<Laugh>("Bone-appetit.")
                }
                8 -> {
                    npc<Happy>("Ooh. This is exciting!")
                    player<Happy>("Yup!")
                }
                9 -> npc<Happy>("Well. This beats doing the shopping!")
                10 -> {
                    npc<Frustrated>("Can't you see I'm watching the duels?")
                    player<Shock>("I'm sorry!")
                }
                11 -> npc<Happy>("Hi!")
                12 -> {
                    npc<Happy>("My favourite fighter is Mubariz!")
                    player<Confused>("The guy at the information kiosk?")
                    npc<Happy>("Yeah! He rocks!")
                    player<Bored>("Takes all sorts, I guess.")
                }
                13 -> npc<Happy>("Waaaaassssssuuuuupp?!")
            }
        }
    }
}
