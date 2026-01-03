package content.area.misthalin.barbarian_village

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.quest.Cutscene
import content.quest.quest
import content.quest.questComplete
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile

class Gudrun : Script {

    val objects: GameObjects by inject()

    val npcs: NPCs by inject()
    val region = Region(12341)

    init {
        npcOperate("Talk-to", "gudrun*") {
            when (quest("gunnars_ground")) {
                "gunnars_ground" -> gunnarsGround()
                "recital" -> recital()
                "poem" -> poem()
                "tell_dororan", "write_poem", "more_poem", "one_more_poem", "poem_done" -> {
                    npc<Neutral>("If there's anything you can do to make papa see sense, please do it.")
                }
                "tell_gudrun" -> whatHeSay()
                "meet_chieftain" -> {
                    npc<Neutral>("If there's anything you can do to make papa see sense, please do it.")
                    meetChieftain()
                }
                "show_gudrun" -> showGudrun()
                else -> unstarted()
            }
        }

        npcOperate("Talk-to", "gudrun_after_quest") {
            when (quest("gunnars_ground")) {
                "completed" -> {
                    npc<Happy>("Hello!")
                    choice {
                        option<Idle>("I want to ask you something.") {
                            npc<Quiz>("Of course, what is it?")
                            menu()
                        }
                        option<Idle>("Just passing through.") {
                            npc<Happy>("Goodbye!")
                        }
                    }
                }
                else -> message("error")
            }
        }
    }

    suspend fun Player.recital() {
        npc<Quiz>("Are you ready for the recital?")
        choice {
            option<Idle>("Yes.") {
                cutscene()
            }
            option<Idle>("Not right now.") {
            }
        }
    }

    suspend fun Player.cutscene() {
        open("fade_out")
        val cutscene = startCutscene("gudrun", region)
        cutscene.onEnd {
            open("fade_out")
            delay(3)
            tele(3081, 3416)
            clearCamera()
            clearAnim()
        }
        delay(4)
        tele(cutscene.tile(3078, 3435), clearInterfaces = false)
        val dororan = npcs.add("dororan_cutscene", cutscene.tile(3079, 3435), Direction.SOUTH)
        dororan.anim("dororan_lean_on_door")
        anim("player_lean_on_door")
        dororan.face(Direction.NORTH)
        face(Direction.NORTH)
        moveCamera(cutscene.tile(3079, 3430), 280)
        turnCamera(cutscene.tile(3079, 3436), 230)
        delay(2)
        open("fade_in")
        npc<Neutral>("dororan_cutscene", "How long have they been in there?")
        anim("player_calm_doroan")
        choice {
            option<Idle>("They're just starting.") {
                cutsceneMenu(cutscene)
            }
            option<Idle>("You're late.") {
                cutsceneMenu(cutscene)
            }
        }
    }

    suspend fun Player.cutsceneMenu(cutscene: Cutscene) {
        npc<Disheartened>("dororan_cutscene", "This isn't going to work.")
        choice {
            option<Idle>("Why's that?") {
                anim("player_calm_doroan")
                cutsceneMenu2(cutscene)
            }
            option<Idle>("You're so pessimistic.") {
                anim("player_calm_doroan")
                cutsceneMenu2(cutscene)
            }
        }
    }

    suspend fun Player.cutsceneMenu2(cutscene: Cutscene) {
        npc<Cry>("dororan_cutscene", "What was I thinking? You should go in there and stop them before Gudrun makes a fool of herself.")
        choice {
            option<Idle>("Okay, I will.") {
                anim("player_calm_doroan")
                npc<Disheartened>("dororan_cutscene", "No! Wait, stay here, it's too late now. We'll just have to see how it turns out.")
                cutsceneMenu3(cutscene)
            }
            option<Idle>("Don't be silly.") {
                anim("player_calm_doroan")
                npc<Disheartened>("dororan_cutscene", "You're right, it's too late now. We'll just have to see how it turns out.")
                cutsceneMenu3(cutscene)
            }
        }
    }

    suspend fun Player.cutsceneMenu3(cutscene: Cutscene) {
        npc<Disheartened>("dororan_cutscene", "I can't hear what's happening. Can you hear what's happening?")
        anim("player_calm_doroan")
        player<Neutral>("Gunthor is laughing at something.")
        npc<Sad>("dororan_cutscene", "He's probably considering the various tortures he has planned for me.")
        anim("player_calm_doroan")
        choice {
            option<Idle>("Why would he do that?") {
                cutsceneMenu4(cutscene)
            }
            option<Idle>("Now you're just being ridiculous.") {
                cutsceneMenu4(cutscene)
            }
        }
    }

    suspend fun Player.cutsceneMenu4(cutscene: Cutscene) {
        npc<Neutral>("dororan_cutscene", "The poem says you can honour your ancestors by settling peacefully on the land they conquered.")
        npc<Disheartened>("dororan_cutscene", "He'll probably just find it insulting.")
        anim("player_calm_doroan")
        choice {
            option<Idle>("Now's your chance to find out.") {
                cutscenePart2(cutscene)
            }
            option<Idle>("You're doomed.") {
                cutscenePart2(cutscene)
            }
        }
    }

    suspend fun Player.cutscenePart2(cutscene: Cutscene) {
        open("fade_out")
        delay(3)
        npcs.clear(cutscene.instance.toLevel(0))
        clearAnim()
        delay(1)
        tele(cutscene.tile(3083, 3426), clearInterfaces = false)
        face(Direction.WEST)
        val dororan = npcs.add("dororan_cutscene", cutscene.tile(3082, 3428), Direction.SOUTH)
        val gudrun = npcs.add("gudrun_cutscene", cutscene.tile(3080, 3426), Direction.SOUTH)
        val kjell = npcs.add("kjell_cutscene", cutscene.tile(3077, 3426), Direction.SOUTH)
        val gunthor = npcs.add("chieftain_gunthor_cutscene", cutscene.tile(3079, 3425), Direction.SOUTH)
        val haakon = npcs.add("haakon_the_champion_cutscene", cutscene.tile(3078, 3425), Direction.SOUTH)
        dororan.face(gudrun)
        moveCamera(cutscene.tile(3079, 3419), 400)
        turnCamera(cutscene.tile(3079, 3426), 150)
        delay(2)
        open("fade_in")
        npc<Sad>("dororan_cutscene", "I hope they at least give me a decent burial.")
        gunthor.anim("gunthor_announcement")
        npc<Frustrated>("chieftain_gunthor_cutscene", "Freemen! Freemen! I have an announcement!")
        npc<Frustrated>("kjell_cutscene", "Hear the chieftain speak! Hear him!")
        npc<Frustrated>("chieftain_gunthor_cutscene", "We have always borne the legacy of our ancestors, and we have borne it with honour!")
        npc<Mad>("kjell_cutscene", "FOR GUNNAR!")
        npc<Frustrated>("chieftain_gunthor_cutscene", "And though we honour them still, the time of our ancestors is past. this is the time of Gunthor!")
        npc<Mad>("haakon_the_champion_cutscene", "FOR GUNNAR!")
        npc<Frustrated>("chieftain_gunthor_cutscene", "Gunthor says: This is Gunnar's ground, bought with blood! Let it remain Gunnar's ground forever! Here we settle!")
        npc<Frustrated>("chieftain_gunthor_cutscene", "GUNNAR'S GROUND!")
        kjell.anim("kjell_cheer")
        haakon.anim("haakon_cheer")
        npc<Mad>("haakon_the_champion_cutscene", "GUNNAR'S GROUND!")
        open("fade_out")
        delay(4)
        val npc = listOf(kjell, gunthor, haakon, gudrun, dororan)
        for (remove in npc) {
            npcs.remove(remove)
        }
        moveCamera(cutscene.tile(3084, 3421), 350)
        turnCamera(cutscene.tile(3082, 3426), 250)
        val gudrunHugging = objects.add("gudrun_and_dororan", cutscene.tile(3082, 3426), shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1)
        open("fade_in")
        npc<Happy>("gudrun_cutscene", "That was brilliant! I must know who wrote that poem.")
        npc<Disheartened>("dororan_cutscene", "Um, that would be me. Hello")
        npc<Pleased>("gudrun_cutscene", "That line about beauty was for me, wasn't it?")
        npc<Sad>("dororan_cutscene", "Uh, Yes.")
        npc<Happy>("gudrun_cutscene", "You're the mystery poet who sent me the gold ring!")
        npc<Cry>("dororan_cutscene", "Sorry.")
        npc<Happy>("gudrun_cutscene", "I had no idea dwarves could be so romantic! Come here! ")
        delay(2)
        gudrunHugging.anim("gudrun_hugging")
        delay(4)
        queue.clear("gunnars_ground_cutscene_end")
        cutscene.end()
        set("gunnars_ground", "gunnars_ground")
        set("kjell", "guitar")
        set("dororan", "hidden")
        set("dororan_after_cutscene", "shown")
        set("gudrun", "hidden")
        set("gudrun_after_cutscene", "shown")
        val gudrunAfter = npcs[Tile(3082, 3417)].firstOrNull { it.id == "gudrun_after_cutscene" }
        if (gudrunAfter != null) {
            interactNpc(gudrunAfter, "Talk-to")
        } else {
            gunnarsGround()
        }
    }

    suspend fun Player.gunnarsGround() {
        npc<Happy>("Papa was so impressed by Dororan's poem, he's made him the village poet!")
        npc<Happy>("dororan_after_cutscene2", "I'm more then a little surprised! He even gave me a house to live in!")
        npc<Pleased>("Our people's tradition is that the tribe provides lodging for the poet.")
        npc<Laugh>("dororan_after_cutscene2", "It's huge!")
        npc<Happy>("It's not in the village. It's east of here: across the river and north of the road on the way to Varrock. It's a big house with roses outside.")
        npc<Pleased>("dororan_after_cutscene2", "I think Gunthor wants to keep me close, but not too close. Oh, I found something there for you!")
        npc<Happy>("dororan_after_cutscene2", "Whoever lived there before left a dozen pairs of boots in the attic.")
        npc<Neutral>("dororan_after_cutscene2", "I picked out a pair for you to thank you for all your help.")
        npc<Happy>("dororan_after_cutscene2", "Underneath them all was this magic lamb. You should have it as well!")
        npc<Happy>("We're going to the new house. You should come and visit!")
        npc<Happy>("dororan_after_cutscene2", "Yes, we'll see you there!")
        choice {
            option<Idle>("I'll see you soon.") {
                finishQuest()
            }
            option<Idle>("I'll consider dropping in.") {
                finishQuest()
            }
        }
    }

    suspend fun Player.poem() {
        if (holdsItem("gunnars_ground")) {
            npc<Quiz>("What have you got there?")
            player<Pleased>("Another gift from your mysterious suitor.")
            npc<Quiz>("A scroll?")
            player<Neutral>("It's a poem; a story to convince your father to settle down. You could recite it to him.")
            npc<Amazed>("Let me see that.")
            anim("hand_over_item")
            item("gunnars_ground", 400, "You show Gudrun the poem")
            npc<Neutral>("'Gunnar's Ground'")
            npc<Pleased>("Yes! I think this could work. I'll go to the longhouse right away!")
            inventory.remove("gunnars_ground")
            set("gunnars_ground", "recital")
            cutscene()
        } else {
            npc<Quiz>("What is it?")
            player<Sad>("I was meant to bring you a poem, but I seem to have mislaid it.")
        }
    }

    suspend fun Player.whatHeSay() {
        npc<Quiz>("What did he say?")
        player<Neutral>("He mentioned someone called Gunnar, and that you should think about his feelings.")
        npc<Angry>("By the eyeballs of Guthix! Always Gunnar!")
        choice {
            option<Idle>("Who is Gunnar?") {
                npc<Frustrated>("He was my great-grandpapa! He founded this village a hundred years ago.")
                fathersAttitude()
            }
            option<Idle>("What should we do now?") {
                npc<Frustrated>("I don't know. Maybe your mystery man has some ideas.")
                set("gunnars_ground", "tell_dororan")
                player<Pleased>("I'll ask him.")
            }
        }
    }

    suspend fun Player.fathersAttitude() {
        choice {
            option<Idle>("You don't seem to share your father's attitude towards him.") {
                npc<Frustrated>("I think there's a difference between respecting my ancestors and obsessing over them. Papa thinks whatever stupid war Gunnar fought is still going on.")
                npc<Frustrated>("I don't know. Maybe your mystery man has some ideas.")
                set("gunnars_ground", "tell_dororan")
                player<Pleased>("I'll ask him.")
            }
            option<Idle>("What should we do now?") {
                npc<Frustrated>("I don't know. Maybe your mystery man has some ideas.")
                set("gunnars_ground", "tell_dororan")
                player<Pleased>("I'll ask him.")
            }
        }
    }

    suspend fun Player.meetChieftain() {
        choice {
            option<Idle>("Where is he?") {
                npc<Neutral>("In the longhouse at the north end of the village, drinking and shouting.")
            }
            option<Idle>("I'll see what I can do.") {
            }
        }
    }

    suspend fun Player.showGudrun() {
        npc<Frustrated>("kjell_sword", "Gudrun! You caught enough fish?")
        npc<Frustrated>("Yes! I have plenty of fish!")
        npc<Frustrated>("kjell_sword", "Your father needs many fish to feed the freemen!")
        npc<Frustrated>("I know!")
        npc<Frustrated>("kjell_sword", "Maybe you sneak off to the outerlander city again? Buy fish in market, instead of catching them?")
        npc<Frustrated>("Shut up! I'm much better at fishing than you.")
        npc<Frustrated>("kjell_sword", "You are not!")
        npc<Frustrated>("Just guard the hut like chieftain told you to!")
        npc<Frustrated>("kjell_sword", "Fine!")
        npc<Frustrated>("Stupid barbarian.")
        npc<Quiz>("Sorry about that, stranger. Did you want something?.")
        player<Quiz>("Are you Gudrun?")
        npc<Neutral>("Yes.")
        if (holdsItem("dororans_engraved_ring")) {
            player<Pleased>("This is for you.")
            anim("hand_over_item")
            item("dororans_engraved_ring", 400, "You show Gudrun the ring.")
            npc<Happy>("It's lovely! There's something written on it:")
            npc<Pleased>("'Gudrun the Fair, Gudrun the Fiery.' Is it about me?")
            choice {
                option<Idle>("Yes.") {
                    aboutRing()
                }
                option<Idle>("Presumable.") {
                    aboutRing()
                }
            }
        } else {
            player<Disheartened>("I was meant to bring you a ring but I seem to have mislaid it.")
        }
    }

    suspend fun Player.aboutRing() {
        npc<Pleased>("This is beautiful gift, stranger. Thank you.")
        choice {
            option<Idle>("The ring isn't from me!") {
                whoFrom()
            }
            option<Idle>("It should belong to someone just as beautiful.") {
                npc<Pleased>("That's very flattering! You look like an adventurer, though?")
                thatsRight()
            }
        }
    }

    suspend fun Player.thatsRight() {
        choice {
            option<Pleased>("That's right.") {
                npc<Disheartened>("I'm sorry, I could never get involved with an adventurer.")
                whoFrom()
            }
            option<Neutral>("Some call me that.") {
                npc<Disheartened>("I'm sorry, I could never get involved with an adventurer.")
                whoFrom()
            }
        }
    }

    suspend fun Player.whoFrom() {
        npc<Shock>("Oh! Who is it from?")
        choice {
            option<Idle>("A great poet.") {
                npc<Pleased>("A tale-teller? A bard? My people have great respect from poets.")
                outsideVillage()
            }
            option<Idle>("A secret admirer.") {
                npc<Pleased>("Does that really happen? How exciting!")
                outsideVillage()
            }
            option<Idle>("A short suitor.") {
                npc<Quiz>("What?")
                player<Neutral>("A petite paramour.")
                npc<Amazed>("What?")
                player<Neutral>("A concise courter!")
                outsideVillage()
            }
        }
    }

    suspend fun Player.outsideVillage() {
        npc<Quiz>("This man, he is from outside the village?")
        player<Neutral>("Yes.")
        npc<Pleased>("I would love to leave the village and be romanced by exotic, handsome, outerlander men. There's a problem, though.")
        player<Quiz>("What's that?")
        npc<Disheartened>("My papa, the chieftain. He would never let an outerlander pursue me.")
        player<Quiz>("Why not?")
        npc<Neutral>("He thinks all your people are our enemies.")
        choice {
            option<Idle>("So, you want me to talk to your father?") {
                npc<Quiz>("I suppose that might work.")
                reasonWithHim()
            }
            option<Idle>("So, you want me to kill your father?") {
                npc<Shock>("What? no! Maybe...you could just try talking to him.")
                reasonWithHim()
            }
        }
    }

    suspend fun Player.reasonWithHim() {
        npc<Disheartened>("I've tried to reason with him, but he's impossible! Maybe he'll listen to you. I know some of the others feel the same, but they're loyal to papa.")
        set("gunnars_ground", "meet_chieftain")
        inventory.remove("dororans_engraved_ring")
        meetChieftain()
    }

    suspend fun Player.unstarted() {
        npc<Pleased>("Can I help you, stranger?")
        npc<Frustrated>("kjell_sword", "Why are you talking to that outerlander?")
        npc<Frustrated>("It's none of your business, Kjell! Just guard the hut!")
        npc<Amazed>("Sorry about that. Did you want something?")
        choice {
            option<Idle>("What is this place?") {
                whatIsThisPlace()
            }
            option<Idle>("Who are you?") {
                whoAreYou()
            }
            option<Idle>("Actually, no. Goodbye") {
            }
        }
    }

    suspend fun Player.whoAreYou() {
        npc<Pleased>("My name is Gudrun. My father, Gunthor, is chieftain of the village.")
        choice {
            option<Happy>("What is this place?") {
                whatIsThisPlace()
            }
            option<Pleased>("Goodbye.") {
            }
        }
    }

    suspend fun Player.whatIsThisPlace() {
        npc<Pleased>("Outerlanders call this the barbarian village. It doesn't have a name because...it's complicated.")
        npc<Neutral>("if you want to know more, you should talk to Hunding. He's up in the tower at the east entrance.")
        choice {
            option<Idle>("Who are you?") {
                whoAreYou()
            }
            option<Pleased>("Goodbye.") {
            }
        }
    }

    suspend fun Player.finishQuest() {
        npc<Happy>("dororan_after_cutscene2", "Goodbye!")
        npc<Happy>("Goodbye!")
        if (inventory.spaces < 2) {
            statement("You don't have room for the two reward items. Speak to Gudrun or Dororan again when you have room.")
            return
        }
        open("fade_out")
        delay(4)
        set("dororan_after_cutscene", "hidden")
        set("gudrun_after_cutscene", "hidden")
        set("dororan_after_quest", "shown")
        set("gudrun_after_quest", "shown")
        open("fade_in")
        questComplete()
    }

    fun Player.questComplete() {
        AuditLog.event(this, "quest_completed", "gunnars_ground")
        jingle("quest_complete_3")
        set("gunnars_ground", "completed")
        inc("quest_points", 5)
        experience.add(Skill.Crafting, 300.0)
        softQueue("quest_complete", 1) {
            questComplete(
                "Gunnar's Ground",
                "5 Quest Points",
                "300 Crafting XP.",
                "Antique lamp.",
                "Swanky boots.",
                item = "gunnars_ground",
            )
        }
        inventory.add("antique_lamp_gunnars_ground")
        inventory.add("swanky_boots")
    }

    suspend fun Player.menu() {
        choice {
            option<Idle>("How are things with Dororan?") {
                npc<Pleased>("I really like him. he's funny, vulnerable and nothing like my people.")
                choice {
                    option<Idle>("You're going to stay together then?") {
                        npc<Happy>("Of course!")
                        elseGoodbye()
                    }
                    option<Idle>("I want to ask about something else.") {
                        npc<Quiz>("Of course, what is it?")
                        menu()
                    }
                    option<Idle>("Goodbye.") {
                        npc<Happy>("Oh, Goodbye!")
                    }
                }
            }
            option<Idle>("Where did this house come from?") {
                npc<Idle>("I don't know. Papa said the previous owners left it to him. I don't know why they would do that.")
                theory()
            }
            option<Idle>("Goodbye.") {
                npc<Happy>("Oh, Goodbye!")
            }
        }
    }

    suspend fun Player.theory() {
        choice {
            option<Idle>("Do you have a theory?") {
                npc<Amazed>("Gunnar always said 'A warrior does not barter; he simply takes!'. I think papa bought the house, but doesn't want anyone to know.")
                elseGoodbye()
            }
            option<Idle>("I want to ask about something else.") {
                npc<Quiz>("Of course, what is it?")
                menu()
            }
            option<Idle>("Goodbye.") {
                npc<Happy>("Oh, Goodbye!")
            }
        }
    }

    suspend fun Player.elseGoodbye() {
        choice {
            option<Idle>("I want to ask about something else.") {
                npc<Quiz>("Of course, what is it?")
                menu()
            }
            option<Idle>("Goodbye.") {
                npc<Happy>("Oh, Goodbye!")
            }
        }
    }
}
