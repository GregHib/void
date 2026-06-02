package content.area.kandarin.yanille

import content.entity.npc.findNearbyNPC
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.items
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.inv.item.addOrDrop
import content.quest.member.hand_in_the_sand.sendHandQuestReward
import content.quest.member.ogre.zogre_flesh_eaters
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

class ZavisticRarve : Script {

    lateinit var npc: NPC

    init {
        // ===== Plain Talk-to (or via bell) =====

        npcOperate("Talk-to", "zavistic_rarve") { (target) ->
            npc = target
            val sandProgress = get("hand_in_the_sand", 0)
            val zogreProgress = zogre_flesh_eaters
            if (sandProgress < 40 && zogreProgress < 4) {
                npc<Neutral>(
                    "What are you doing bothering me? Don't you think some of us have work to do?",
                )
                player<Neutral>("I thought you were here to help?")
                npc<Neutral>(
                    "Well... I am, I suppose, anyway... we're very busy here, hurry up, what " +
                        "do you want?",
                )
            } else {
                npc<Neutral>(
                    "What are you doing...Oh, it's you...sorry...didn't realise...what can I " +
                        "do for you?",
                )
            }
            mainMenu()
        }

        // ===== Bell-rung Talk-to =====

        objectOperate("Ring", "zogre_outdoor_bell") { (target) ->
            sound("zogre_bell")
            target.anim("zogre_bell_ring")
            var zavistic = findNearbyNPC("zavistic_rarve")
            if (zavistic  == null) {
                zavistic = NPCs.add(
                    id = "zavistic_rarve",
                    tile = Tile(2598, 3087, 0),
                    ticks = 200,
                )
            }
            talkWith(zavistic)

            val sandProgress = get("hand_in_the_sand", 0)
            val zogreProgress = zogre_flesh_eaters
            if (sandProgress < 40 && zogreProgress < 4) {
                npc<Neutral>(
                    "What are you doing ringing that bell?! Don't you think some of us have " +
                        "work to do?",
                )
                player<Neutral>("But I was told to ring the bell if I wanted some attention.")
                npc<Neutral>(
                    "Well...anyway...we're very busy here, hurry up what do you want?",
                )
            } else {
                npc<Neutral>(
                    "What are you doing...Oh, it's you...sorry...didn't realise...what can I " +
                        "do for you?",
                )
            }
            mainMenu()
        }

        // ===== Item-on-Zavistic =====

        itemOnNPCOperate("beer_hand", "zavistic_rarve") {
            handInTheSandHandReveal()
        }

        itemOnNPCOperate("magic_scroll", "zavistic_rarve") {
            magicScrollReveal()
        }

        itemOnNPCOperate("black_prism", "zavistic_rarve") {
            player<Neutral>(
                "I found this black prism at Jiggig where the undead ogre activity was happening?",
            )
            showBlackPrism()
        }

        itemOnNPCOperate("torn_page", "zavistic_rarve") {
            player<Neutral>(
                "I think I've found a clue from the Jiggig area where the undead ogre activity " +
                    "is happening.",
            )
            showTornPage()
        }

        itemOnNPCOperate("dragon_inn_tankard", "zavistic_rarve") {
            item(item = "dragon_inn_tankard", text = "You show the dragon Inn Tankard to Zavistic.")
            showTankard()
        }

        itemOnNPCOperate("necromancy_book", "zavistic_rarve") {
            item(item = "necromancy_book", text = "You show the Necromancy book to Zavistic.")
            showNecromancyBook()
        }

        itemOnNPCOperate("book_of_ham", "zavistic_rarve") {
            item(item = "book_of_ham", text = "You show the HAM book to Zavistic.")
            showHamBook()
        }

        itemOnNPCOperate("zogre_sithik_portrait_signed", "zavistic_rarve") {
            item(item = "zogre_sithik_portrait_signed", text = "You show the signed portrait of Sithik to Zavistic.")
            showSignedPortrait()
        }

        itemOnNPCOperate("zogre_sithik_portrait_good", "zavistic_rarve") {
            item(item = "zogre_sithik_portrait_good", text = "You show the portrait of Sithik to Zavistic.")
            npc<Neutral>("Hmm, great...but I already know what he looks like!")
        }

        itemOnNPCOperate("zogre_sithik_portrait_bad", "zavistic_rarve") {
            player<Neutral>("Look, I made a portrait of Sithik.")
            item(item = "zogre_sithik_portrait_bad", text = "You show the sketch...")
            npc<Neutral>(
                "Who the demonikin is that? Is it meant to be a portrait of Sithik, it doesn't " +
                    "look anything like him!",
            )
        }
    }

    // ===== Top-level menu router =====

    private suspend fun Player.mainMenu() {
        val sand = get("hand_in_the_sand", 0) >= 20
        val zogre = zogre_flesh_eaters >= 3
        when {
            zogre && sand -> {
                choice {
                    aboutZogres()
                    aboutSand()
                }
            }
            zogre -> sendZogreChat()
            sand -> sendSandChat()
            else -> guildMenu()
        }
    }

    fun ChoiceOption.aboutZogres(): Unit = option<Neutral>("I'm here about the sicks...err Zogres") {
        sendZogreChat()
    }

    fun ChoiceOption.aboutSand(): Unit = option<Neutral>(
        "I have a rather sandy problem that I'd like to palm off on you.",
    ) {
        sendSandChat()
    }

    // ===== Wizards' Guild info menu (shared default) =====

    suspend fun Player.guildMenu() {
        choice {
            whatIsThereToDo()
            whatAreRequirements()
            whatDoYouDo()
            okThanks()
        }
    }

    suspend fun Player.guildMenuWithOrbHelp() {
        choice {
            whatIsThereToDo()
            whatAreRequirements()
            whatDoYouDo()
            if (inventory.contains("magical_orb") || inventory.contains("magical_orb_active")) {
                canYouHelpMore()
            } else {
                lostOrb()
            }
        }
    }

    suspend fun Player.guildMenuWithLostOrb() {
        choice {
            whatIsThereToDo()
            whatAreRequirements()
            whatDoYouDo()
            lostOrb()
        }
    }

    fun ChoiceOption.whatIsThereToDo(): Unit = option<Quiz>(
        "What is there to do in the Wizards' Guild?",
    ) {
        npc<Neutral>(
            "This is the finest wizards' establishment in the land. We have magic portals to " +
                "the other towers of wizardry around RuneScape. We have a particularly wide " +
                "collection of runes in our rune shop. We sell some of",
        )
        npc<Neutral>(
            "the finest mage robes in the land and we have a training area full of zombies for " +
                "you to practice your magic on.",
        )
        guildMenu()
    }

    fun ChoiceOption.whatAreRequirements(): Unit = option<Quiz>(
        "What are the requirements to get in the Wizards' Guild?",
    ) {
        npc<Neutral>(
            "You need a magic level of 66, the high magic energy level is too dangerous for " +
                "anyone below that level.",
        )
        guildMenu()
    }

    fun ChoiceOption.whatDoYouDo(): Unit = option<Neutral>("What do you do in the Guild?") {
        npc<Neutral>(
            "I'm the Grand Secretary for the Wizards' Guild, I have lots of correspondence to " +
                "keep up with, as well as attending to the discipline of the more problematic " +
                "guild members.",
        )
        guildMenu()
    }

    fun ChoiceOption.okThanks(): Unit = option<Neutral>("Ok, thanks.")

    fun ChoiceOption.canYouHelpMore(): Unit = option<Quiz>("Can you help me more?") {
        helpMoreFlow()
    }

    fun ChoiceOption.lostOrb(): Unit = option<Sad>("I've lost my magical scrying orb!") {
        replaceOrb()
    }

    // ===== HAND IN THE SAND branches =====

    private suspend fun Player.sendSandChat() {
        when (get("hand_in_the_sand", 0)) {
            20 -> {
                if (inventory.contains("beer_hand")) {
                    handInTheSandHandReveal()
                } else {
                    statement("Maybe you should have the hand with you before speaking to Zavistic.")
                }
            }
            30, 40, 50 -> {
                npc<Quiz>("Did you find out who killed Clarence yet?")
                player<Sad>("Not yet, but don't lose your head over it.")
            }
            60 -> {
                if (inventory.contains("magic_scroll")) {
                    magicScrollReveal()
                } else {
                    statement(
                        "Perhaps you should have the scroll from Bert with you before you " +
                            "speak to Zavistic.",
                    )
                }
            }
            70 -> guildMenuWithOrbHelp()
            80, 90, 100 -> {
                npc<Quiz>("Have you made the serum and talked to Sandy yet?")
                if (inventory.contains("magical_orb")) {
                    player<Neutral>("Not yet, but don't bust a gut over it!")
                } else {
                    player<Sad>("I've lost my magical scrying orb!")
                    replaceOrb()
                }
            }
            110 -> guildMenuWithLostOrb()
            120 -> {
                if (inventory.contains("magical_orb_active")) {
                    statement(
                        "You hand the magical scrying orb to the Wizard and watch as the " +
                            "recording is played back.",
                    )
                    npc<Angry>(
                        "Well, well...I think this Sandy needs a lesson, please bring me 5 " +
                            "earth runes and a bucket of sand.",
                    )
                    runesAndSandRequest()
                } else {
                    player<Sad>("I got the whole story from Sandy... but I lost the orb.")
                    npc<Neutral>(
                        "It's ok, I saw the whole thing as the orb is connected via magic to " +
                            "me as I enchanted it.",
                    )
                    npc<Angry>(
                        "I think this Sandy needs a lesson, please bring me 5 earth runes and " +
                            "a bucket of sand.",
                    )
                    runesAndSandRequest()
                }
            }
            130 -> {
                if (inventory.contains("earth_rune", 5) && inventory.contains("bucket_of_sand")) {
                    player<Quiz>("I've brought what you wanted, what are you going to do?")
                    sandpitRefillCutscene()
                } else {
                    npc<Happy>(
                        "You really mean you forgot? Bring me 5 earth runes and 1 bucket of " +
                            "sand to help stop that moneygrabbing Sandy!",
                    )
                }
            }
            140 -> {
                npc<Quiz>(
                    "Did you visit the Entrana sandpit yet? Ask the worker there if he's found " +
                        "an arm or a leg.",
                )
                player<Neutral>(
                    "Not yet no. I've been running around like a headless chicken, but I'll " +
                        "get to it!",
                )
            }
            150 -> {
                if (inventory.contains("wizard_head")) {
                    item(item = "wizard_head", text = "You show the wizard the head.")
                    npc<Sad>("Alas poor Clarence. I knew him, $name.")
                    npc<Neutral>(
                        "Thank you - we shall bury him today. I have sent word for the guards " +
                            "to arrest Sandy, so no one will ever see him again!",
                    )
                    sendHandQuestReward()
                } else {
                    statement(
                        "Perhaps you should have the wizard's head with you before speaking to " +
                            "Zavistic.",
                    )
                }
            }
            160 -> {
                npc<Happy>(
                    "Thank you so much for helping to lay Clarence to rest and lock up his " +
                        "murderer!",
                )
                guildMenu()
            }
            else -> guildMenu()
        }
    }

    // ===== Hand reveal flow (the murder is revealed) =====

    private suspend fun Player.handInTheSandHandReveal() {
        if (!inventory.contains("beer_hand")) {
            statement("Maybe you should have the hand with you before speaking to Zavistic.")
            return
        }
        player<Quiz>("Ummm... Do you have all your wizards?")
        npc<Confused>("All my.... whatever do you mean...?")
        player<Neutral>(
            "The Guard Captain asked me to see if you have any... missing... wizards.",
        )
        npc<Shock>("That's silly! No one would kill a wizard... would they?")
        player<Shifty>("Erm... no... ")
        player<Neutral>(
            "Well.. maybe, you see Bert found this hand and it might belong to.. a wizard!",
        )
        npc<Quiz>(
            "Bert? Ahh yes, the sandman who seems to have been working very long hours " +
                "recently. Let's see that hand...",
        )
        set("hand_in_the_sand", 30)
        inventory.remove("beer_hand")
        item(item = "beer_hand", text = "You hand it over.")
        npc<Shock>(
            "Oh my! This is most definitely Clarence, my most able student! You must find out " +
                "who did this!",
        )
        player<Neutral>("Do you have any input as to the matter at hand?")
        npc<Neutral>(
            "Well.... Ask Bert about the long hours he's been working, that sounds suspicious " +
                "to me. Digging things up at all hours of the day isn't natural.",
        )
    }

    // ===== Magic scroll reveal (mind-altering spell) =====

    private suspend fun Player.magicScrollReveal() {
        if (!inventory.contains("magic_scroll")) {
            statement("Perhaps you should have the scroll from Bert with you before you speak to Zavistic.")
            return
        }
        player<Neutral>(
            "I talked to Bert and found something very strange about his hours.",
        )
        npc<Quiz>("Oh? Did he kill Clarence?")
        player<Neutral>(
            "No, but he doesn't remember changing his hours, and his rota and the original " +
                "that his boss Sandy had, are different! ",
        )
        player<Neutral>(
            "... oh, and this scroll appeared when they changed - he gave it to me.",
        )
        npc<Happy>(
            "I recognise that type of scroll! It's used in a mind altering spell of some sort. " +
                "Did you speak to this... Sandy guy? Perhaps he has a hand in this.",
        )
        player<Neutral>(
            "I took a look around his office. I don't know about a hand in it, I think he has " +
                "both hands and feet in it!",
        )
        npc<Neutral>(
            "Even more suspicious! Here, take this magical scrying orb and get some Truth " +
                "Serum from Betty in Port Sarim, she owes me a favour, just tell her I sent " +
                "you if she complains.",
        )
        npc<Neutral>(
            "Then you will be equipped to ask Sandy a few questions. Oh Clarence, I will find " +
                "your murderer!",
        )
        set("hand_in_the_sand", 70)
        inventory.remove("magic_scroll")
        addOrDrop("magical_orb")
        item(
            item = "magical_orb",
            text = "You exchange the scroll for the magical scrying orb. Perhaps Zavistic can " +
                "give you even more of a hand to find the murderer?",
        )
    }

    // ===== "Can you help me more?" / replace orb / teleport =====

    private suspend fun Player.helpMoreFlow() {
        if (get("handsand_tele", false)) {
            npc<Sad>(
                "Unfortunately I've already helped you with one teleport, get some exercise - " +
                    "your legs won't fall off!",
            )
            return
        }
        npc<Happy>("Bring me a vial and I'll help you a little more.")
        if (!inventory.contains("vial")) return
        player<Happy>("I have a vial here for you.")
        npc<Quiz>(
            "Ok, would you like me to transport you to Port Sarim? I'm sticking my neck out " +
                "a bit helping you like this and can only do it once though!",
        )
        choice {
            yesTeleport()
            noTeleport()
        }
    }

    fun ChoiceOption.yesTeleport(): Unit = option<Happy>("Yes, that would be great!") {
        npc<Neutral>("Off you go then, break a leg!")
        portSarimTeleport()
    }

    fun ChoiceOption.noTeleport(): Unit = option<Sad>("No, I prefer using my legs, thanks all the same.") {
        npc<Neutral>("Ok, suit yourself!")
    }

    private suspend fun Player.replaceOrb() {
        if (inventory.contains("magical_orb") || inventory.contains("magical_orb_active")) {
            // Already has one
            return
        }
        if (inventory.isFull()) {
            npc<Sad>(
                "I'd give you another magical scrying orb if you had some space in your " +
                    "inventory.",
            )
            return
        }
        if (get("hand_in_the_sand", 0) == 110) {
            addOrDrop("magical_orb_active")
            npc<Neutral>(
                "No matter, here, have another I've already activated it for you!",
            )
        } else {
            addOrDrop("magical_orb")
            npc<Neutral>(
                "No matter, here, have another and please hurry, whoever killed Clarence must " +
                    "pay!",
            )
        }
    }

    // ===== Port Sarim teleport cutscene =====

    private suspend fun Player.portSarimTeleport() {
        set("handsand_tele", true)
        inventory.remove("vial")
        // npc.anim("human_castentangle") TODO
        delay(2)
        gfx("pickaxe_summon_effect_spotanim", height = 92)
        anim("human_shrink", delay = 4)
        sound("teleport_all")
        delay(3)
        clearAnim()
        tele(3014, 3259)
    }

    // ===== Runes and sand request continuation =====

    private suspend fun Player.runesAndSandRequest() {
        set("hand_in_the_sand", 130)
        inventory.remove("magical_orb_active")
        player<Shock>("Erm, why?")
        npc<Angry>(
            "Don't question me or you'll end up as braindead as that legless Guard Captain!",
        )
        player<Scared>("Umm.. ok, I'll get you the 5 earth runes and bucket of sand.")
    }

    // ===== Sandpit refill cutscene (instanced) =====

    private suspend fun Player.sandpitRefillCutscene() {
        npc<Happy>("Ahh excellent, let's have those! Watch and learn...")
        // TODO: full instanced cutscene
        // - Create instance at base (317, 386), 3x3 size
        // - Spawn Bert NPC (id 3108) inside the instance
        // - Fade out, start cutscene mode
        // - Camera move to (2536, 3109) height 850, look at (2544, 3102) height 25
        // - Wizard chants — show info dialogue:
        //   "The Wizard chants and your attention is taken to the sandpit where Bert found the hand."
        // - Bert walks to (2542, 3101), faces (2542, 3103)
        // - Bert animates 2702, sandpit object animates 3037, sound 1591
        // - Bert says "My sand! My lovely sand"
        // - Show info dialogue:
        //   "Something very strange happens to the Sandpit, it looks like it has filled itself up!"
        // - Set varbit 278 to 1 (sandpit refilled flag)
        // - Fade out, destroy instance, reset camera, fade in
        // - Delete 5 earth runes, 1 bucket of sand
        // - Set hand_in_the_sand to 140

        statement(
            "The Wizard chants and your attention is taken to the sandpit where Bert found " +
                "the hand.",
        )
        statement(
            "Something very strange happens to the Sandpit, it looks like it has filled " +
                "itself up!",
        )
        inventory.remove("earth_rune", 5)
        inventory.remove("bucket_of_sand")
        set("hand_in_the_sand", 140)
        npc<Happy>(
            "There, the sand pit will now magically refill. No more work for Bert! ",
        )
        npc<Neutral>(
            "We must find the rest of Clarence, I've sent some wizards out to some of the " +
                "sandpits, would you please check the Entrana sandpit?",
        )
    }

    // ===== ZOGRE FLESH EATERS branches =====

    private suspend fun Player.sendZogreChat() {
        val progress = zogre_flesh_eaters
        val sithikIntro = get("thzfe_prismsearch", 0)

        when {
            progress >= 8 -> {
                npc<Neutral>(
                    "Don't you worry about Sithik, he's not likely to be moving from his bed " +
                        "for a long time. When he eventually does get better, he's going to " +
                        "be sent before a disciplinary tribunal, then we'll sort out what's what.",
                )
                player<Neutral>("Thanks for your help with all of this.")
                npc<Neutral>(
                    "Ooohh, no thanks required. It's I who should be thanking you my friend..." +
                        "your investigative mind has shown how vigilant we really should be " +
                        "for this type of evil use of the magical arts.",
                )
                guildMenu()
            }
            progress == 4 || progress == 6 -> {
                npc<Neutral>("Have you used that potion yet?")
                if (progress == 6) {
                    yesUsedPotion()
                } else if (inventory.contains("zogre_ogre_trans_potion")) {
                    notYetUsedPotion()
                } else {
                    lostPotion()
                }
            }
            sithikIntro == 5 -> sithikInvestigationMenu()
            sithikIntro == 4 -> sithikInvestigationMenuLimited()
            inventory.contains("black_prism") && inventory.contains("torn_page") -> {
                player<Neutral>(
                    "There's some undead ogre activity over at Jiggig, I've found some clues, " +
                        "I wondered if you'd have a look at them.",
                )
                showBothClues()
            }
            inventory.contains("black_prism") -> {
                player<Neutral>(
                    "There's some undead ogre activity over at 'Jiggig', and the ogres have " +
                        "asked me to look into it. I think I've found a clue and I wonder if " +
                        "you could take a look at it for me?",
                )
                showBlackPrism()
            }
            inventory.contains("torn_page") -> {
                player<Neutral>(
                    "There's some undead ogre activity over at Jiggig, I've found a clue that " +
                        "you may be able to help with.",
                )
                showTornPage()
            }
            else -> guildMenu()
        }
    }

    // ===== Sithik investigation menus =====

    suspend fun Player.sithikInvestigationMenuLimited() {
        val evidenceCount = evidenceCount()
        choice {
            whatDidYouSayShouldDo()
            whereIsSithik()
            if (hasEvidence()) showEvidenceOption(evidenceCount)
            wantToAskAboutGuild()
            sorryHaveToGo()
        }
    }

    suspend fun Player.sithikInvestigationMenu() {
        val evidenceCount = evidenceCount()
        choice {
            whatDidYouSayShouldDo()
            whereIsSithik()
            if (hasEvidence()) showEvidenceOption(evidenceCount) else canYouHelp()
            wantToAskAboutGuild()
            sorryHaveToGo()
        }
    }

    fun ChoiceOption.whatDidYouSayShouldDo(): Unit = option<Neutral>(
        "What did you say I should do?",
    ) {
        npc<Neutral>(
            "You should go and have a chat with Sithik Ints, he's in that house just to the " +
                "north. He's a lodger and has a room upstairs. Just tell him that I sent you " +
                "to see him. He should be fine once you've mentioned my name.",
        )
        sithikInvestigationMenu()
    }

    fun ChoiceOption.whereIsSithik(): Unit = option<Neutral>("Where is Sithik?") {
        npc<Neutral>(
            "He's in that house just to the north, less than a few seconds walk away. He's a " +
                "lodger and has a room upstairs...he's not very well though.",
        )
        sithikInvestigationMenu()
    }

    fun ChoiceOption.showEvidenceOption(evidenceCount: Int) {
        val text = if (evidenceCount == 1) {
            "I have an item that I'd like you to look at."
        } else {
            "I have some items that I'd like you to look at."
        }
        return option<Neutral>(text) {
            showAllEvidence()
        }
    }

    // Workaround helper since ChoiceOption doesn't expose Player directly in some patterns
    private fun Player.evidenceCount(): Int {
        var count = 0
        if (inventory.contains("necromancy_book")) count++
        if (inventory.contains("book_of_ham")) count++
        if (inventory.contains("dragon_inn_tankard")) count++
        if (inventory.contains("signed_portrait")) count++
        return count
    }

    private fun Player.hasEvidence(): Boolean = evidenceCount() > 0

    fun ChoiceOption.canYouHelp(): Unit = option<Neutral>("Can you help me?") {
        npc<Neutral>(
            "I'm happy to help as much as I can but you have to remember that I'm quite busy. " +
                "If you find any more clues about what happened at Jiggig, I'll consider " +
                "them with an open mind - that's as much as I can offer.",
        )
        sithikInvestigationMenu()
    }

    fun ChoiceOption.wantToAskAboutGuild(): Unit = option<Neutral>(
        "I want to ask about the Magic Guild.",
    ) {
        npc<Neutral>("Sure, go ahead, ask away.")
        guildMenu()
    }

    fun ChoiceOption.sorryHaveToGo(): Unit = option<Neutral>("Sorry, I have to go.")

    // ===== Cycle through all evidence the player has =====

    private suspend fun Player.showAllEvidence() {
        // Order: necromancy book, HAM book, tankard, signed portrait
        if (inventory.contains("necromancy_book")) {
            item(item = "necromancy_book", text = "You show the Necromancy book to Zavistic.")
            showNecromancyBook()
        }
        if (inventory.contains("book_of_ham")) {
            item(item = "book_of_ham", text = "You show the HAM book to Zavistic.")
            showHamBook()
        }
        if (inventory.contains("dragon_inn_tankard")) {
            item(item = "dragon_inn_tankard", text = "You show the dragon Inn Tankard to Zavistic.")
            showTankard()
        }
        if (inventory.contains("zogre_sithik_portrait_signed")) {
            item(item = "zogre_sithik_portrait_signed", text = "You show the signed portrait of Sithik to Zavistic.")
            showSignedPortrait()
        }
    }

    // ===== Individual evidence reveals =====

    private suspend fun Player.showBlackPrism() {
        item(item = "black_prism", text = "You show the black prism to the aged wizard.")
        if (get("thzfe_prismsearch", 0) >= 4) {
            npc<Neutral>(
                "Yes, you've already showed me that, bring it to me when you've resolved the " +
                    "problems at Jiggig and I'll see what I can do.",
            )
            return
        }
        npc<Neutral>(
            "Hmmm, well this is an uncommon spell component. On it's own it's useless, but " +
                "with certain necromantic spells it can be very powerful. Did you find " +
                "anything else there?",
        )
        if (inventory.contains("dragon_inn_tankard")) {
            item(item = "dragon_inn_tankard", text = "You show the tankard to Zavistic.")
            player<Neutral>("Well, I found this...")
            npc<Neutral>(
                "Hmmm, no, that's not really associated with this to be honest. Did you find " +
                    "anything else there?",
            )
            player<Neutral>("Not really.")
        } else {
            player<Neutral>("Not really.")
        }
        npc<Neutral>(
            "I don't know what to say then, there isn't enough to go on with the clues " +
                "you've shown me so far. I'd suggest going back to search a bit more, but " +
                "you may just be wasting your time?",
        )
        npc<Neutral>(
            "Hmm, but this prism does seem to have some magical protection. Once you've " +
                "finished with this item, bring it back to me would you? I may have a " +
                "reward for you!",
        )
        player<Neutral>("Sure...I mean, I'll try if I remember.")
    }

    private suspend fun Player.showTornPage() {
        item(item = "torn_page", text = "You show the necromantic half page to the aged wizard.")
        npc<Neutral>(
            "Hmm, this is a half torn spell page, it requires another spell component to be " +
                "effective. Did you find anything else there?",
        )
        if (inventory.contains("black_prism")) {
            showBothClues()
        } else if (inventory.contains("dragon_inn_tankard")) {
            item(item = "dragon_inn_tankard", text = "You show the tankard to Zavistic.")
            player<Neutral>("Well, I found this...")
            npc<Neutral>(
                "Hmmm, no, that's not really associated with this to be honest. Did you find " +
                    "anything else there?",
            )
            player<Neutral>("Not really.")
            npc<Neutral>(
                "I don't know what to say then, there isn't enough to go on with the clues " +
                    "you've shown me so far. I'd suggest going back to search a bit more, " +
                    "but you may just be wasting your time?",
            )
        } else {
            player<Neutral>("Not really.")
            npc<Neutral>(
                "I don't know what to say then, there isn't enough to go on with the clues " +
                    "you've shown me so far. I'd suggest going back to search a bit more, " +
                    "but you may just be wasting your time?",
            )
        }
    }

    // The combined clue scene — sets up the Sithik investigation
    private suspend fun Player.showBothClues() {
        items("black_prism", "torn_page", "You show the prism and the necromantic half page to the aged wizard.")
        npc<Happy>("Hmmm, now this is interesting! Where did you get these from?")
        player<Neutral>(
            "I got them from a nearby Ogre tomb, it's recently been infested with zombie " +
                "ogres and I'm trying to work out what happened there.",
        )
        npc<Sad>(
            "This is very troubling $name, very troubling indeed. While it's permitted for " +
                "learned members of our order to research the 'dark arts', it's absolutely " +
                "forbidden to make use of such magic.",
        )
        player<Neutral>("Do you have any leads on people that I might talk to regarding this?")
        set("thzfe_prismsearch", 4)
        npc<Neutral>(
            "Well a wizard by the name of 'Sithik Ints' was doing some research in this area. " +
                "He may know something about it. He's lodged at that guest house to the " +
                "North, though he's ill and isn't able to leave his room.",
        )
        npc<Neutral>(
            "Why not go and talk to him, poke around a bit and see if anything comes up. Let " +
                "me know how you get on. However, I doubt that 'Sithik' had anything to do " +
                "with it.",
        )
        npc<Neutral>(
            "There's a severe penalty for using the 'dark arts'. If you find any evidence to " +
                "the contrary, please bring it to me.",
        )
        npc<Neutral>(
            "Hmm, that black prism seems to have some magical protection. Once you've " +
                "finished with this item, bring it back to me would you. I may have a " +
                "reward for you.",
        )
    }

    private suspend fun Player.showTankard() {
        player<Neutral>("This is the tankard I found on the remains of Brentle Vahn!")
        if (get("thzfe_innkeepermugshown", false)) {
            npc<Neutral>(
                "Yeah, you've shown me this before...if this is all the evidence you have?",
            )
            player<Neutral>("Please just look at it again...")
            npc<Neutral>("Ok, let me look then.")
            item(item = "dragon_inn_tankard", text = "You show the tankard to Zavistic, he looks at it again.")
        }
        set("thzfe_innkeepermugshown", true)
        npc<Neutral>(
            "That doesn't mean anything in itself, you could have gotten that from anywhere. " +
                "Even from the Dragon Inn tavern! There isn't anything to link Brentle Vahn " +
                "with Sithik Ints.",
        )
    }

    private suspend fun Player.showNecromancyBook() {
        player<Neutral>(
            "I have this necromancy book as evidence that Sithik is involved with the undead " +
                "ogres at Jiggig.",
        )
        if (get("thzfe_shownnecrobook", false)) {
            npc<Neutral>(
                "Yeah, you've shown me this before...if this is all the evidence you have?",
            )
            player<Neutral>("Please just look at it again...")
            npc<Neutral>("Ok, let me look then.")
        }
        npc<Neutral>(
            "Ok, so he's researching necromancy...it doesn't mean anything in itself.",
        )
        player<Neutral>(
            "Yes, but if you look, you can see that there is a half torn page which matches " +
                "the page I found at Jiggig.",
        )
        set("thzfe_shownnecrobook", true)
        npc<Neutral>(
            "Hmm, yes, but someone could have stolen that from him and then gone and cast it " +
                "without his permission or to try and deliberately implicate him.",
        )
    }

    private suspend fun Player.showHamBook() {
        player<Neutral>(
            "Look, this book proves that Sithik hates all monsters and most likely Ogres " +
                "with a passion.",
        )
        if (get("thzfe_shownhambook", false)) {
            npc<Neutral>(
                "Yeah, you've shown me this before...if this is all the evidence you have?",
            )
            player<Neutral>("Please just look at it again...")
            npc<Neutral>("Ok, let me look then.")
            item(item = "book_of_ham", text = "You show the HAM book to Zavistic, he looks through it again.")
        }
        set("thzfe_shownhambook", true)
        npc<Neutral>(
            "So what, hating monsters isn't a crime in itself...although I suppose that it " +
                "does give a motive if Sithik was involved. On its own, it's not enough " +
                "evidence though.",
        )
    }

    private suspend fun Player.showSignedPortrait() {
        player<Neutral>(
            "This is a portrait of Sithik, signed by the landlord of the Dragon Inn saying " +
                "that he saw Sithik and Brentle Vahn together.",
        )
        if (get("thzfe_shownsignedportrait", false)) {
            npc<Neutral>(
                "Yeah, you've shown me this before...if this is all the evidence you have?",
            )
            player<Neutral>("Please just look at it again...")
            npc<Neutral>("Ok, let me look then.")
            item(item = "signed_portrait", text = "You show the signed portrait of Sithik again to Zavistic.")
        }
        set("thzfe_shownsignedportrait", true)
        npc<Neutral>("Hmmm, well that is interesting.")
        if (showedAllEvidence()) {
            handOverPotion()
        }
    }

    private fun Player.showedAllEvidence(): Boolean = get("thzfe_shownnecrobook", false) && get("thzfe_shownsignedportrait", false)

    // ===== The big payoff: receive the strange potion =====

    private suspend fun Player.handOverPotion() {
        npc<Neutral>(
            "And I'm starting to think that Sithik may be involved. Here, take this potion " +
                "and give some to Sithik. It'll bring on a change which should solicit some " +
                "answers - tell him the effects won't revert until he's told the truth.",
        )
        zogre_flesh_eaters = 4
        inventory.remove("necromancy_book")
        inventory.remove("torn_page")
        inventory.remove("dragon_inn_tankard")
        inventory.remove("zogre_sithik_portrait_signed")
        inventory.remove("book_of_ham")
        addOrDrop("zogre_ogre_trans_potion")
        item(
            item = "zogre_ogre_trans_potion",
            text = "Zavistic hands you a strange looking potion bottle and takes all the " +
                "evidence you've accumulated so far.",
        )
    }

    private suspend fun Player.notYetUsedPotion() {
        player<Neutral>("No, not yet, what was I supposed to do again?")
        npc<Neutral>(
            "Try to use the potion on Sithik somehow, he should undergo an interesting " +
                "transformation, though you'll probably want to leave the house in case " +
                "there are any side effects. Then go back and question Sithik and tell",
        )
        npc<Neutral>(
            "him the effects won't wear off until he tells the truth. In fact, that's not " +
                "exactly true, but I'm sure it'll be an extra incentive to get him to be " +
                "honest.",
        )
        guildMenu()
    }

    private suspend fun Player.lostPotion() {
        player<Neutral>("Well, actually, I've lost it, could I have another one please?")
        npc<Neutral>("Sure, but don't lose it this time.")
        addOrDrop("zogre_ogre_trans_potion")
        item(item = "zogre_ogre_trans_potion", text = "Zavistic hands you a bottle of strange potion.")
    }

    private suspend fun Player.yesUsedPotion() {
        player<Neutral>("Yes, I have in fact. I poured it into his tea.")
        npc<Neutral>(
            "Ok, that's good, that should work. Pop back in a little while to see Sithik and " +
                "start questioning him.",
        )
        guildMenu()
    }

}
