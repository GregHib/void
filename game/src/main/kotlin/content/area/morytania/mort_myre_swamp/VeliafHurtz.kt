package content.area.morytania.mort_myre_swamp

import content.entity.combat.hit.damage
import content.entity.effect.transform
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Evil
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.proj.shoot
import content.quest.member.myreque.checkMembers
import content.quest.member.myreque.hasAllWeapons
import content.quest.member.myreque.myrequeStage
import content.quest.member.myreque.spawnHellHound
import content.quest.questStage
import content.quest.setInstanceLogout
import content.quest.startCutscene
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.moveCamera
import world.gregs.voidps.engine.client.turnCamera
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class VeliafHurtz : Script {
    init {
        npcOperate("Talk-to", "veliaf_hurtz_meiyerditch_tunnels") {
            when (questStage("in_search_of_the_myreque")) {
                myrequeStage("delivered_weapons") -> playMyrequeCutscene()
                myrequeStage("entered_hideout") -> firstMeeting()
                myrequeStage("met_veliaf") -> {
                    if (!checkMembers()) {
                        npc<Quiz>("Have you introduced yourself to the rest of the gang?")
                        introducedYourselfChoice()
                    } else {
                        npc<Neutral>(
                            "Hello again...so, you've introduced yourself to the team have you? Good. " +
                                "Now, let's have a look at those weapons you brought us.",
                        )
                        if (hasAllWeapons()) {
                            acceptWeapons()
                        } else {
                            wrongWeapons()
                        }
                    }
                }
                myrequeStage("weapons_accepted") -> {
                    if (hasAllWeapons()) {
                        deliverWeaponsAndStartCutscene()
                    } else {
                        wrongWeapons()
                    }
                }
                myrequeStage("hellhound_summoned") -> {
                    message("Veliaf can't talk right now, he seems to be formulating a plan.")
                }
                else -> postCutsceneIntro()
            }
        }
    }

    private suspend fun Player.firstMeeting() {
        player<Neutral>("Hello there...")
        npc<Quiz>("Hello there...how did you get in here? Who are you?")
        player<Neutral>(
            "I was asked to bring you some weapons...apparently you need them? Don't worry, " +
                "I'm a friend.",
        )
        set("in_search_of_the_myreque", "met_veliaf")
        npc<Neutral>(
            "Hmm, well, it's true we could do with some supplies. It's very good of you to bring " +
                "them to us. I'm sorry but I'm a bit busy at the moment, please introduce yourself " +
                "to the others, then we can talk about those weapons.",
        )
        firstMeetingChoice()
    }

    suspend fun Player.firstMeetingChoice() {
        choice {
            areYouLeader()
            tellMeAboutOrganization()
            tellMeAboutMorytania()
            futurePlans()
            option<Neutral>("Ok, thanks.")
        }
    }

    fun ChoiceOption.areYouLeader(): Unit = option<Neutral>("So are you the leader?") {
        npc<Neutral>(
            "Well, as much as anyone is, it's not official or anything, I guess the others just " +
                "look up to me or something. I feel a certain responsibility for them so I guess " +
                "that means I'm a leader of sorts.",
        )
        firstMeetingChoice()
    }

    fun ChoiceOption.tellMeAboutOrganization(): Unit = option<Quiz>("Can you tell me about your organization?") {
        npc<Neutral>(
            "Huh, organization? That's rich! We're really not that organized, however, the " +
                "Myreque are dedicated to mounting a resistance against the Drakans and the evil " +
                "that they've brough to Morytania.",
        )
        firstMeetingChoice()
    }

    fun ChoiceOption.tellMeAboutMorytania(): Unit = option<Quiz>("What can you tell me about Morytania?") {
        npc<Sad>(
            "I can tell you that it used to be a beautiful country before Lowerniel and his " +
                "quarelling siblings arrived.",
        )
        player<Neutral>("Lowerniel? Who's that?")
        npc<Angry>(
            "Lowerniel Vergidiyad Drakan - he's the head of the Drakan family and along with his " +
                "brother Ranis and his sister Vanescula they play out their petty backstabbing " +
                "games using the peoples of Morytania as their pawns.",
        )
        player<Neutral>("You sound very bitter.")
        npc<Sad>(
            "So would you if you lost everyone dear to you at the hands of such monsters.",
        )
        firstMeetingChoice()
    }

    fun ChoiceOption.futurePlans(): Unit = option<Quiz>("What are your plans for the future?") {
        npc<Neutral>(
            "At the moment we're maintaining a low profile after we were spotted scouting out " +
                "the Drakans' castle. The forest where we camped is infested with vampire, and " +
                "even though they're only juveniles in terms of experience, we only just managed " +
                "to survive.",
        )
        npc<Neutral>(
            "After we've regrouped, we will try to recruit more people to our cause, at the " +
                "minute we're hopelessly outnumberd.",
        )
        firstMeetingChoice()
    }

    suspend fun Player.introducedYourselfChoice() {
        choice {
            askQuestionsFirst()
            goIntroduceNow()
            whoElseToMeet()
        }
    }

    fun ChoiceOption.askQuestionsFirst(): Unit = option<Neutral>("I wanted to ask you some questions first.") {
        npc<Neutral>("Well, ok, but only briefly, I have things to do.")
        firstMeetingChoice()
    }

    fun ChoiceOption.goIntroduceNow(): Unit = option<Neutral>("Not yet, I'll go do that now.") {
        npc<Neutral>("Ok, great.")
    }

    fun ChoiceOption.whoElseToMeet(): Unit = option<Quiz>("Who else do I have to introduce myself to?") {
        val sani = if (get("met_sani", false)) "<str>Sani" else "<col=0080FF>Sani"
        val harold = if (get("met_harold", false)) "<str>Harold" else "<col=0080FF>Harold"
        val radigad = if (get("met_radigad", false)) "<str>Radigad" else "<col=0080FF>Radigad"
        val polmafi = if (get("met_polmafi", false)) "<str>Polmafi" else "<col=0080FF>Polmafi"
        val ivan = if (get("met_ivan", false)) "<str>Ivan" else "<col=0080FF>Ivan"
        statement("$sani<br>$harold<br>$radigad<br>$polmafi<br>$ivan")
    }

    private suspend fun Player.wrongWeapons() {
        npc<Neutral>(
            "Well, those weapons aren't that useful. We really need specific ones, we have our " +
                "own specialism.",
        )
        weaponChoice()
    }

    suspend fun Player.weaponChoice() {
        choice {
            goGetWeapons()
            whichWeapons()
        }
    }

    fun ChoiceOption.goGetWeapons(): Unit = option<Neutral>("Ok, I'll go get them.") {
        npc<Neutral>("Ok, I'd appreciate that.")
    }

    fun ChoiceOption.whichWeapons(): Unit = option<Quiz>("Which weapons do you need?") {
        npc<Neutral>("Here, I've compiled a list.")
        showWeaponList()
    }

    private suspend fun Player.acceptWeapons() {
        set("in_search_of_the_myreque", "weapons_accepted")
        npc<Happy>("These weapons look great! Many thanks.")
        deliverWeaponsAndStartCutscene()
    }

    private suspend fun Player.deliverWeaponsAndStartCutscene() {
        inventory.remove("steel_longsword", 1)
        inventory.remove("steel_dagger", 1)
        inventory.remove("steel_mace", 1)
        inventory.remove("steel_warhammer", 1)
        inventory.remove("steel_sword", 2)
        set("thsfm_vanstrom_hide", true)
        set("in_search_of_the_myreque", "delivered_weapons")
        playMyrequeCutscene()
    }

    private suspend fun Player.showWeaponList() {
        val longsword = weapon(inventory.contains("steel_longsword"), "1 Steel ", "Longsword")
        val shortSword = weapon(inventory.contains("steel_sword", 2), "2 Steel ", "Short Swords")
        val dagger = weapon(inventory.contains("steel_dagger"), "1 Steel ", "Dagger")
        val mace = weapon(inventory.contains("steel_mace"), "1 Steel ", "Mace")
        val warhammer = weapon(inventory.contains("steel_warhammer"), "1 Steel ", "Warhammer")
        statement("$longsword $shortSword $dagger $mace $warhammer")
    }

    private fun weapon(has: Boolean, prefix: String, name: String): String = if (has) {
        "<str>$prefix$name</str>"
    } else {
        "<col=0080FF>$prefix</col><col=660000>$name</col>"
    }

    private suspend fun Player.playMyrequeCutscene() {
        open("fade_out")
        val cutscene = startCutscene("myreque_ambush", AMBUSH_BASE, width = 6, height = 3)
        setInstanceLogout(VELIAF_SPOT)
        cutscene.onEnd {
            clearCamera()
        }
        delay(4)

        val veliaf = NPCs.add("veliaf_hurtz_meiyerditch_tunnels", cutscene.convert(VELIAF_SPOT), Direction.SOUTH)
        val harold = NPCs.add("harold_evans_meiyerditch_tunnels", cutscene.convert(HAROLD_SPOT), Direction.SOUTH)
        val sani = NPCs.add("sani_piliu_meiyerditch_tunnels", cutscene.convert(SANI_SPOT), Direction.SOUTH)
        val radigad = NPCs.add("radigad_ponfit", cutscene.convert(RADIGAD_SPOT), Direction.EAST)
        for (member in listOf(veliaf, harold, sani, radigad)) {
            member.mode = PauseMode
        }
        tele(cutscene.convert(PLAYER_SPOT), clearInterfaces = false)
        delay(1)
        veliaf.face(cutscene.tile(3506, 9837, 2))
        harold.face(cutscene.tile(3504, 9836, 2))
        sani.face(cutscene.tile(3508, 9836, 2))
        radigad.face(cutscene.tile(3511, 9831, 2))

        moveCamera(cutscene.tile(3506, 9846, 2), 750)
        turnCamera(cutscene.tile(3506, 9834, 2), 125)
        open("fade_in")
        delay(3)
        face(cutscene.convert(VANSTROM_SPOT))
        moveCamera(cutscene.tile(3508, 9836, 2), 325)
        turnCamera(cutscene.tile(3502, 9842, 2), 125)
        npc<Angry>(
            "veliaf_hurtz_meiyerditch_tunnels",
            """
                Hey, what's that mist coming through the door!
                WE'VE A VAMPIRE IN THE ROOM!
            """,
        )

        moveCamera(cutscene.tile(3506, 9846, 2), 750)
        turnCamera(cutscene.tile(3506, 9834, 2), 125)
        message("You see a shifting mist enter the room!")
        sound("vanstrom_mist")
        val mist = NPCs.add("route_vanstrom_vampire_misty", cutscene.tile(3504, 9832, 2), Direction.SOUTH)
        delay(1)
        mist.walkTo(cutscene.tile(3505, 9835, 2))
        delay(3)
        NPCs.remove(mist)
        sound("vanstrom_appear")

        val vanstrom = NPCs.add("route_vanstrom", cutscene.convert(VANSTROM_SPOT), Direction.SOUTH)
        vanstrom.mode = PauseMode
        vanstrom.face(cutscene.convert(PLAYER_SPOT))
        face(cutscene.convert(VANSTROM_SPOT))
        vanstrom.say("Ha...ha...ha... you took me straight to them!")
        message("Vanstrom Klause: Ha...ha...ha...you took me straight to them!")
        npc<Neutral>("stranger_2", "Ha...ha...ha... you took me straight to them!")
        vanstrom.say("The little dears are going to wonder which side you're on!")
        message("Vanstrom Klause: The little dears are going to wonder which side you're on!")
        npc<Neutral>("stranger_2", "The little dears are going to wonder which side you're on!")

        moveCamera(cutscene.tile(3511, 9839, 2), 325)
        turnCamera(cutscene.tile(3506, 9837, 2), 125)
        face(cutscene.convert(SANI_SPOT))
        vanstrom.face(cutscene.convert(SANI_SPOT))
        sani.face(cutscene.convert(VANSTROM_SPOT))
        sani.say("It's Vanstrom...we're dead...")
        message("Sani Piliu: It's Vanstrom...we're dead...")
        npc<Sad>("sani_piliu_meiyerditch_tunnels", "It's Vanstrom...we're dead...")

        face(cutscene.convert(VANSTROM_SPOT))
        vanstrom.strike(sani, cutscene.convert(VANSTROM_SPOT))
        sani.say("Ahhhhhh!")
        vanstrom.say("Quite right...")
        message("Vanstrom Klause: Quite right....")
        npc<Neutral>("stranger_2", "Quite right...")

        turnCamera(cutscene.tile(3506, 9838, 2), 125, speed = 3, acceleration = 3)
        delay(1)
        turnCamera(cutscene.tile(3506, 9837, 2), 125, speed = 3, acceleration = 3)
        vanstrom.face(cutscene.convert(HAROLD_SPOT))
        vanstrom.strike(harold, cutscene.convert(VANSTROM_SPOT).add(-1, 0))
        harold.say("Auugh!")
        vanstrom.anim("vanstrom_transform")
        vanstrom.transform("route_vanstrom_vampire")
        vanstrom.say("Sorry, Harold, you too!")
        message("Vanstrom Klause: Sorry Harold, you too!")
        npc<Neutral>("stranger_2", "Sorry, Harold, you too!")

        delay(2)
        vanstrom.transform("route_vanstrom_vampire_flying")
        vanstrom.anim("vanstrom_hover")
        sound("vanstrom_vampire")
        vanstrom.say("And now I'm going to finish the rest of them off.")
        message("Vanstrom Klause: And... now I'm going to finish the rest of them off...")
        npc<Neutral>("route_vanstrom_vampire", "And now I'm going to finish the rest of them off.")

        vanstrom.face(cutscene.convert(PLAYER_SPOT))
        vanstrom.say("With my little pet!")
        message("Vanstrom Klause: With my little pet!")
        npc<Neutral>("route_vanstrom_vampire", "With my little pet!")
        vanstrom.transform("route_vanstrom_vampire_misty")
        vanstrom.say("Ha ha ha ha haaaaaa!")
        sound("vanstrom_mist")
        delay(2)
        vanstrom.walkOverDelay(cutscene.tile(3504, 9832, 2))
        NPCs.remove(vanstrom)
        message("Vanstrom Klause: Ha ha ha ha haaaaaa!")
        npc<Evil>("route_vanstrom_vampire", "Ha ha ha ha haaaaaa!")

        open("fade_out")
        delay(2)
        set("in_search_of_the_myreque", "hellhound_summoned")
        tele(HOUND_SPOT)
        cutscene.end()
        spawnHellHound(this)
    }

    private fun NPC.strike(target: NPC, from: Tile) {
        anim("thrown_accurate")
        gfx("iron_knife_throw")
        from.shoot("iron_knife", target)
        target.anim("myreque_knockout")
        target["absorb_damage"] = 0
        target.damage(300, source = this)
    }

    private suspend fun Player.postCutsceneIntro() {
        npc<Sad>(
            "That murderer Vanstrom has killed Sani and Harold! He came in and killed them right " +
                "in front of our eyes. And that beast he summoned probably would have killed the " +
                "rest of us if it wasn't for you!",
        )
        player<Neutral>(
            "It was the least I could do! After all, I was the one he followed here, I'm sort of " +
                "responsible.",
        )
        npc<Neutral>(
            "That is for your conscience to bear, but now perhaps you can understand why we " +
                "fight for our freedom?",
        )
        player<Neutral>("Yes, I think I understand now.")
        aftermathChoice()
    }

    suspend fun Player.aftermathChoice() {
        choice {
            joinOrganisation()
            whatNext()
            howToGetOut()
            getRevenge()
            option<Neutral>("Ok, thanks.")
        }
    }

    fun ChoiceOption.joinOrganisation(): Unit = option<Neutral>("I want to join your organisation.") {
        npc<Neutral>(
            "Hmm, well thanks for your support. But I'll need to talk to my superior first.",
        )
        aftermathChoice()
    }

    fun ChoiceOption.whatNext(): Unit = option<Quiz>("What do you plan to do now?") {
        npc<Neutral>(
            "Well, I guess Vanstrom will expect that we're dead. He's so arrogant that he doesn't " +
                "really consider us a threat. I can understand that, if I was that powerful I " +
                "would probably feel the same.",
        )
        npc<Neutral>(
            "I guess we'll regroup and then try to recruit more people. We first need to know " +
                "how we can fight against these monsters, then we'll take the war to them!",
        )
        aftermathChoice()
    }

    fun ChoiceOption.howToGetOut(): Unit = option<Quiz>("How do I get out of here?") {
        npc<Neutral>(
            "If you go back into the main corridor which leads into this room, it goes down " +
                "towards the basement of an Inn.",
        )
        if (questStage("in_search_of_the_myreque") == myrequeStage("killed_hellhound")) {
            set("in_search_of_the_myreque", "shown_way_out")
        }
        npc<Neutral>(
            "If you search the wall, you'll find that it is in fact moveable. You should be able " +
                "to get into the room, take the ladder to come out to the south of the " +
                "'Hair of the Dog Tavern' in Canifis.",
        )
        aftermathChoice()
    }

    fun ChoiceOption.getRevenge(): Unit = option<Quiz>("Let's get revenge on Vanstrom!") {
        npc<Happy>(
            "Yes, of course, we'll certainly get our revenge on him and his superiors! Then " +
                "they'll taste the bitter sorrow that we've had to endure so long!",
        )
        aftermathChoice()
    }

    private companion object {
        private val AMBUSH_BASE = Tile(3496, 9824)

        private val PLAYER_SPOT = Tile(3507, 9838, 2)
        private val VELIAF_SPOT = Tile(3506, 9838, 2)
        private val SANI_SPOT = Tile(3508, 9837, 2)
        private val HAROLD_SPOT = Tile(3504, 9837, 2)
        private val RADIGAD_SPOT = Tile(3510, 9831, 2)
        private val VANSTROM_SPOT = Tile(3506, 9836, 2)

        private val HOUND_SPOT = Tile(3507, 9838, 0)
    }
}
