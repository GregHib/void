package content.area.misthalin.barbarian_village

import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.event.Context
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectShape
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.LogoutBehaviour
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.activity.quest.sendQuestComplete
import world.gregs.voidps.world.activity.quest.startCutscene
import world.gregs.voidps.world.activity.quest.stopCutscene
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.*
import world.gregs.voidps.world.interact.entity.sound.playJingle

val objects: GameObjects by inject()

npcOperate("Talk-to", "gudrun*") {
    when (player.quest("gunnars_ground")) {
        "gunnars_ground" -> gunnarsGround()
        "recital" -> recital()
        "poem" -> poem()
        "tell_dororan", "write_poem", "more_poem", "one_more_poem", "poem_done" -> {
            npc<Talk>("If there's anything you can do to make papa see sense, please do it.")
        }
        "tell_gudrun" -> whatHeSay()
        "meet_chieftain" -> {
            npc<Talk>("If there's anything you can do to make papa see sense, please do it.")
            meetChieftain()
        }
        "show_gudrun" -> showGudrun()
        else -> unstarted()
    }
}

suspend fun SuspendableContext<Player>.recital() {
    npc<Quiz>("Are you ready for the recital?")
    choice {
        option<Neutral>("Yes.") {
            cutscene()
        }
        option<Neutral>("Not right now.") {
        }
    }
}

val npcs: NPCs by inject()
val region = Region(12341)

suspend fun SuspendableContext<Player>.cutscene() {
    player.open("fade_out")
    val instance = startCutscene(region)
    val offset = instance.offset(region)
    setCutsceneEnd(instance)
    delay(4)
    player.tele(Tile(3078, 3435).add(offset), clearInterfaces = false)
    val dororan = npcs.add("dororan_cutscene", Tile(3079, 3435).add(offset), Direction.SOUTH) ?: return
    dororan.anim("dororan_lean_on_door")
    player.anim("player_lean_on_door")
    dororan.face(Direction.NORTH)
    player.face(Direction.NORTH)
    player.moveCamera(Tile(3079, 3430).add(offset), 280)
    player.turnCamera(Tile(3079, 3436).add(offset), 230)
    delay(2)
    player.open("fade_in")
    npc<Talk>("dororan_cutscene", "How long have they been in there?")
    player.anim("player_calm_doroan")
    choice {
        option<Neutral>("They're just starting.") {
            cutsceneMenu(instance)
        }
        option<Neutral>("You're late.") {
            cutsceneMenu(instance)
        }
    }
}

suspend fun SuspendableContext<Player>.cutsceneMenu(instance: Region) {
    npc<Sad>("dororan_cutscene", "This isn't going to work.")
    choice {
        option<Neutral>("Why's that?") {
            player.anim("player_calm_doroan")
            cutsceneMenu2(instance)
        }
        option<Neutral>("You're so pessimistic.") {
            player.anim("player_calm_doroan")
            cutsceneMenu2(instance)
        }
    }
}

suspend fun SuspendableContext<Player>.cutsceneMenu2(instance: Region) {
    npc<Cry>("dororan_cutscene", "What was I thinking? You should go in there and stop them before Gudrun makes a fool of herself.")
    choice {
        option<Neutral>("Okay, I will.") {
            player.anim("player_calm_doroan")
            npc<Sad>("dororan_cutscene", "No! Wait, stay here, it's too late now. We'll just have to see how it turns out.")
            cutsceneMenu3(instance)
        }
        option<Neutral>("Don't be silly.") {
            player.anim("player_calm_doroan")
            npc<Sad>("dororan_cutscene", "You're right, it's too late now. We'll just have to see how it turns out.")
            cutsceneMenu3(instance)
        }
    }
}

suspend fun SuspendableContext<Player>.cutsceneMenu3(instance: Region) {
    npc<Sad>("dororan_cutscene", "I can't hear what's happening. Can you hear what's happening?")
    player.anim("player_calm_doroan")
    player<Talk>("Gunthor is laughing at something.")
    npc<Upset>("dororan_cutscene", "He's probably considering the various tortures he has planned for me.")
    player.anim("player_calm_doroan")
    choice {
        option<Neutral>("Why would he do that?") {
            cutsceneMenu4(instance)
        }
        option<Neutral>("Now you're just being ridiculous.") {
            cutsceneMenu4(instance)
        }
    }
}

suspend fun SuspendableContext<Player>.cutsceneMenu4(instance: Region) {
    npc<Talk>("dororan_cutscene", "The poem says you can honour your ancestors by settling peacefully on the land they conquered.")
    npc<Sad>("dororan_cutscene", "He'll probably just find it insulting.")
    player.anim("player_calm_doroan")
    choice {
        option<Neutral>("Now's your chance to find out.") {
            cutscenePart2(instance)
        }
        option<Neutral>("You're doomed.") {
            cutscenePart2(instance)
        }
    }
}

suspend fun SuspendableContext<Player>.cutscenePart2(instance: Region) {
    player.open("fade_out")
    delay(3)
    npcs.clear(instance.toLevel(0))
    player.clearAnim()
    delay(1)
    val offset = instance.offset(region)
    player.tele(Tile(3083, 3426).add(offset), clearInterfaces = false)
    player.face(Direction.WEST)
    val dororan = npcs.add("dororan_cutscene", Tile(3082, 3428).add(offset), Direction.SOUTH) ?: return
    val gudrun = npcs.add("gudrun_cutscene", Tile(3080, 3426).add(offset), Direction.SOUTH) ?: return
    val kjell = npcs.add("kjell_cutscene", Tile(3077, 3426).add(offset), Direction.SOUTH) ?: return
    val gunthor = npcs.add("chieftain_gunthor_cutscene", Tile(3079, 3425).add(offset), Direction.SOUTH) ?: return
    val haakon = npcs.add("haakon_the_champion_cutscene", Tile(3078, 3425).add(offset), Direction.SOUTH) ?: return
    dororan.face(gudrun)
    player.moveCamera(Tile(3079, 3419).add(offset), 400)
    player.turnCamera(Tile(3079, 3426).add(offset), 150)
    delay(2)
    player.open("fade_in")
    npc<Upset>("dororan_cutscene", "I hope they at least give me a decent burial.")
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
    player.open("fade_out")
    delay(4)
    val npc = listOf(kjell, gunthor, haakon, gudrun, dororan)
    for (remove in npc) {
        npcs.remove(remove)
        npcs.removeIndex(remove)
    }
    player.moveCamera(Tile(3084, 3421).add(offset), 350)
    player.turnCamera(Tile(3082, 3426).add(offset), 250)
	val gudrunHugging = objects.add("gudrun_and_dororan", Tile(3082,3426).add(offset), shape = ObjectShape.CENTRE_PIECE_STRAIGHT, rotation = 1)
    player.open("fade_in")
    npc<Happy>("gudrun_cutscene", "That was brilliant! I must know who wrote that poem.")
    npc<Sad>("dororan_cutscene", "Um, that would be me. Hello")
    npc<Pleased>("gudrun_cutscene", "That line about beauty was for me, wasn't it?")
    npc<Upset>("dororan_cutscene", "Uh, Yes.")
    npc<Happy>("gudrun_cutscene", "You're the mystery poet who sent me the gold ring!")
    npc<Cry>("dororan_cutscene", "Sorry.")
    npc<Happy>("gudrun_cutscene", "I had no idea dwarves could be so romantic! Come here! ")
    delay(2)
    gudrunHugging.animate("gudrun_hugging")
    delay(4)
    player.queue.clear("gunnars_ground_cutscene_end")
    endCutscene(instance)
    player["gunnars_ground"] = "gunnars_ground"
    player["kjell"] = "guitar"
    player["dororan"] = "hidden"
    player["dororan_after_cutscene"] = "shown"
    player["gudrun"] = "hidden"
    player["gudrun_after_cutscene"] = "shown"
    val gudrunAfter = npcs[Tile(3082, 3417)].firstOrNull { it.id == "gudrun_after_cutscene" }
    if (gudrunAfter != null) {
        player.mode = Interact(player, gudrunAfter, NPCOption(player, gudrunAfter, gudrunAfter.def, "Talk-to"))
    } else {
        gunnarsGround()
    }
}

suspend fun SuspendableContext<Player>.gunnarsGround() {
    npc<Happy>("Papa was so impressed by Dororan's poem, he's made him the village poet!")
    npc<Happy>("dororan_after_cutscene2", "I'm more then a little surprised! He even gave me a house to live in!")
    npc<Pleased>("Our people's tradition is that the tribe provides lodging for the poet.")
    npc<Chuckle>("dororan_after_cutscene2", "It's huge!")
    npc<Happy>("It's not in the village. It's east of here: across the river and north of the road on the way to Varrock. It's a big house with roses outside.")
    npc<Pleased>("dororan_after_cutscene2", "I think Gunthor wants to keep me close, but not too close. Oh, I found something there for you!")
    npc<Happy>("dororan_after_cutscene2", "Whoever lived there before left a dozen pairs of boots in the attic.")
    npc<Talk>("dororan_after_cutscene2", "I picked out a pair for you to thank you for all your help.")
    npc<Happy>("dororan_after_cutscene2", "Underneath them all was this magic lamb. You should have it as well!")
    npc<Happy>("We're going to the new house. You should come and visit!")
    npc<Happy>("dororan_after_cutscene2", "Yes, we'll see you there!")
    choice {
        option<Neutral>("I'll see you soon.") {
            finishQuest()
        }
        option<Neutral>("I'll consider dropping in.") {
            finishQuest()
        }
    }
}

fun Context<Player>.setCutsceneEnd(instance: Region) {
    player.queue("gunnars_ground_cutscene_end", 1, LogoutBehaviour.Accelerate) {
        endCutscene(instance)
    }
}

suspend fun SuspendableContext<Player>.endCutscene(instance: Region) {
    player.open("fade_out")
    delay(3)
    player.tele(3081, 3416)
    stopCutscene(instance)
    player.clearCamera()
    player.clearAnim()
}

suspend fun SuspendableContext<Player>.poem() {
    if (player.holdsItem("gunnars_ground")) {
        npc<Quiz>("What have you got there?")
        player<Pleased>("Another gift from your mysterious suitor.")
        npc<Quiz>("A scroll?")
        player<Talk>("It's a poem; a story to convince your father to settle down. You could recite it to him.")
        npc<Amazed>("Let me see that.")
        player.anim("hand_over_item")
        item("gunnars_ground", 400, "You show Gudrun the poem")
        npc<Talk>("'Gunnar's Ground'")
        npc<Pleased>("Yes! I think this could work. I'll go to the longhouse right away!")
        player.inventory.remove("gunnars_ground")
        player["gunnars_ground"] = "recital"
        cutscene()
    } else {
        npc<Quiz>("What is it?")
        player<Upset>("I was meant to bring you a poem, but I seem to have mislaid it.")
    }
}

suspend fun SuspendableContext<Player>.whatHeSay() {
    npc<Quiz>("What did he say?")
    player<Talk>("He mentioned someone called Gunnar, and that you should think about his feelings.")
    npc<Angry>("By the eyeballs of Guthix! Always Gunnar!")
    choice {
        option<Neutral>("Who is Gunnar?") {
            npc<Frustrated>("He was my great-grandpapa! He founded this village a hundred years ago.")
            fathersAttitude()
        }
        option<Neutral>("What should we do now?") {
            npc<Frustrated>("I don't know. Maybe your mystery man has some ideas.")
            player["gunnars_ground"] = "tell_dororan"
            player<Pleased>("I'll ask him.")
        }
    }
}

suspend fun SuspendableContext<Player>.fathersAttitude() {
    choice {
        option<Neutral>("You don't seem to share your father's attitude towards him.") {
            npc<Frustrated>("I think there's a difference between respecting my ancestors and obsessing over them. Papa thinks whatever stupid war Gunnar fought is still going on.")
            npc<Frustrated>("I don't know. Maybe your mystery man has some ideas.")
            player["gunnars_ground"] = "tell_dororan"
            player<Pleased>("I'll ask him.")
        }
        option<Neutral>("What should we do now?") {
            npc<Frustrated>("I don't know. Maybe your mystery man has some ideas.")
            player["gunnars_ground"] = "tell_dororan"
            player<Pleased>("I'll ask him.")
        }
    }
}

suspend fun SuspendableContext<Player>.meetChieftain() {
    choice {
        option<Neutral>("Where is he?") {
            npc<Talk>("In the longhouse at the north end of the village, drinking and shouting.")
        }
        option<Neutral>("I'll see what I can do.") {
        }
    }
}

suspend fun SuspendableContext<Player>.showGudrun() {
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
    npc<Talk>("Yes.")
    if (player.holdsItem("dororans_engraved_ring")) {
        player<Pleased>("This is for you.")
        player.anim("hand_over_item")
        item("dororans_engraved_ring", 400, "You show Gudrun the ring.")
        npc<Happy>("It's lovely! There's something written on it:")
        npc<Pleased>("'Gudrun the Fair, Gudrun the Fiery.' Is it about me?")
        choice {
            option<Neutral>("Yes.") {
                aboutRing()
            }
            option<Neutral>("Presumable.") {
                aboutRing()
            }
        }
    } else {
        player<Sad>("I was meant to bring you a ring but I seem to have mislaid it.")
    }
}

suspend fun SuspendableContext<Player>.aboutRing() {
    npc<Pleased>("This is beautiful gift, stranger. Thank you.")
    choice {
        option<Neutral>("The ring isn't from me!") {
            whoFrom()
        }
        option<Neutral>("It should belong to someone just as beautiful.") {
            npc<Pleased>("That's very flattering! You look like an adventurer, though?")
            thatsRight()
        }
    }
}

suspend fun SuspendableContext<Player>.thatsRight() {
    choice {
        option<Pleased>("That's right.") {
            npc<Sad>("I'm sorry, I could never get involved with an adventurer.")
            whoFrom()
        }
        option<Talk>("Some call me that.") {
            npc<Sad>("I'm sorry, I could never get involved with an adventurer.")
            whoFrom()
        }
    }
}

suspend fun SuspendableContext<Player>.whoFrom() {
    npc<Surprised>("Oh! Who is it from?")
    choice {
        option<Neutral>("A great poet.") {
            npc<Pleased>("A tale-teller? A bard? My people have great respect from poets.")
            outsideVillage()
        }
        option<Neutral>("A secret admirer.") {
            npc<Pleased>("Does that really happen? How exciting!")
            outsideVillage()
        }
        option<Neutral>("A short suitor.") {
            npc<Quiz>("What?")
            player<Talk>("A petite paramour.")
            npc<Amazed>("What?")
            player<Talk>("A concise courter!")
            outsideVillage()
        }
    }
}

suspend fun SuspendableContext<Player>.outsideVillage() {
    npc<Quiz>("This man, he is from outside the village?")
    player<Talk>("Yes.")
    npc<Pleased>("I would love to leave the village and be romanced by exotic, handsome, outerlander men. There's a problem, though.")
    player<Quiz>("What's that?")
    npc<Sad>("My papa, the chieftain. He would never let an outerlander pursue me.")
    player<Quiz>("Why not?")
    npc<Talk>("He thinks all your people are our enemies.")
    choice {
        option<Neutral>("So, you want me to talk to your father?") {
            npc<Quiz>("I suppose that might work.")
            reasonWithHim()
        }
        option<Neutral>("So, you want me to kill your father?") {
            npc<Surprised>("What? no! Maybe...you could just try talking to him.")
            reasonWithHim()
        }
    }
}

suspend fun SuspendableContext<Player>.reasonWithHim() {
    npc<Sad>("I've tried to reason with him, but he's impossible! Maybe he'll listen to you. I know some of the others feel the same, but they're loyal to papa.")
    player["gunnars_ground"] = "meet_chieftain"
    player.inventory.remove("dororans_engraved_ring")
    meetChieftain()
}

suspend fun SuspendableContext<Player>.unstarted() {
    npc<Pleased>("Can I help you, stranger?")
    npc<Frustrated>("kjell_sword", "Why are you talking to that outerlander?")
    npc<Frustrated>("It's none of your business, Kjell! Just guard the hut!")
    npc<Amazed>("Sorry about that. Did you want something?")
    choice {
        option<Neutral>("What is this place?") {
            whatIsThisPlace()
        }
        option<Neutral>("Who are you?") {
            whoAreYou()
        }
        option<Neutral>("Actually, no. Goodbye") {
        }
    }
}

suspend fun SuspendableContext<Player>.whoAreYou() {
    npc<Pleased>("My name is Gudrun. My father, Gunthor, is chieftain of the village.")
    choice {
        option<Happy>("What is this place?") {
            whatIsThisPlace()
        }
        option<Pleased>("Goodbye.") {
        }
    }
}

suspend fun SuspendableContext<Player>.whatIsThisPlace() {
    npc<Pleased>("Outerlanders call this the barbarian village. It doesn't have a name because...it's complicated.")
    npc<Talk>("if you want to know more, you should talk to Hunding. He's up in the tower at the east entrance.")
    choice {
        option<Neutral>("Who are you?") {
            whoAreYou()
        }
        option<Pleased>("Goodbye.") {
        }
    }
}

suspend fun SuspendableContext<Player>.finishQuest() {
    npc<Happy>("dororan_after_cutscene2", "Goodbye!")
    npc<Happy>("Goodbye!")
    if (player.inventory.spaces < 2) {
        statement("You don't have room for the two reward items. Speak to Gudrun or Dororan again when you have room.")
        return
    }
    player.open("fade_out")
    delay(4)
    player["dororan_after_cutscene"] = "hidden"
    player["gudrun_after_cutscene"] = "hidden"
    player["dororan_after_quest"] = "shown"
    player["gudrun_after_quest"] = "shown"
    player.open("fade_in")
    questComplete()
}

fun Context<Player>.questComplete() {
    player.playJingle("quest_complete_3")
    player["gunnars_ground"] = "completed"
    player.inc("quest_points", 5)
    player.experience.add(Skill.Crafting, 300.0)
    player.softQueue("quest_complete", 1) {
        player.sendQuestComplete("Gunnar's Ground", listOf(
            "5 Quest Points",
            "300 Crafting XP.",
            "Antique lamp.",
            "Swanky boots."
        ), Item("gunnars_ground"))
    }
    player.inventory.add("antique_lamp_gunnars_ground")
    player.inventory.add("swanky_boots")
}

npcOperate("Talk-to", "gudrun_after_quest") {
    when (player.quest("gunnars_ground")) {
        "completed" -> {
            npc<Happy>("Hello!")
            choice {
                option<Neutral>("I want to ask you something.") {
                    npc<Quiz>("Of course, what is it?")
                    menu()
                }
                option<Neutral>("Just passing through.") {
                    npc<Happy>("Goodbye!")
                }
            }
        }
        else -> player.message("error")
    }
}

suspend fun SuspendableContext<Player>.menu() {
    choice {
        option<Neutral>("How are things with Dororan?") {
            npc<Pleased>("I really like him. he's funny, vulnerable and nothing like my people.")
            choice {
                option<Neutral>("You're going to stay together then?") {
                    npc<Happy>("Of course!")
                    elseGoodbye()
                }
                option<Neutral>("I want to ask about something else.") {
                    npc<Quiz>("Of course, what is it?")
                    menu()
                }
                option<Neutral>("Goodbye.") {
                    npc<Happy>("Oh, Goodbye!")
                }
            }
        }
        option<Neutral>("Where did this house come from?") {
            npc<Neutral>("I don't know. Papa said the previous owners left it to him. I don't know why they would do that.")
            theory()
        }
        option<Neutral>("Goodbye.") {
            npc<Happy>("Oh, Goodbye!")
        }
    }
}

suspend fun SuspendableContext<Player>.theory() {
    choice {
        option<Neutral>("Do you have a theory?") {
            npc<Amazed>("Gunnar always said 'A warrior does not barter; he simply takes!'. I think papa bought the house, but doesn't want anyone to know.")
            elseGoodbye()
        }
        option<Neutral>("I want to ask about something else.") {
            npc<Quiz>("Of course, what is it?")
            menu()
        }
        option<Neutral>("Goodbye.") {
            npc<Happy>("Oh, Goodbye!")
        }
    }
}

suspend fun SuspendableContext<Player>.elseGoodbye() {
    choice {
        option<Neutral>("I want to ask about something else.") {
            npc<Quiz>("Of course, what is it?")
            menu()
        }
        option<Neutral>("Goodbye.") {
            npc<Happy>("Oh, Goodbye!")
        }
    }
}
