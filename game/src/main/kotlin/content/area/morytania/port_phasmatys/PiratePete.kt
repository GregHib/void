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
import content.quest.questStage
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class PiratePete : Script {
    init {
        npcOperate("Talk-to", "pirate_pete*") { (target) ->
            when (questStage("rum_deal")) {
                0 -> {
                    player<Happy>("Hello there!")
                    npc<Neutral>("Mornin'.")
                    if (!questCompleted("zogre_flesh_eaters") || !hasMax(Skill.Slayer, 42)) {
                        player<Quiz>("Got any quests?")
                        npc<Shifty>("I may have a quest, but you don't look like you'd be able handle the kind of monsters I have problems with.")
                        return@npcOperate
                    }
                    return@npcOperate offerQuest(target)
                }
                1 -> {
                    player<Neutral>("Had any luck finding someone to slay the demon?")
                    return@npcOperate askAgain(target)
                }
                2 -> {
                    player<Happy>("Hello again!")
                    return@npcOperate greetAgain(target)
                }
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
                option("Not now") {
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

    // ===== Progress 0: The sob story =====

    private suspend fun Player.offerQuest(pete: NPC) {
        npc<Shifty>("Hey...you're an adventurer, right?")
        player<Happy>("Yes I am!")
        player<Neutral>("Got any quests for me?")
        npc<Neutral>("Yeah, I do, as a matter of fact!")
        npc<Neutral>("(Ahem.)")
        npc<Sad>("I am a poor, dispossessed nobleman, forced by circumstance to lurk in the middle of nowhere, soliciting help from passers-by.")
        npc<Neutral>("You see, my fiendish half-brother has seized my estates and forced me into exile.")
        npc<Neutral>("The simple lemon farmers suffer under his tyrannous yoke, and only a brave adventurer can lift his iron boot from the neck of the poor.")
        npc<Neutral>("To reclaim my lands I will need to have my family sword returned to me so that I may present it as proof of my rulership.")
        npc<Quiz>("Will you help me find my family sword?")
        choice {
            option("Yes!") {
                player<Neutral>("Yes! Your uncorroborated sob story has touched my heart. When do we set off?")
                set("rum_deal", "agreed_to_help")
                describeBarrelor(pete)
            }
            option("No.") {
                player<Neutral>("No, I don't think I'll help you out this time.")
                waitForSomeoneHeroic()
            }
        }
    }

    private suspend fun Player.describeBarrelor(pete: NPC) {
        npc<Happy>("You'll help! Wonderful!")
        npc<Sad>("But, alas, my half brother has a powerful ally, the mighty demon...")
        npc<Shifty>("Err...")
        npc<Neutral>("Err...Barrelor!")
        npc<Sad>("Yes, the mighty, fearsome, tall, deadly, oaken, round demon Barrelor the Destroyer.")
        player<Quiz>("Barrelor?")
        npc<Neutral>("That's what I said!")
        npc<Sad>("Barrelor is an awesome opponent, and to reclaim my family sword you will need to defeat him, for he guards it within the deadly Trapped Pit of Barrelor.")
        npc<Quiz>("Wanna give it a shot?")
        choice {
            option("Of course, I fear no demon!") {
                player<Neutral>("Of course, I fear no demon!")
                set("rum_deal", "slay_barrelor")
                offerPayment(pete)
            }
            option("Not a chance, this sounds too dangerous.") {
                player<Neutral>("Not a chance, this sounds too dangerous.")
                waitForSomeoneHeroic()
            }
        }
    }

    private suspend fun Player.offerPayment(pete: NPC) {
        npc<Neutral>("Atta ${boyOrGirl()}!")
        npc<Neutral>("When I am reinstated in my rightful place, I will not be a very wealthy man, as my half-brother has squandered my family fortune.")
        npc<Neutral>("However, I will gladly give you every bent penny of what is left, and starve in the gutter with my many adorable children if you say you will help me.")
        choice {
            option("Nonsense! Keep the money!") {
                player<Happy>("Nonsense! Keep the money! I will dispose of this evil half-brother of yours and leave you what little money is left to feed your family.")
                setOff(pete)
            }
            option("Great, I'll take the cash in used coins please.") {
                player<Neutral>("Great, I'll take the cash in used coins please.")
                npc<Shifty>("Er...")
                waitForSomeoneHeroic()
            }
        }
    }

    // ===== Progress 1: Turned him down, but came back =====

    private suspend fun Player.askAgain(pete: NPC) {
        npc<Quiz>("What?")
        npc<Happy>("Oh! Yes! The demon, with the oppression and stuff!")
        npc<Sad>("No, not yet. Why?")
        choice {
            option("Because I came to offer to help.") {
                player<Happy>("Because I came to offer to help.")
                set("rum_deal", "slay_barrelor")
                offerPayment(pete)
            }
            option("No reason.") {
                player<Neutral>("No reason. I was just in the area and I thought I would ask.")
                npc<Quiz>("I see...")
                waitForSomeoneHeroic()
            }
        }
    }

    // ===== Progress 2: Agreed to fight, hasn't set off yet =====

    private suspend fun Player.greetAgain(pete: NPC) {
        npc<Neutral>("Oh, it's you again.")
        npc<Neutral>("So, what brings you down here today?")
        choice {
            option("I've decided to help you for free.") {
                player<Happy>("I've decided to help you for free! I will dispose of this evil half-brother of yours and leave you what little money is left to feed your family.")
                setOff(pete)
            }
            option("No reason.") {
                player<Neutral>("No reason. I was just in the area and I thought I would ask.")
                npc<Quiz>("I see...")
                waitForSomeoneHeroic()
            }
        }
    }

    private suspend fun Player.waitForSomeoneHeroic() {
        npc<Neutral>("Look, I think I'll wait here for someone a little more... you know...")
        npc<Neutral>("...heroic.")
    }

    private suspend fun Player.setOff(pete: NPC) {
        npc<Neutral>("Wonderful! Just pick up your diversion and we'll leave!")
        player<Quiz>("What diversion?")
        face(Direction.SOUTH)
        introCutscene(pete)
    }

    /**
     * Pete knocks the player out and rows them to Braindeath Island; they come round in the
     * brewery just as Pete is reporting back to Captain Braindeath.
     */
    private suspend fun Player.introCutscene(pete: NPC) {
        delay(2)
        say("Ow!")
        pete.anim("mace_pummel")
        gfx("stun_long", delay = 20)
        sound("cudgel", delay = 15)
        open("fade_out")
        delay(4)
        val cutscene = startCutscene("rum_deal_intro", Region(8527))
        cutscene.onEnd {
            open("fade_out")
            delay(3)
            clearCamera()
            tele(2144, 5108, 1)
            face(Direction.NORTH)
            gfx("stun_long")
            sound("stunned")
        }
        tele(cutscene.tile(2142, 5105, 1), clearInterfaces = false)
        val braindeath = NPCs.add("captain_braindeath", cutscene.tile(2142, 5111, 1), Direction.NORTH)
        val peteCopy = NPCs.add("pirate_pete", cutscene.tile(2148, 5108, 1), Direction.NORTH)
        moveCamera(cutscene.tile(2143, 5107), height = 305)
        turnCamera(cutscene.tile(2143, 5111), height = 100)
        delay(2)
        open("fade_in")
        jingle("captain_braindeath")
        npc<Sad>("captain_braindeath", "Arrr... 'Tis lookin' bleak...", clickToContinue = false)
        overhead(braindeath, "Arrr... 'Tis lookin' bleak...")

        npc<Happy>("pirate_pete", "Cap'n!", clickToContinue = false)
        overhead(peteCopy, "Cap'n!")

        peteCopy.walkTo(cutscene.tile(2144, 5111, 1))
        npc<Neutral>("pirate_pete", "Good news Cap'n!", clickToContinue = false)
        overhead(peteCopy, "Good news Cap'n!")

        npc<Neutral>("pirate_pete", "I found us a hero down by the docks!", clickToContinue = false)
        overhead(peteCopy, "I found us a hero down", "by the docks!")

        peteCopy.face(braindeath)
        braindeath.face(peteCopy)
        npc<Quiz>("captain_braindeath", "Be they heroic, brave and true?", clickToContinue = false)
        overhead(braindeath, "Be they heroic, brave and", "true?")

        npc<Happy>("pirate_pete", "Aye! They also be gullible, tied up and unconscious!", clickToContinue = false)
        overhead(peteCopy, "Aye! They also be gullible,", "tied up and unconscious!")

        npc<Happy>(
            "pirate_pete",
            "They were willing to help out some random stranger with a good enough sob story, " +
                "so I smacked them with a bottle and rowed them over.",
            clickToContinue = false,
        )
        overhead(
            peteCopy,
            "They were willing to help",
            "out some random stranger",
            "with a good enough sob",
            "story, so I smacked them",
            "with a bottle and rowed",
            "them over.",
        )

        npc<Happy>("captain_braindeath", "Brilliant! The island's location will remain a secret!", clickToContinue = false)
        overhead(braindeath, "Brilliant! The island's", "location will remain a", "secret!")

        npc<Happy>("captain_braindeath", "Bring 'em here and wake 'em up.", clickToContinue = false)
        overhead(braindeath, "Bring 'em here and wake", "'em up.")

        npc<Happy>("captain_braindeath", "We may make it through this yet...", clickToContinue = false)
        overhead(braindeath, "We may make it through", "this yet...")

        cutscene.end()

        // The original hands straight over to Braindeath once the player comes round, rather than
        // leaving them stood in the brewery. onEnd drops them directly south of his spawn.
        delay(2)
        val captain = NPCs.findOrNull(tile.regionLevel, "captain_braindeath")
        if (captain != null) {
            talkWith(captain)
            interactNpc(captain, "Talk-to")
        }
    }

    private fun Player.boyOrGirl(): String = if (male) "boy" else "girl"

    /**
     * Cutscene speech. The chatbox line carries no continue arrow because the scene is timed
     * rather than clicked through, and the speaker repeats it overhead in the same short chunks
     * the original splits it into - overhead text is too narrow for a whole line.
     */
    private suspend fun Player.overhead(speaker: NPC, vararg chunks: String) {
        for ((index, chunk) in chunks.withIndex()) {
            if (index > 0) {
                delay(3)
            }
            speaker.say(chunk)
        }
        delay(3)
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
