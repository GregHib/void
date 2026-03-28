package content.area.morytania.port_phasmatys

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class PiratePete : Script {
    init {
        npcOperate("Talk-to", "pirate_pete*") { (target) ->
            if (!questCompleted("rum_deal")) {
                player<Happy>("Hello there!")
                npc<Neutral>("Mornin'.")
                player<Quiz>("Got any quests?")
                npc<Shifty>("I may have a quest, but you don't look like you'd be able handle the kind of monsters I have problems with.")
                return@npcOperate
            }
            player<Quiz>("Do I know you?")
            npc<Happy>("Yes, you owe me some money.")
            if (target.id == "pirate_pete_braindeath_island") {
                npc<Quiz>("Want a lift to Port Phasmatys?")
            } else {
                npc<Quiz>("Want a lift to Braindeath Island?")
                player<Quiz>("Well, possibly, but could I go to Mos Le'Harmless instead?")
                npc<Neutral>("Nope. It's Braindeath island or nothing. Interested?")
            }
            choice {
                option<Happy>("Okay!") {
                    if (target.id == "pirate_pete_braindeath_island") {
                        travel(target, Tile(3680, 3536))
                    } else {
                        travel(target, Tile(2162, 5114, 1))
                    }
                }
                option<Quiz>("Not now") {
                    player<Sad>("I'm getting an awful headache talking to you. Any idea why?")
                    npc<Shifty>("No idea whatsoever.")
                }
                option<Quiz>("Why do I get a headache every time I see you?") {
                    npc<Neutral>("Well, it's possibly the weight of all of your expensive items giving you a sore back.")
                    npc<Shifty>("As a doctor I can tell you that sometimes a bad back can manifest as a headache.")
                    player<Quiz>("You're a doctor?")
                    npc<Shifty>("I'm on a break.")
                    npc<Happy>("Regardless, I can tell you that if you hand me your most expensive items, then the pain will disappear.")
                    npc<Happy>("CoughonceyouturnaroundagainCough!")
                }
                option<Quiz>("Are you any relation to Party Pete?") {
                    npc<Sad>("Yes I am, he's my cousin.")
                    player<Quiz>("Well, you don't sound too happy about it. What happened?")
                    npc<Sad>("Well, I arranged with all my friends to have a party at his place.")
                    npc<Sad>("But then I humiliated myself by trying to dance with the knights.")
                    npc<Sad>("All of them collapsed on me in a horrific, jangling pile.")
                    npc<Sad>("I tried to salvage the night by having all the balloons come down...")
                    player<Quiz>("So what happened?")
                    npc<Angry>("I didn't know that someone had swapped the balloons with cannonballs!")
                    npc<Sad>("The casualties were horrific...")
                    npc<Sad>("That was the worst fifth birthday party in the history of the world.")
                    player<Neutral>("I'm sure it wasn't that bad.")
                    npc<Angry>("Not according to the Official History of Gielinor!")
                    npc<Sad>("Every edition... the pictures bring it all back...")
                    player<Shock>("Ouch...")
                }
            }
        }
    }

    private suspend fun Player.travel(target: NPC, tile: Tile) {
        if (random.nextBoolean()) {
            npc<Quiz>("Err... sure...")
            player<Quiz>("Why are you looking over my shoulder?")
        } else {
            npc<Happy>("Well I'll be more than happy to...")
            npc<Shock>("Egad! Did you see that?")
            player<Quiz>("What? Where?")
        }
        face(Direction.SOUTH)
        delay(2)
        say("Ow!")
        target.anim("mace_pummel")
        gfx("stun_long", delay = 20)
        open("fade_out")
        sound("cudgel", delay = 15)
        delay(3)
        tele(tile)
        delay(3)
        open("fade_in")
        face(Direction.NORTH)
        delay(2)
        player<Drunk>("Ooooh... my head...")
        npc<Quiz>("Are you ok?  You, errr...")
        when (random.nextInt(3)) {
            0 -> npc<Shifty>("...missed your mouth while drinking from a bottle. Hence the bottle-shaped bruises.")
            1 -> {
                npc<Shifty>("...hit your head on my oars while I was rowing over.")
                npc<Shifty>("Twice.")
            }
            else -> npc<Shifty>("...slipped and fell down some stairs.")
        }
        player<Neutral>("Wow... I'm lucky I wasn't seriously hurt!")
    }
}
