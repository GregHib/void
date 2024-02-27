package world.gregs.voidps.world.map.barbarian_village

import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.CharacterContext
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.LogoutBehaviour
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.delay
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.activity.quest.sendQuestComplete
import world.gregs.voidps.world.activity.quest.startCutscene
import world.gregs.voidps.world.activity.quest.stopCutscene
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.item
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.sound.playJingle

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

suspend fun CharacterContext.recital() {
    npc<Unsure>("Are you ready for the recital?")
    choice {
        option<Talking>("Yes.") {
            cutscene()
        }
        option<Talking>("Not right now.") {
        }
    }
}

val npcs: NPCs by inject()
val region = Region(12341)

suspend fun CharacterContext.cutscene() {
    player.open("fade_out")
    val instance = startCutscene(region)
    val offset = instance.offset(region)
    setCutsceneEnd(instance)
    delay(4)
    player.tele(Tile(3078, 3435).add(offset), clearInterfaces = false)
    val dororan = npcs.add("dororan_cutscene", Tile(3079, 3435).add(offset), Direction.SOUTH) ?: return
    dororan.setAnimation("14725")
    player.setAnimation("14724")
    dororan.face(Direction.NORTH)
    player.face(Direction.NORTH)
    player.moveCamera(Tile(3079, 3430).add(offset), 280)
    player.turnCamera(Tile(3079, 3436).add(offset), 230)
    delay(2)
    player.open("fade_in")
    npc<Talk>("dororan_cutscene", "How long have they been in there?")
    player.setAnimation("6709")
    choice {
        option<Talking>("They're just starting.") {
            cutsceneMenu()
        }
        option<Talking>("You're late.") {
            cutsceneMenu()
        }
    }
}

suspend fun CharacterContext.cutsceneMenu() {
    npc<Sad>("dororan_cutscene", "This isn't going to work.")
    choice {
        option<Talking>("Why's that?") {
            player.setAnimation("6709")
            cutsceneMenu2()
        }
        option<Talking>("You're so pessimistic.") {
            player.setAnimation("6709")
            cutsceneMenu2()
        }
    }
}

suspend fun CharacterContext.cutsceneMenu2() {
    npc<Unknown_expression>("dororan_cutscene", "What was I thinking? You should go in there and stop them before Gudrun makes a fool of herself.")
    choice {
        option<Talking>("Okay, I will.") {
            player.setAnimation("6709")
            npc<Sad>("dororan_cutscene", "No! Wait, stay here, it's too late now. We'll just have to see how it turns out.")
            cutsceneMenu3()
        }
        option<Talking>("Don't be silly.") {
            player.setAnimation("6709")
            npc<Sad>("dororan_cutscene", "You're right, it's too late now. We'll just have to see how it turns out.")
            cutsceneMenu3()
        }
    }
}

suspend fun CharacterContext.cutsceneMenu3() {
    npc<Sad>("dororan_cutscene", "I can't hear what's happening. Can you hear what's happening?")
    player.setAnimation("6709")
    player<Talk>("Gunthor is laughing at something.")
    npc<Upset>("dororan_cutscene", "He's probably considering the various tortures he has planned for me.")
    player.setAnimation("6709")
    choice {
        option<Talking>("Why would he do that?") {
            cutsceneMenu4()
        }
        option<Talking>("Now you're just being ridiculous.") {
            cutsceneMenu4()
        }
    }
}

suspend fun CharacterContext.cutsceneMenu4() {
    npc<Talk>("dororan_cutscene", "The poem says you can honour your ancestors by settling peacefully on the land they conquered.")
    npc<Sad>("dororan_cutscene", "He'll probably just find it insulting.")
    player.setAnimation("6709")
    choice {
        option<Talking>("Now's your chance to find out.") {
            cutscenePart2()
        }
        option<Talking>("You're doomed.") {
            cutscenePart2()
        }
    }
}

suspend fun CharacterContext.cutscenePart2() {
    player.queue.clear("gunnars_ground_cutscene_end")
    player.open("fade_out")
    val instance = startCutscene(region)
    val offset = instance.offset(region)
    setCutsceneEnd(instance)
    delay(4)
    player.tele(Tile(3083, 3426).add(offset), clearInterfaces = false)
    player.face(Direction.WEST)
    val dororan = npcs.add("dororan_cutscene", Tile(3082, 3428).add(offset), Direction.SOUTH) ?: return
    val gudrun = npcs.add("gudrun_cutscene", Tile(3080, 3426).add(offset), Direction.SOUTH) ?: return
    val kjell = npcs.add("kjell_cutscene", Tile(3077, 3426).add(offset), Direction.SOUTH) ?: return
    val gunthor = npcs.add("chieftain_gunthor_cutscene", Tile(3079, 3425).add(offset), Direction.SOUTH) ?: return
    val haakon = npcs.add("haakon_the_champion_cutscene", Tile(3078, 3425).add(offset), Direction.SOUTH) ?: return
    player.moveCamera(Tile(3079, 3419).add(offset), 400)
    player.turnCamera(Tile(3079, 3426).add(offset), 150)
    delay(2)
    player.open("fade_in")
    npc<Upset>("dororan_cutscene", "I hope they at least give me a decent burial.")
    gunthor.setAnimation("14734")
    npc<Angry>("chieftain_gunthor_cutscene", "Freemen! Freemen! I have an announcement!")
    npc<Angry>("kjell_cutscene", "Hear the chieftain speak! Hear him!")
    npc<Angry>("chieftain_gunthor_cutscene", "We have always borne the legacy of our ancestors, and we have borne it with honour!")
    npc<Unknown_expression>("kjell_cutscene", "FOR GUNNAR!")
    npc<Angry>("chieftain_gunthor_cutscene", "And though we honour them still, the time of our ancestors is past. this is the time of Gunthor!")
    npc<Unknown_expression>("haakon_the_champion_cutscene", "FOR GUNNAR!")
    npc<Angry>("chieftain_gunthor_cutscene", "Gunthor says: This is Gunnar's ground, bought with blood! Let it remain Gunnar's ground forever! Here we settle!")
    npc<Angry>("chieftain_gunthor_cutscene", "GUNNAR'S GROUND!")
    kjell.setAnimation("1531")
    haakon.setAnimation("14739")
    npc<Unknown_expression>("haakon_the_champion_cutscene", "GUNNAR'S GROUND!")
    player.open("fade_out")
    delay(4)
    dororan.tele(Tile(3082, 3426).add(offset))
    dororan.face(Direction.WEST)
    gudrun.tele(Tile(3081, 3426).add(offset))
    gudrun.face(Direction.EAST)
    val npc = listOf(kjell, gunthor, haakon)
    for (remove in npc) {
        npcs.remove(remove)
        npcs.removeIndex(remove)
        npcs.releaseIndex(remove)
    }
    player.moveCamera(Tile(3084, 3421).add(offset), 350)
    player.turnCamera(Tile(3082, 3426).add(offset), 250)
    player.open("fade_in")
    npc<Cheerful>("gudrun_cutscene", "That was brilliant! I must know who wrote that poem.")
    npc<Sad>("dororan_cutscene", "Um, that would be me. Hello")
    npc<Happy>("gudrun_cutscene", "That line about beauty was for me, wasn't it?")
    npc<Upset>("dororan_cutscene", "Uh, Yes.")
    npc<Cheerful>("gudrun_cutscene", "You're the mystery poet who sent me the gold ring!")
    npc<Unknown_expression>("dororan_cutscene", "Sorry.")
    npc<Cheerful>("gudrun_cutscene", "I had no idea dwarves could be so romantic! Come here! ")
    delay(2)
    //anim 17513 - Gudrun Hugging Dororan (Gunnar’s Ground) ?
    gudrun.setAnimation("17513")
    player.queue.clear("gunnars_ground_cutscene_end")
    endCutscene(instance)
    player["gunnars_ground"] = "gunnars_ground"
    player["kjell"] = "guitar"
    player["dororan"] = "hidden"
    player["dororan_after_cutscene"] = "shown"
    player["gudrun"] = "hidden"
    player["gudrun_after_cutscene"] = "shown"
    gunnarsGround()
}

suspend fun CharacterContext.gunnarsGround() {
    npc<Cheerful>("Papa was so impressed by Dororan's poem, he's made him the village poet!")
    npc<Cheerful>("dororan_after_cutscene2", "I'm more then a little surprised! He even gave me a house to live in!")
    npc<Happy>("Our people's tradition is that the tribe provides lodging for the poet.")
    npc<Unknown_expression>("dororan_after_cutscene2", "It's huge!")
    npc<Cheerful>("It's not in the village. It's east of here: across the river and north of the road on the way to Varrock. It's a big house with roses outside.")
    npc<Happy>("dororan_after_cutscene2", "I think Gunthor wants to keep me close, but not too close. Oh, I found something there for you!")
    npc<Cheerful>("dororan_after_cutscene2", "Whoever lived there before left a dozen pairs of boots in the attic.")
    npc<Talk>("dororan_after_cutscene2", "I picked out a pair for you to thank you for all your help.")
    npc<Cheerful>("dororan_after_cutscene2", "Underneath them all was this magic lamb. You should have it as well!")
    npc<Cheerful>("We're going to the new house. You should come and visit!")
    npc<Cheerful>("dororan_after_cutscene2", "Yes, we'll see you there!")
    choice {
        option<Talking>("I'll see you soon.") {
            finishQuest()
        }
        option<Talking>("I'll consider dropping in.") {
            finishQuest()
        }
    }
}

suspend fun CharacterContext.finishQuest() {
    npc<Cheerful>("dororan_after_cutscene2", "Goodbye!")
    npc<Cheerful>("Goodbye!")
    player.open("fade_out")
    delay(4)
    player["dororan_after_cutscene"] = "hidden"
    player["gudrun_after_cutscene"] = "hidden"
    player["dororan_after_quest"] = "shown"
    player["gudrun_after_quest"] = "shown"
    player.open("fade_in")
    questComplete()
}


fun CharacterContext.setCutsceneEnd(instance: Region) {
    player.queue("gunnars_ground_cutscene_end", 1, LogoutBehaviour.Accelerate) {
        endCutscene(instance)
    }
}

suspend fun CharacterContext.endCutscene(instance: Region) {
    player.open("fade_out")
    delay(3)
    player.tele(3081, 3416)
    stopCutscene(instance)
    player.clearCamera()
}

suspend fun CharacterContext.poem() {
    if (player.holdsItem("gunnars_ground")) {
        npc<Unsure>("What have you got there?")
        player<Happy>("Another gift from your mysterious suitor.")
        npc<Unsure>("A scroll?")
        player<Talk>("It's a poem; a story to convince your father to settle down. You could recite it to him.")
        npc<Amazed>("Let me see that.")
        player.setAnimation("14737")
        item("gunnars_ground", 400, "You show Gudrun the poem")
        npc<Talk>("'Gunnar's Ground'")
        npc<Happy>("Yes! I think this could work. I'll go to the longhouse right away!")
        player.inventory.remove("gunnars_ground")
        player["gunnars_ground"] = "recital"
        cutscene()
    } else {
        npc<Unsure>("What is it?")
        player<Upset>("I was meant to bring you a poem, but I seem to have mislaid it.")
    }
}

suspend fun CharacterContext.whatHeSay() {
    npc<Unsure>("What did he say?")
    player<Talk>("He mentioned someone called Gunnar, and that you should think about his feelings.")
    npc<Furious>("By the eyeballs of Guthix! Always Gunnar!")
    choice {
        option<Talking>("Who is Gunnar?") {
            npc<Angry>("He was my great-grandpapa! He founded this village a hundred years ago.")
            fathersAttitude()
        }
        option<Talking>("What should we do now?") {
            npc<Angry>("I don't know. Maybe your mystery man has some ideas.")
            player["gunnars_ground"] = "tell_dororan"
            player<Happy>("I'll ask him.")
        }
    }
}

suspend fun CharacterContext.fathersAttitude() {
    choice {
        option<Talking>("You don't seem to share your father's attitude towards him.") {
            npc<Angry>("I think there's a difference between respecting my ancestors and obsessing over them. Papa thinks whatever stupid war Gunnar fought is still going on.")
            npc<Angry>("I don't know. Maybe your mystery man has some ideas.")
            player["gunnars_ground"] = "tell_dororan"
            player<Happy>("I'll ask him.")
        }
        option<Talking>("What should we do now?") {
            npc<Angry>("I don't know. Maybe your mystery man has some ideas.")
            player["gunnars_ground"] = "tell_dororan"
            player<Happy>("I'll ask him.")
        }
    }
}

suspend fun CharacterContext.meetChieftain() {
    choice {
        option<Talking>("Where is he?") {
            npc<Talk>("In the longhouse at the north end of the village, drinking and shouting.")
        }
        option<Talking>("I'll see what I can do.") {
        }
    }
}

suspend fun CharacterContext.showGudrun() {
    npc<Angry>("kjell_sword", "Gudrun! You caught enough fish?")
    npc<Angry>("Yes! I have plenty of fish!")
    npc<Angry>("kjell_sword", "Your father needs many fish to feed the freemen!")
    npc<Angry>("I know!")
    npc<Angry>("kjell_sword", "Maybe you sneak off to the outerlander city again? Buy fish in market, instead of catching them?")
    npc<Angry>("Shut up! I'm much better at fishing than you.")
    npc<Angry>("kjell_sword", "You are not!")
    npc<Angry>("Just guard the hut like chieftain told you to!")
    npc<Angry>("kjell_sword", "Fine!")
    npc<Angry>("Stupid barbarian.")
    npc<Unsure>("Sorry about that, stranger. Did you want something?.")
    player<Unsure>("Are you Gudrun?")
    npc<Talk>("Yes.")
    if (player.holdsItem("dororans_engraved_ring")) {
        player<Happy>("This is for you.")
        player.setAnimation("14737")
        item("dororans_engraved_ring", 400, "You show Gudrun the ring.")
        npc<Cheerful>("It's lovely! There's something written on it:")
        npc<Happy>("'Gudrun the Fair, Gudrun the Fiery.' Is it about me?")
        choice {
            option<Talking>("Yes.") {
                aboutRing()
            }
            option<Talking>("Presumable.") {
                aboutRing()
            }
        }
    } else {
        player<Sad>("I was meant to bring you a ring but I seem to have mislaid it.")
    }
}

suspend fun CharacterContext.aboutRing() {
    npc<Happy>("This is beautiful gift, stranger. Thank you.")
    choice {
        option<Talking>("The ring isn't from me!") {
            whoFrom()
        }
        option<Talking>("It should belong to someone just as beautiful.") {
            npc<Happy>("That's very flattering! You look like an adventurer, though?")
            thatsRight()
        }
    }
}

suspend fun CharacterContext.thatsRight() {
    choice {
        option<Happy>("That's right.") {
            npc<Sad>("I'm sorry, I could never get involved with an adventurer.")
            whoFrom()
        }
        option<Talk>("Some call me that.") {
            npc<Sad>("I'm sorry, I could never get involved with an adventurer.")
            whoFrom()
        }
    }
}

suspend fun CharacterContext.whoFrom() {
    npc<Surprised>("Oh! Who is it from?")
    choice {
        option<Talking>("A great poet.") {
            npc<Happy>("A tale-teller? A bard? My people have great respect from poets.")
            outsideVillage()
        }
        option<Talking>("A secret admirer.") {
            npc<Happy>("Does that really happen? How exciting!")
            outsideVillage()
        }
        option<Talking>("A short suitor.") {
            npc<Unsure>("What?")
            player<Talk>("A petite paramour.")
            npc<Amazed>("What?")
            player<Talk>("A concise courter!")
            outsideVillage()
        }
    }
}

suspend fun CharacterContext.outsideVillage() {
    npc<Unsure>("This man, he is from outside the village?")
    player<Talk>("Yes.")
    npc<Happy>("I would love to leave the village and be romanced by exotic, handsome, outerlander men. There's a problem, though.")
    player<Unsure>("What's that?")
    npc<Sad>("My papa, the chieftain. He would never let an outerlander pursue me.")
    player<Unsure>("Why not?")
    npc<Talk>("He thinks all your people are our enemies.")
    choice {
        option<Talking>("So, you want me to talk to your father?") {
            npc<Unsure>("I suppose that might work.")
            reasonWithHim()
        }
        option<Talking>("So, you want me to kill your father?") {
            npc<Surprised>("What? no! Maybe...you could just try talking to him.")
            reasonWithHim()
        }
    }
}

suspend fun CharacterContext.reasonWithHim() {
    npc<Sad>("I've tried to reason with him, but he's impossible! Maybe he'll listen to you. I know some of the others feel the same, but they're loyal to papa.")
    player["gunnars_ground"] = "meet_chieftain"
    player.inventory.remove("dororans_engraved_ring")
    meetChieftain()
}

suspend fun CharacterContext.unstarted() {
    npc<Happy>("Can I help you, stranger?")
    npc<Angry>("kjell_sword", "Why are you talking to that outerlander?")
    npc<Angry>("It's none of your business, Kjell! Just guard the hut!")
    npc<Amazed>("Sorry about that. Did you want something?")
    choice {
        option<Talking>("What is this place?") {
            whatIsThisPlace()
        }
        option<Talking>("Who are you?") {
            whoAreYou()
        }
        option<Talking>("Actually, no. Goodbye") {
        }
    }
}

suspend fun CharacterContext.whoAreYou() {
    npc<Happy>("My name is Gudrun. My father, Gunthor, is chieftain of the village.")
    choice {
        option<Cheerful>("What is this place?") {
            whatIsThisPlace()
        }
        option<Happy>("Goodbye.") {
        }
    }
}

suspend fun CharacterContext.whatIsThisPlace() {
    npc<Happy>("Outerlanders call this the barbarian village. It doesn't have a name because...it's complicated.")
    npc<Talk>("if you want to know more, you should talk to Hunding. He's up in the tower at the east entrance.")
    choice {
        option<Talking>("Who are you?") {
            whoAreYou()
        }
        option<Happy>("Goodbye.") {
        }
    }
}

fun CharacterContext.questComplete() {
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

on<NPCOption>({ operate && target.id == "gudrun_after_quest" && option == "Talk-to" }) { player: Player ->
    when (player.quest("gunnars_ground")) {
        "completed" -> {
            npc<Cheerful>("Hello!")
            choice {
                option<Talking>("I want to ask you something.") {
                    npc<Unsure>("Of course, what is it?")
                    menu()
                }
                option<Talking>("Just passing through.") {
                    npc<Cheerful>("Goodbye!")
                }
            }
        }
        else -> player.message("error")
    }
}

suspend fun CharacterContext.menu() {
    choice {
        option<Talking>("How are things with Dororan?") {
            npc<Happy>("I really like him. he's funny, vulnerable and nothing like my people.")
            choice {
                option<Talking>("You're going to stay together then?") {
                    npc<Cheerful>("Of course!")
                    elseGoodbye()
                }
                option<Talking>("I want to ask about something else.") {
                    npc<Unsure>("Of course, what is it?")
                    menu()
                }
                option<Talking>("Goodbye.") {
                    npc<Cheerful>("Oh, Goodbye!")
                }
            }
        }
        option<Talking>("Where did this house come from?") {
            npc<Talking>("I don't know. Papa said the previous owners left it to him. I don't know why they would do that.")
            theory()
        }
        option<Talking>("Goodbye.") {
            npc<Cheerful>("Oh, Goodbye!")
        }
    }
}

suspend fun CharacterContext.theory() {
    choice {
        option<Talking>("Do you have a theory?") {
            npc<Amazed>("Gunnar always said 'A warrior does not barter; he simply takes!'. I think papa bought the house, but doesn't want anyone to know.")
            elseGoodbye()
        }
        option<Talking>("I want to ask about something else.") {
            npc<Unsure>("Of course, what is it?")
            menu()
        }
        option<Talking>("Goodbye.") {
            npc<Cheerful>("Oh, Goodbye!")
        }
    }
}

suspend fun CharacterContext.elseGoodbye() {
    choice {
        option<Talking>("I want to ask about something else.") {
            npc<Unsure>("Of course, what is it?")
            menu()
        }
        option<Talking>("Goodbye.") {
            npc<Cheerful>("Oh, Goodbye!")
        }
    }
}