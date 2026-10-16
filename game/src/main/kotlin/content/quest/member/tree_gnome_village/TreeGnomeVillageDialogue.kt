package content.quest.member.tree_gnome_village

import content.entity.combat.Combat
import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import content.quest.questComplete
import content.quest.questStage
import content.quest.refreshQuestJournal
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.jingle
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.combatLevel
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

internal const val BOLREN = "king_bolren_tree_gnome_village"
internal const val MONTAI = "commander_montai_battlefield"
internal const val ELKOY = "elkoy_tree_gnome_village_4"

internal suspend fun Player.gnomeEscort(out: Boolean, text: String) {
    // Moving happens immediately after Continue on Bolren's assistant line.
    tele(if (out) Tile(2504, 3192) else Tile(2514, 3160))
    npc<Happy>(ELKOY, text)
}

class TreeGnomeVillageDialogue : Script {
    init {
        npcOperate("Talk-to", BOLREN) { bolren() }
        npcOperate("Talk-to", MONTAI) { montai() }
        npcOperate("Talk-to", "tracker_gnome_1_battlefield,tracker_gnome_3_battlefield") { (target) ->
            tracker(target.id.substringAfter("tracker_gnome_").substringBefore('_').toInt(), target.id)
        }
        npcApproach("Talk-to", "tracker_gnome_2_battlefield") { (target) ->
            approachRange(2, update = false)
            tracker(2, target.id)
        }
        npcOperate("Talk-to", "khazard_warlord_underground_pass") { (target) ->
            val stage = questStage("tree_gnome_village")
            when {
                stage == 5 -> {
                    player<Neutral>("Hello there.")
                    npc<Neutral>(target.id, "You think you're so clever. You know nothing!")
                    player<Neutral>("What?")
                    npc<Neutral>(target.id, "I'll crush you and those pesky little green men!")
                }
                stage < 6 -> {
                    player<Neutral>("Hello, how are you?")
                    npc<Neutral>(target.id, "Don't speak to me you insignificant wretch! Die in the name of Khazard!")
                }
                stage >= 7 -> {
                    player<Neutral>("I thought I killed you?")
                    npc<Neutral>(target.id, "Fool, warriors blessed by Khazard don't die. You can't kill that which is already dead. However I can kill you!")
                    if (stage == 7) TreeGnomeVillageProgress.recoverOrbs(this)
                }
                else -> {
                    player<Neutral>("You there, stop!")
                    npc<Neutral>(target.id, "Go back to your pesky little green friends.")
                    player<Neutral>("I've come for the orbs.")
                    npc<Neutral>(target.id, "You're out of your depth traveller. These orbs are part of a much larger picture.")
                    player<Neutral>("They're stolen goods, now give them here!")
                    npc<Neutral>(target.id, "Ha, you really think you stand a chance? I'll crush you.")
                }
            }
            if (stage >= 6) {
                target["warlord_challenger"] = index
                Combat.combat(target, this)
            }
        }
    }

    private suspend fun Player.bolren() {
        when (quest("tree_gnome_village")) {
            "unstarted" -> {
                player<Neutral>("Hello.")
                npc<Happy>(BOLREN, "Well hello stranger. My name's Bolren, I'm the king of the tree gnomes.")
                npc<Shock>(BOLREN, "I'm surprised you made it in, maybe I made the maze too easy.")
                player<Neutral>("Maybe.")
                npc<Sad>(BOLREN, "I'm afraid I have more serious concerns at the moment. Very serious.")
                choice {
                    option<Quiz>("Can I help at all?") {
                        npc<Happy>(BOLREN, "I'm glad you asked.")
                        npc<Sad>(BOLREN, "The truth is my people are in grave danger. We have always been protected by the Spirit Tree. No creature of dark can harm us while its three orbs are in place.")
                        npc<Sad>(BOLREN, "We are not a violent race, but we fight when we must. Many gnomes have fallen battling the dark forces of Khazard to the North.")
                        npc<Sad>(BOLREN, "We became desperate, so we took one orb of protection to the battlefield. It was a foolish move.")
                        npc<Sad>(BOLREN, "Khazard troops seized the orb. Now we are completely defenceless.")
                        player<Quiz>("How can I help?")
                        npc<Neutral>(BOLREN, "You would be a huge benefit on the battlefield. If you would go there and try to retrieve the orb, my people and I will be forever grateful.")
                        if (combatLevel < 45) {
                            statement("Before starting this quest, be aware that your combat level is lower than the recommended level of 45.")
                        }
                        choice {
                            option("I would be glad to help.") {
                                player<Neutral>("I would be glad to help.")
                                if (TreeGnomeVillageProgress.start(this@bolren)) {
                                    npc<Neutral>(BOLREN, "Thank you. The battlefield is to the north of the maze. Commander Montai will inform you of their current situation.")
                                    npc<Neutral>(BOLREN, "That is if he's still alive.")
                                    npc<Neutral>(BOLREN, "My assistant shall guide you out. Good luck friend, try your best to return the orb")
                                    gnomeEscort(true, "We're out of the maze now. Please hurry, we must have the orb if we are to survive.")
                                }
                            }
                            option("I'm sorry but I won't be involved.") {
                                player<Neutral>("I'm sorry but I won't be involved.")
                                npc<Neutral>(BOLREN, "Ok then, travel safe.")
                            }
                        }
                    }
                    option<Bored>("I'll leave you to it then.") { npc<Neutral>(BOLREN, "Ok, take care.") }
                }
            }
            "orb_recovered" -> {
                if (!inventory.contains("orb_of_protection")) {
                    player<Neutral>("Hello Bolren.")
                    npc<Quiz>(BOLREN, "Do you have the orb?")
                    player<Sad>("No, I'm afraid not.")
                    npc<Sad>(BOLREN, "Please, we must have the orb if we are to survive.")
                    return
                }
                player<Happy>("I have the orb.")
                npc<Sad>(BOLREN, "Oh my... The misery, the horror!")
                player<Quiz>("King Bolren, are you OK?")
                npc<Sad>(BOLREN, "Thank you traveller, but it's too late. We're all doomed.")
                player<Quiz>("What happened?")
                npc<Sad>(BOLREN, "They came in the night. I don't know how many, but enough.")
                player<Quiz>("Who?")
                npc<Sad>(BOLREN, "Khazard troops. They slaughtered anyone who got in their way. Women, children, my wife.")
                player<Sad>("I'm sorry.")
                npc<Sad>(BOLREN, "They took the other orbs, now we are defenceless.")
                player<Quiz>("Where did they take them?")
                npc<Sad>(BOLREN, "They headed north of the stronghold. A warlord carries the orbs.")
                choice {
                    option<Neutral>("I will find the warlord and bring back the orbs.") {
                        npc<Neutral>(BOLREN, "You are brave, but this task will be tough even for you. I wish you the best of luck. Once again you are our only hope.")
                        npc<Neutral>(BOLREN, "I will safeguard this orb and pray for your safe return. My assistant will guide you out.")
                        if (TreeGnomeVillageProgress.returnOrb(this)) {
                            gnomeEscort(true, "Good luck friend.")
                        }
                    }
                    option<Neutral>("I'm sorry but I can't help.") {
                        npc<Sad>(BOLREN, "I understand, this isn't your battle.")
                    }
                }
            }
            "hunt_warlord", "warlord_defeated" -> {
                player<Neutral>("Bolren, I have returned.")
                npc<Neutral>(BOLREN, "You made it back! Do you have the orbs?")
                player<Neutral>("No, I'm afraid not.")
                npc<Neutral>(BOLREN, "Please, we must have the orbs if we are to survive.")
            }
            "orbs_recovered" -> {
                if (get("tree_gnome_village_ceremony", 0) == 2) {
                    if (inventory.spaces == 0) {
                        statement("You need a free inventory space for King Bolren's reward.")
                        return
                    }
                    npc<Neutral>(BOLREN, "Now at last my people are safe once more. We can live in peace again.")
                    player<Neutral>("I'm pleased I could help.")
                    npc<Neutral>(BOLREN, "You are modest brave traveller.")
                    npc<Neutral>(BOLREN, "Please, for your efforts take this amulet. It's made from the same sacred stone as the orbs of protection. It will help keep you safe on your journeys.")
                    player<Neutral>("Thank you King Bolren.")
                    npc<Neutral>(BOLREN, "The tree has many other powers, some of which I cannot reveal. As a friend of the gnome people, I can now allow you to use the tree's magic to teleport to other trees grown from related seeds.")
                    if (TreeGnomeVillageProgress.complete(this)) {
                        jingle("quest_complete_1")
                        questComplete("Tree Gnome Village", "2 Quest Points", "11,450 Attack XP", "Gnome amulet", "Access to spirit tree teleportation", item = "gnome_amulet")
                    }
                    return
                }
                if (get("tree_gnome_village_ceremony", 0) == 0) {
                    player<Neutral>("Bolren, I have returned.")
                    npc<Neutral>(BOLREN, "You made it back! Do you have the orbs?")
                    if (!inventory.contains("orbs_of_protection")) {
                        player<Neutral>("No, I'm afraid not.")
                        npc<Neutral>(BOLREN, "Please, we must have the orbs if we are to survive.")
                        return
                    }
                    player<Neutral>("I have them here.")
                    npc<Neutral>(BOLREN, "Hooray, you're amazing. I didn't think it was possible but you've saved us.")
                    npc<Neutral>(BOLREN, "Once the orbs are replaced we will be safe once more. We must begin the ceremony immediately.")
                    player<Neutral>("What does the ceremony involve?")
                    npc<Neutral>(BOLREN, "The spirit tree has looked over us for centuries. Now we must pay our respects.")
                }
                if (TreeGnomeVillageProgress.beginCeremony(this)) {
                    statement("The gnomes begin to chant. Meanwhile, King Bolren holds the orbs of protection out in front of him.")
                    ceremony()
                    statement("The orbs of protection come to rest gently in the branches of the ancient spirit tree.")
                    TreeGnomeVillageProgress.finishCeremony(this)
                }
            }
            "completed" -> {
                if (!ownsItem("gnome_amulet")) {
                    if (inventory.spaces == 0) {
                        statement("You need a free inventory space for King Bolren's amulet.")
                    } else {
                        npc<Neutral>(BOLREN, "Here, I think this belongs to you.")
                        if (!ownsItem("gnome_amulet") && inventory.add("gnome_amulet")) {
                            item(item = "gnome_amulet", text = "King Bolren gives you an amulet.")
                        }
                    }
                }
                player<Neutral>("Hello again, Bolren.")
                npc<Neutral>(BOLREN, "Hello there. It's good to see you again.")
                player<Neutral>("How are things here?")
                npc<Neutral>(BOLREN, "Those Khazard troops continue to be a menace, but things are much better now. All thanks to you, of course.")
                statement("You do not meet all of the requirements to start The Path of Glouphrie quest.")
            }
            else -> {
                player<Neutral>("Hello Bolren.")
                if (get("tree_gnome_village_montai_met", false)) {
                    npc<Neutral>(BOLREN, "The orb is being held at the battlefield north of the maze.")
                } else {
                    npc<Neutral>(BOLREN, "Hello traveller, we must retrieve the orb. It's being held by Khazard troops north of here.")
                    player<Neutral>("Ok, I'll try my best.")
                }
            }
        }
    }

    private fun Player.chant(north: Boolean, text: String) {
        for (gnome in NPCs.at(Tile(2542, 3170).regionLevel)) {
            if (gnome.id == "local_gnome_tree_gnome_village" && (gnome.tile.y >= 3170) == north) {
                gnome.clearWatch()
                gnome.face(Tile(2543, 3170))
                gnome.anim("tree_gnome_village_chant")
                gnome.say(text)
            }
        }
    }

    private suspend fun Player.ceremony() {
        val king = NPCs.at(Tile(2542, 3170).regionLevel).firstOrNull { it.id == BOLREN }
        val previousMode = king?.mode
        try {
            repeat(2) {
                chant(true, "Su tana.")
                delay(2) // Two 600 ms server ticks approximate the observed one-second gap.
                chant(false, "En tania.")
                delay(2)
            }
            if (king != null) {
                king.walkTo(Tile(2542, 3169), forceWalk = true)
                var ticks = 0
                while (king.tile != Tile(2542, 3169) && ticks++ < 10) delay()
                king.mode = PauseMode
                king.clearWatch()
                king.face(Tile(2543, 3169))
            }
            if (king != null) {
                king.anim("tree_gnome_village_cast_orbs", delay = 20)
                king.gfx("tree_gnome_village_orbs", delay = 20, height = 124)
                delay(6)
                val tree = GameObjects.find(Tile(2543, 3168), "spirit_tree_gnome")
                tree.anim("tree_gnome_village_tree_orbs")
                delay(3) // Sequence 333 lasts 73 client frames, approximately 1.5 seconds.
                tree.anim("tree_gnome_village_tree_idle")
            }
        } finally {
            if (king != null && previousMode != null) king.mode = previousMode
        }
    }

    private suspend fun Player.montai() {
        when {
            // TODO reorder
            questStage("tree_gnome_village") >= 6 -> {
                player<Quiz>("Hello Montai, how are you?")
                npc<Bored>(MONTAI, "I'm ok, this battle is going to take longer to win than I expected. The Khazard troops won't give up even without the orb.")
                player<Happy>("Hang in there.")
            }
            quest("tree_gnome_village") == "orb_recovered" -> {
                player<Happy>("I have the orb of protection.")
                npc<Happy>(MONTAI, "Incredible, for a human you really are something.")
                player<Confused>("Thanks... I think!")
                npc<Neutral>(MONTAI, "I'll stay here with my troops and try and hold Khazard's men back. You return the orb to the gnome village. Go as quick as you can, the village is still unprotected.")
            }
            quest("tree_gnome_village") == "stronghold_open" -> {
                player<Happy>("I've breached the stronghold.")
                npc<Happy>(MONTAI, "I saw, that was a beautiful sight. The Khazard troops didn't know what hit them.")
                npc<Neutral>(MONTAI, "Now is the time to retrieve the orb. It's all in your hands. I'll be praying for you.")
            }
            get("tree_gnome_village_briefed", false) -> {
                player<Neutral>("Hello.")
                npc<Bored>(MONTAI, "Hello warrior. We need the coordinates for a direct hit from the ballista. Once you have a direct hit you will be able to enter the stronghold and retrieve the orb.")
            }
            quest("tree_gnome_village") == "logs_delivered" -> {
                player<Neutral>("How are you doing Montai?")
                npc<Bored>(MONTAI, "We're hanging in there soldier. For the next phase of our attack we need to breach their stronghold.")
                npc<Bored>(MONTAI, "The ballista can break through the stronghold wall, and then we can advance and seize back the orb.")
                player<Quiz>("So what's the problem?")
                npc<Bored>(MONTAI, "From this distance we can't get an accurate enough shot. We need the correct coordinates of the stronghold for a direct hit. I've sent out three tracker gnomes to gather them.")
                player<Quiz>("Have they returned?")
                npc<Sad>(MONTAI, "I'm afraid not, and we're running out of time. I need you to go into the heart of the battlefield, find the trackers, and bring back the coordinates.")
                npc<Quiz>(MONTAI, "Do you think you can do it?")
                TreeGnomeVillageProgress.brief(this)
                choice {
                    option<Sad>("No, I've had enough of your battle.") {
                        npc<Sad>(MONTAI, "I understand, this isn't your fight.")
                    }
                    option<Neutral>("I'll try my best.") {
                        npc<Happy>(MONTAI, "Thank you, you're braver than most.")
                        npc<Bored>(MONTAI, "I don't know how long I will be able to hold out. Once you have the coordinates come back and fire the ballista right into those monsters.")
                        npc<Bored>(MONTAI, "If you can retrieve the orb and bring safety back to my people, none of the blood spilled on this field will be in vain.")
                    }
                }
            }
            quest("tree_gnome_village") == "unstarted" -> {
                player<Neutral>("Hello.")
                npc<Angry>(MONTAI, "I can't talk now. Can't you see we're trying to win a battle here? If we can't hold back Khazard's men we're all doomed.")
            }
            !get("tree_gnome_village_montai_met", false) -> {
                player<Neutral>("Hello.")
                npc<Neutral>(MONTAI, "Hello traveller, are you here to help or just to watch?")
                player<Neutral>("I've been sent by King Bolren to retrieve the orb of protection.")
                npc<Happy>(MONTAI, "Excellent we need all the help we can get.")
                npc<Neutral>(MONTAI, "I'm commander Montai. The orb is in the Khazard stronghold to the north, but until we weaken their defences we can't get close.")
                player<Quiz>("What can I do?")
                npc<Neutral>(MONTAI, "Firstly we need to strengthen our own defences. We desperately need wood to make more battlements, once the battlements are gone it's all over. Six loads of normal logs should do it.")
                set("tree_gnome_village_montai_met", true)
                refreshQuestJournal()
                choice {
                    option<Neutral>("Ok, I'll gather some wood.") {
                        npc<Neutral>(MONTAI, "Please be as quick as you can, I don't know how much longer we can hold out.")
                    }
                    option<Sad>("Sorry, I no longer want to be involved.") {
                        npc<Sad>(MONTAI, "That's a shame, we could have done with your help.")
                    }
                }
            }
            else -> {
                player<Neutral>("Hello.")
                if (inventory.contains("logs", 6)) {
                    npc<Neutral>(MONTAI, "Hello again, we're still desperate for wood soldier.")
                    player<Happy>("I have some here.<br><red>(You give six loads of logs to the commander.)</col>")
                    if (TreeGnomeVillageProgress.deliverLogs(this)) {
                        npc<Happy>(MONTAI, "That's excellent, now we can make more defensive battlements. Give me a moment to organise the troops and then come speak to me. I'll inform you of our next phase of attack.")
                    }
                } else {
                    npc<Neutral>(MONTAI, "Hello again, we're still desperate for wood soldier. We need six loads of normal logs.")
                    player<Neutral>("I'll see what I can do.")
                    npc<Neutral>(MONTAI, "Thank you.")
                }
            }
        }
    }

    private suspend fun Player.tracker(number: Int, id: String) { // TODO split into individual dialogues
        val stage = questStage("tree_gnome_village")
        if (stage == 0) {
            player<Neutral>("Hello.")
            npc<Neutral>(id, if (number == 2) "I can't talk now. If the guards catch me I'll be dead gnome meat." else "I can't talk now. Can't you see we're trying to win a battle here?")
        } else if (stage >= 6) {
            player<Neutral>("Hello.")
            when (number) {
                1 -> npc<Bored>(id, "When will this battle end? I feel like I've been fighting forever.")
                2 -> npc<Bored>(id, "When will this battle end? I feel like I've been locked up my whole life.")
                else -> {
                    npc<Confused>(id, "I feel dizzy, where am I? Oh dear, oh dear I need some rest.")
                    player<Confused>("I think you do.")
                }
            }
        } else if (stage >= 4) {
            if (number == 3) {
                player<Neutral>("Hello again.")
                npc<Confused>(id, "Don't talk to me, you can't see me. No one can, just the demons.")
            } else if (stage == 5) {
                player<Neutral>("How are you tracker?")
                npc<Neutral>(id, if (number == 1) "Now we have the orb I'm much better. They won't stand a chance without it." else "Now we have the orb I'm much better. Soon my comrades will come and free me.")
            } else {
                player<Neutral>("Hello again.")
                npc<Neutral>(id, "Well done, you've broken down their defences. This battle must be ours.")
            }
        } else if (get("tree_gnome_village_briefed", false)) {
            when (number) {
                1 -> {
                    player<Quiz>("Do you know the coordinates of the Khazard stronghold?")
                    npc<Happy>(id, "I managed to get one, although it wasn't easy.")
                    statement("The gnome tells you the height coordinate.")
                    TreeGnomeVillageProgress.recordTracker(this, 1)
                    player<Happy>("Well done.")
                    npc<Neutral>(id, "The other two tracker gnomes should have the other coordinates if they're still alive.")
                    player<Neutral>("OK, take care.")
                }
                2 -> {
                    player<Quiz>("Are you OK?")
                    npc<Bored>(id, "They caught me spying on the stronghold. They beat and tortured me.")
                    npc<Angry>(id, "But I didn't crack. I told them nothing. They can't break me!")
                    player<Sad>("I'm sorry.")
                    npc<Shifty>(id, "Don't be. I have the position of the stronghold!")
                    statement("The gnome tells you the <col=003366>y</col> coordinate.")
                    TreeGnomeVillageProgress.recordTracker(this, 2)
                    player<Happy>("Well done.")
                    npc<Shifty>(id, "Now leave before they find you and all is lost.")
                    player<Neutral>("Hang in there.")
                    npc<Angry>(id, "Go!")
                }
                3 -> {
                    player<Quiz>("Are you OK?")
                    npc<Confused>(id, "OK? Who's OK? Not me! Hee hee!")
                    player<Quiz>("What's wrong?")
                    npc<Confused>(id, "You can't see me, no one can. Monsters, demons, they're all around me!")
                    player<Quiz>("What do you mean?")
                    npc<Confused>(id, "They're dancing, all of them, hee hee.")
                    statement("He's clearly lost the plot.")
                    player<Quiz>("Do you have the coordinate for the Khazard stronghold?")
                    npc<Confused>(id, "Who holds the stronghold?")
                    player<Confused>("What?")
                    val clue = when (get("tree_gnome_village_coordinate", 1)) {
                        1 -> "Less than my hands."
                        2 -> "More than my head, less than my fingers."
                        3 -> "More than we, less than our feet."
                        else -> "My legs and your legs, ha ha ha!"
                    }
                    npc<Confused>(id, clue)
                    TreeGnomeVillageProgress.recordTracker(this, 3)
                    player<Confused>("You're mad.")
                    npc<Confused>(id, "Dance with me, and Khazard's men are beat.")
                    statement("The toll of war has affected his mind.")
                    player<Sad>("I'll pray for you.")
                    npc<Confused>(id, "All day we pray in the hay, hee hee.")
                }
            }
        } else {
            when (number) {
                1 -> {
                    player<Neutral>("Hello.")
                    npc<Angry>(id, "I can't talk now. Can't you see we're trying to win a battle here?")
                }
                2 -> {
                    player<Neutral>("Hi there.")
                    npc<Neutral>(id, "The battle is far from over. If you have a pure heart you will help us win.")
                }
                3 -> {
                    player<Neutral>("Hi there.")
                    npc<Neutral>(id, "I can't stand this war. The misery, the pain, it's driving me crazy! When will it end?")
                }
            }
        }
    }
}
