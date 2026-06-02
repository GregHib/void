package content.area.kandarin.yanille

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
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
import content.quest.member.ogre.zogre_flesh_eaters
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class SithikInts : Script {
    init {
        objectOperate("Talk-to", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            val progress = zogre_flesh_eaters
            when (progress) {
                0, 2 -> sleepyOldManIntro()
                3, 4 -> conversationByVarbit488()
                6 -> postOgreReveal()
                8 -> postQuestProgressed()
                10, 12 -> backToGloat()
                else -> sleepyOldManIntro() // catch-all preservation of other states
            }
            // TODO (case 14): unknown progress 14 branch — preserve original placeholder
        }

        objectOperate("Search", "sithiks_drawers") {
            if (noMoreSnooping()) return@objectOperate
            if (!hasPermission()) {
                snoopWarning()
                return@objectOperate
            }
            searchDrawer()
        }

        objectOperate("Search", "sithiks_cupboard") {
            if (noMoreSnooping()) return@objectOperate
            if (!hasPermission()) {
                snoopWarning()
                return@objectOperate
            }
            searchCupboard()
        }

        objectOperate("Search", "sithiks_wardrobe") {
            if (noMoreSnooping()) return@objectOperate
            if (!hasPermission()) {
                snoopWarning()
                return@objectOperate
            }
            searchWardrobe()
        }

        itemOnObjectOperate("necromancy_book", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>("Aha! A necromantic book! What's this doing here then?")
            item(item = "necromancy_book", text = "You show the Necromantic book to Sithik.")
            npc<Neutral>(
                npcId = sithik,
                text = "Oh..I'm not quite sure actually...where did you find that then?",
            )
            player<Neutral>(
                "I found it in this cupboard! What do you have to say for yourself?",
            )
            npc<Neutral>(
                npcId = sithik,
                text = "Oh yes, that's right...I remember now. It's for my research, there's nothing " +
                    "really dangerous about it, unless it falls into the wrong hands. I'm " +
                    "sure it's pretty safe with me.",
            )
            player<Neutral>("Hmmm, likely story!")
        }

        // ===== HAM book =====
        itemOnObjectOperate("book_of_ham", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>("What's this then?")
            item(item = "book_of_ham", text = "You show the HAM book to Sithik.")
            npc<Neutral>(
                npcId = sithik,
                text = "What do you mean? It's a book by the respected HAM leader Johanhus " +
                    "Ulsbrecht, that man speaks for a lot of people who are unhappy with the " +
                    "current state of affairs.",
            )
            npc<Angry>(
                npcId = sithik,
                text = "Can you honestly tell me that you've not had to fight for your life against " +
                    "the odd monster or two?",
            )
            player<Neutral>(
                "Hmm, that may be true, but I don't universally hate all monsters, whereas I " +
                    "have a sneaking suspicion that you do...and ogres in particular!",
            )
            npc<Angry>(
                npcId = sithik,
                text = "Hmm, that's an interesting theory, care to back it up with any facts?",
            )
        }

        // ===== Papyrus (sketching Sithik) =====
        itemOnObjectOperate("papyrus", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            if (zogre_flesh_eaters >= 6) {
                message("You have already created Sithik's portrait, you don't need another one.")
                return@itemOnObjectOperate
            }
            if (!inventory.contains("charcoal")) {
                statement("You have no charcoal with which to sketch this subject.")
                return@itemOnObjectOperate
            }
            npc<Happy>(npcId = sithik, text = "Oh lovely! You're making my portrait! Let me see it afterwards!")
            statement("You begin sketching the irritable Sithik.")
            anim("human_mapping")
            sound("zogre_writing")
            delay(2)
            inventory.remove("papyrus")
            val portrait = if ((0 until 3).random() == 0) "zogre_sithik_portrait_good" else "zogre_sithik_portrait_bad"
            inventory.add(portrait)
            item(item = portrait, text = "You get a portrait of Sithik.")
        }

        // ===== Book of portraiture =====
        itemOnObjectOperate("book_of_portraiture", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>("Oh, so explain this then?")
            item(item = "book_of_portraiture", text = "You show the book on portraiture to Sithik.")
            npc<Neutral>(
                npcId = sithik,
                text =
                "It's my hobby...I'm interested in portraiture, but all art in general. It's " +
                    "fun, you should try it.",
            )
            player<Neutral>("How do I do it...")
            npc<Neutral>(npcId = sithik, text = "Well...you could start by reading the book!")
        }

        // ===== Bad portrait =====
        itemOnObjectOperate("zogre_sithik_portrait_bad", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>("Here you go, what do you think?")
            item(item = "zogre_sithik_portrait_bad", text = "You show the sketch...")
            npc<Happy>(
                npcId = sithik,
                text = "Hmmm, well it's an interesting interpretation, but not really classic " +
                    "realist representation is it? It's not my favourite, but I like the " +
                    "'truth' of the work...well done.",
            )
        }

        // ===== Good portrait =====
        itemOnObjectOperate("zogre_sithik_portrait_good", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>("Here you go, what do you think?")
            item(item = "zogre_sithik_portrait_good", text = "You show the portrait to Sithik.")
            npc<Happy>(
                npcId = sithik,
                text = "Hmmm, well it's not the most flattering of portraits, but I like the " +
                    "'honesty' of the work...well done.",
            )
        }

        // ===== Strange potion =====
        itemOnObjectOperate("zogre_ogre_trans_potion", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>(
                "Here, try some of this potion, it'll make you feel better!",
            )
            npc<Shock>(
                npcId = sithik,
                text =
                "Err, yuck....no way am I taking any potions or medication off you...I don't " +
                    "trust you!",
            )
        }

        // ===== Signed portrait (the bribe scene) =====
        itemOnObjectOperate("signed_portrait", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>(
                "Hey, what do you think of this? I'm going to show it to Zavistic and you're " +
                    "going to be in trouble!",
            )
            item(item = "signed_portrait", text = "You show the portrait to Sithik.")
            npc<Shifty>(
                npcId = sithik,
                text =
                "Hmmm, well, I've got quite a common looking face, I'm often mistaken for " +
                    "other wizards, you know, when I'm wearing my wizard's hat, robes and " +
                    "staff. There's a lot of us around here you know.",
            )
            player<Neutral>(
                "I don't think so! This is a signed picture of you, someone recognised you, " +
                    "you're in deep trouble!",
            )
            npc<Shifty>(
                npcId = sithik,
                text =
                "Ok, I'll pay you to keep this secret - how much do you want for the picture?",
            )
            player<Angry>("You can't buy me Sithik!")
            npc<Shifty>(
                npcId = sithik,
                text =
                "Ok, let's say two million...two million to keep quiet and give me the picture.",
            )
            items("coins", "coins", "Sithik shows you a chest brimming over with coins...")
            player<Neutral>("Oh...erm...well, that is a lot of money actually...er....")
            npc<Shifty>(
                npcId = sithik,
                text =
                "Yes, and you deserve it, you're very clever! Now, take the money...",
            )
            bribeChoice()
        }

        // ===== Dragon Inn tankard =====
        itemOnObjectOperate("dragon_inn_tankard", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>("What about this then? Guess where I found this?")
            item(item = "dragon_inn_tankard", text = "You show the tankard to Sithik.")
            npc<Angry>(
                npcId = sithik,
                text =
                "You probably found it at the local brewhouse! It doesn't take a genius to " +
                    "figure that one out.",
            )
            player<Happy>(
                "Aha! But I found this in an old ogre tomb! I suspect it's a clue which will " +
                    "lead me to the suspect.",
            )
            npc<Shifty>(
                npcId = sithik,
                text =
                "Hmmm, well that eliminates all the local people who don't actually drink at " +
                    "the 'Dragon Inn'. When do you think you'll start questioning the " +
                    "remaining population of Yanille?",
            )
        }

        // ===== Black prism =====
        itemOnObjectOperate("black_prism", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>("Hey, what's this then, can you explain it?!")
            item(item = "black_prism", text = "You show the black prism to Sithik.")
            npc<Angry>(
                npcId = sithik,
                text =
                "Err..it looks sort of familiar, did you steal it from me? Come to think of " +
                    "it, you have the appearance of a common thief!",
            )
            player<Neutral>(
                "I found it in a place called Jiggig where some undead ogres happen to be " +
                    "wandering around.",
            )
            npc<Shifty>(
                npcId = sithik,
                text =
                "Oh, nothing to do with me then, never seen it in my life before!",
            )
        }

        // ===== Torn page =====
        itemOnObjectOperate("torn_page", "zogre_sithik_bed_entity,ogre_bedman_loc") {
            player<Neutral>("Have you ever seen anything like this before?")
            item(item = "torn_page", text = "You show the torn page to Sithik.")
            npc<Angry>(
                npcId = sithik,
                text =
                "It's probably a piece of rubbish someone threw away...what does it say, I " +
                    "can't read it?",
            )
            player<Neutral>(
                "You should be able to read it, it's been torn from a book on necromancy and " +
                    "you're meant to be a specialist in the subject.",
            )
            npc<Shifty>(
                npcId = sithik,
                text =
                "Oh, no..., not really a specialist, just a hobby of mine really. Hardly know " +
                    "anything about it, but it does seem interesting...",
            )
        }
    }

    // ===== Progress 0/2: Initial encounter =====

    private suspend fun Player.sleepyOldManIntro() {
        npc<Neutral>(
            npcId = sithik,
            text =
            "Hey...who gave you permission to come in here! Get out, get out I say.",
        )
        player<Neutral>("Alright, alright...keep your night cap on.")
    }

    // ===== Progress 3/4: Branches by varbit 488 =====

    private suspend fun Player.conversationByVarbit488() {
        when (get("thzfe_prismsearch", 0)) {
            4 -> {
                npc<Neutral>(
                    npcId = sithik,
                    text =
                    "Hey...who gave you permission to come in here!",
                )
                zavisticIntro()
            }
            5 -> {
                npc<Neutral>(npcId = sithik, text = "What do you want now?")
                noNeedToBeRude()
            }
            else -> sleepyOldManIntro()
        }
    }

    private suspend fun Player.zavisticIntro() {
        player<Neutral>(
            "Zavistic Rarve said that I could come and talk to you and ask you a few questions.",
        )
        npc<Confused>(
            npcId = sithik,
            text =
            "Oh, Zavistic...why...why would he send you to me?",
        )
        sithikQuestionsMenu()
    }

    suspend fun Player.sithikQuestionsMenu() {
        choice {
            askAboutUndeadOgres()
            askWhatYouDo()
            mindIfILookAround()
            okThanks()
        }
    }

    fun ChoiceOption.askAboutUndeadOgres(): Unit = option<Neutral>(
        "Do you know anything about the undead ogres at Jiggig?",
    ) {
        npc<Neutral>(
            npcId = sithik,
            text =
            "Er...undead ogres...no, sorry, no idea what you're talking about there.",
        )
        player<Neutral>("Hmm, is that right...")
        npc<Neutral>(npcId = sithik, text = "Well, yes, yes it is. If I knew something, I'd tell you.")
        npc<Happy>(
            npcId = sithik,
            text =
            "Anyway, dead ogres you say? How strange? That must be a strange sight?",
        )
        player<Neutral>(
            "Very well, if you don't know anything about it, you won't mind if I look around then?",
        )
        provokedLookAround()
    }

    fun ChoiceOption.askWhatYouDo(): Unit = option<Neutral>("What do you do?") {
        npc<Neutral>(
            npcId = sithik,
            text =
            "I'm a scholarly student of the magical arts. When I was younger I used to be an " +
                "adventurer, probably just like yourself. But I lost interest in the constant " +
                "fighting, looting and gaining abilities.",
        )
        npc<Neutral>(
            npcId = sithik,
            text =
            "Instead I decided to focus my attention and time to study the purer form of the " +
                "lost arts.",
        )
        player<Neutral>("The lost arts? What are they?")
        npc<Neutral>(
            npcId = sithik,
            text =
            "Ignorant people call them the 'dark arts'. I'm talking about Necromancy, the " +
                "power to bring the dead back to life - the power of the gods! Surely the " +
                "most awesome power known to man.",
        )
        player<Neutral>(
            "Hmm, well I guess I must be an ignorant person then, because bringing the dead " +
                "back to life sounds very unnatural.",
        )
        sithikQuestionsMenu()
    }

    fun ChoiceOption.mindIfILookAround(): Unit = option<Neutral>("Do you mind if I look around?") {
        if (get("thzfe_prismsearch", 0) == 5) {
            triedAlready()
        } else {
            provokedLookAround()
        }
    }

    fun ChoiceOption.okThanks(): Unit = option<Neutral>("Ok, thanks.")

    private suspend fun Player.provokedLookAround() {
        set("thzfe_prismsearch", 5)
        npc<Angry>(
            npcId = sithik,
            text = "Well, err....well, actually yes I do mind...it's my place and I don't want " +
                "strangers going through my things.",
        )
        player<Neutral>(
            "Well, I'm going to have a look around anyway, if you're not involved in this " +
                "whole thing, you won't have anything to hide.",
        )
        npc<Angry>(
            npcId = sithik,
            text = "Why, if I was a few years younger I'd give you a good hiding!"
        )
        player<Neutral>("I'm sure!")
        sithikQuestionsMenu()
    }

    private suspend fun Player.triedAlready() {
        npc<Angry>(
            npcId = sithik,
            text = "I've already told you that I do! But you'll probably just ignore me again!"
        )
        player<Neutral>("Quite right!")
    }

    // ===== "Snooping" reaction (varbit 488 == 5) =====

    private suspend fun Player.noNeedToBeRude() {
        player<Neutral>("Hey there's no need to be rude!")
        npc<Neutral>(
            npcId = sithik,
            text = "What do you expect when you just go snooping around a person's place against " +
                "their express permission.",
        )
        snoopMenu()
    }

    suspend fun Player.snoopMenu() {
        choice {
            askWhatYouDo()
            whyInBed()
            okThanks()
        }
    }

    fun ChoiceOption.whyInBed(): Unit = option<Neutral>("Why do you spend most of your time in bed?") {
        npc<Neutral>(
            npcId = sithik,
            text = "I'm actually quite old and not so very well and I'd like to get over this " +
                "illness I have, then I'll return to my very serious and important studies.",
        )
        sithikQuestionsMenu()
    }

    // ===== Progress 6: Player turned Sithik into an ogre =====

    private suspend fun Player.postOgreReveal() {
        if (get("thzfe_sithik_transformed", 0) >= 1) {
            ogreFormConfession()
        } else {
            npc<Neutral>(
                npcId = sithik,
                text = "What do you want now?"
            )
            noNeedToBeRude()
        }
    }

    private suspend fun Player.ogreFormConfession() {
        npc<Neutral>(
            npcId = sithik,
            text = "Arghhhh..what's happened to me...you beast!"
        )
        player<Neutral>(
            "It's your own fault, you shouldn't have lied about your involvement with the " +
                "undead Ogres at Jiggig. The potion will wear off once you've told the truth!",
        )
        npc<Neutral>(
            npcId = sithik,
            text = "Ok, ok, I admit it, I got Brentle Vahn to cast the spell to put an end to those " +
                "awful Ogres...they're just disgusting creatures...",
        )
        player<Neutral>("Ok, that's a start...now I want some answers.")
        confessionAnswersMenu()
    }

    suspend fun Player.confessionAnswersMenu() {
        choice {
            removeSpellFromArea()
            getRidOfOgres()
            getRidOfDisease()
            sorryHaveToGo()
        }
    }

    fun ChoiceOption.removeSpellFromArea(): Unit = option(
        "How do I remove the effects of the spell from the area?",
    ) {
        player<Neutral>(
            "How do I remove the effects of the spell from the area? The ogres want to get " +
                    "their ceremonial dance area back and can't do that with undead walking all " +
                    "over it.",
        )
        if (zogre_flesh_eaters >= 8) {
            npc<Neutral>(
                npcId = sithik,
                text = "Haven't I told you this already? You can't remove the spell, it's permanent, it will last forever, the only option you have is to move the ceremonial area.",
            )
        } else {
            npc<Neutral>(
                npcId = sithik,
                text = "Unfortunately you can't. The spell is permanent, it will last forever, the only " +
                        "option you have is to move the ceremonial area.",
            )
            zogre_flesh_eaters = 8
        }
        player<Neutral>(
            "You're an evil man and I'm going to make you pay for this...you can stay like " +
                "that forever as far as I'm concerned.",
        )
        npc<Sad>(
            npcId = sithik,
            text = "No...no, let me try to make amends...please I can help you. Just don't leave me " +
                "like this.",
        )
        confessionAnswersMenu()
    }

    fun ChoiceOption.getRidOfOgres(): Unit = option<Neutral>("How do I get rid of the undead ogres?") {
        if (get("thzfe_makebrutalarrow", false)) {
            npc<Shock>(
                npcId = sithik,
                text = "Haven't I already explained this to you once before?"
            )
            player<Neutral>("Humour me!")
            explainBrutalArrows()
        } else {
            explainBrutalArrows()
        }
    }

    private suspend fun Player.explainBrutalArrows() {
        npc<Happy>(
            npcId = sithik,
            text = "Ok, similar spells have been cast before and the only way to deal with the " +
                "resulting creatures is to cordon off the area and not go in there again.",
        )
        npc<Neutral>(
            npcId = sithik,
            text = "The undead creatures usually manifest some sort of disease so it's best to " +
                "attack them from a distance with a ranged weapon.",
        )
        npc<Sad>(
            npcId = sithik,
            text = "Normal missiles like arrows and darts do very little damage to them because " +
                "they're designed to destroy internal organs. This is a waste of time with " +
                "undead creatures like undead ogres.",
        )
        player<Neutral>("Yeah, clearly so what should we use?")
        set("thzfe_makebrutalarrow", true)
        npc<Sad>(
            npcId = sithik,
            text = "From my research it looks like a flat ended arrow was designed called a 'Brutal " +
                "arrow'. This does large amounts of crushing damage to the creature. You can " +
                "make them by using larger arrows. ",
        )
        npc<Neutral>(
            npcId = sithik,
            text = "I think some Ogre hunters make them. But instead of adding an arrow tip, you " +
                "hammer a large nail into the end of the shaft.",
        )
        confessionAnswersMenu()
    }

    fun ChoiceOption.getRidOfDisease(): Unit = option<Neutral>("How do I get rid of the disease?") {
        if (get("thzfe_makecuredisease", false)) {
            npc<Shock>(
                npcId = sithik,
                text = "Haven't I already explained this disease thing to you once before?"
            )
            val threat = if (get("thzfe_sithik_transformed", 0) == 2) {
                "Just tell me again or else I'll turn you back into an ogre!"
            } else {
                "Just tell me again or else I'll never turn you back into a human!"
            }
            player<Neutral>(threat)
            npc<Shock>(
                npcId = sithik,
                text = "No...noo...please, I'll tell you."
            )
            explainDiseaseCure()
        } else {
            explainDiseaseCure()
        }
    }

    private suspend fun Player.explainDiseaseCure() {
        set("thzfe_makecuredisease", true)
        npc<Sad>(
            npcId = sithik,
            text = "My research shows that two jungle based herbs can be used, one is found near " +
                "river tributaries and looks like a vine, the other is found in caves and " +
                "grows on the wall.",
        )
        npc<Neutral>(
            npcId = sithik,
            text = "It's quite well camouflaged so it's unlikely that you'll find it."
        )
        player<Neutral>("We'll see about that!")
        confessionAnswersMenu()
    }

    fun ChoiceOption.sorryHaveToGo(): Unit = option<Neutral>("Sorry, I have to go.") {
        npc<Shock>(
            npcId = sithik,
            text = "But...you can't just leave me here like this!"
        )
    }

    // ===== Progress 8: Returns post-confession =====

    private suspend fun Player.postQuestProgressed() {
        npc<Shock>(
            npcId = sithik,
            text = "Arghhhh..what do you want now...you've turned me into a beast!"
        )
        player<Neutral>(
            "I've got some questions for you...and you'd better answer them well or else!",
        )
        npc<Shock>(
            npcId = sithik,
            text = "Ok, ok, I'll tell you anything, just turn me back into a human again!"
        )
        confessionAnswersMenu()
    }

    // ===== Progress 10/12: Post-quest gloating =====

    private suspend fun Player.backToGloat() {
        npc<Neutral>(
            npcId = sithik,
            text = "Oh, so you're back then, come to gloat have you?"
        )
        player<Neutral>("Nope, I've just come to ask you a couple of questions.")
        choice {
            getRidOfOgres()
            getRidOfDisease()
            sorryHaveToGo()
        }
    }

    /**
     * Returns true if the quest is past the snooping investigation phase.
     * Once you're at progress 4+, the furniture has nothing of significance.
     */
    private fun Player.noMoreSnooping(): Boolean {
        if (zogre_flesh_eaters >= 4) {
            message("You search but find nothing of significance.")
            return true
        }
        return false
    }

    /**
     * Permission to snoop is granted by varbit 488 reaching 5,
     * which happens during Sithik's "Do you mind if I look around?" exchange
     * after he's caught lying about the undead ogres.
     */
    private fun Player.hasPermission(): Boolean = get("thzfe_prismsearch", 0) >= 5

    private suspend fun Player.snoopWarning() {
        npc<Angry>(
            npcId = sithik,
            text = "Hey! What do you think you're doing?",
        )
        player<Neutral>(
            "Erk! I'd better not start rifling through peoples things without permission.",
        )
    }

    // ===== Drawer (object 6875): papyrus, charcoal, book of portraiture =====

    private suspend fun Player.searchDrawer() {
        val hasPapyrus = inventory.contains("papyrus") // 970
        val hasCharcoal = inventory.contains("charcoal") // 973
        val ownsBook = inventory.contains("book_of_portraiture") // already has the book somewhere

        // Determine how much inventory space we need to take everything remaining
        val spaceNeeded = when {
            hasPapyrus && hasCharcoal -> if (ownsBook) 0 else 1
            hasPapyrus || hasCharcoal -> if (ownsBook) 1 else 2
            else -> if (ownsBook) 2 else 3
        }

        if (spaceNeeded > 0 && inventory.spaces < spaceNeeded) {
            statement(
                "You see some items in the drawer, but you need $spaceNeeded free inventory " +
                    "spaces to take them.",
            )
            return
        }

        when {
            // All items already collected
            ownsBook && hasPapyrus && hasCharcoal -> {
                message("You find nothing in the drawers.")
            }
            // Has both papyrus and charcoal — only book left
            hasPapyrus && hasCharcoal -> {
                addOrDrop("book_of_portraiture")
                item(item = "book_of_portraiture", text = "You find a book on portraiture.")
            }
            // Has papyrus only — find charcoal (and maybe book)
            hasPapyrus -> {
                addOrDrop("charcoal")
                item(item = "charcoal", text = "You find some charcoal.")
                if (!ownsBook) findBookFollowup()
            }
            // Has charcoal only — find papyrus (and maybe book)
            hasCharcoal -> {
                addOrDrop("papyrus")
                item(item = "papyrus", text = "You find some papyrus.")
                if (!ownsBook) findBookFollowup()
            }
            // Has neither — find both at once (and maybe book)
            else -> {
                addOrDrop("charcoal")
                addOrDrop("papyrus")
                items("charcoal", "papyrus", "You find some charcoal and papyrus.")
                if (!ownsBook) findBookFollowup()
            }
        }
    }

    private suspend fun Player.findBookFollowup() {
        addOrDrop("book_of_portraiture")
        item(item = "book_of_portraiture", text = "You also find a book on portraiture.")
    }

    // ===== Cupboard (object 6876): necromancy book =====

    private suspend fun Player.searchCupboard() {
        if (inventory.contains("necromancy_book")) {
            statement("You search the cupboard but find nothing.")
            return
        }
        addOrDrop("necromancy_book")
        item(item = "necromancy_book", text = "You find a book on Necromancy.")
    }

    // ===== Wardrobe (object 55412): book of HAM =====

    private suspend fun Player.searchWardrobe() {
        if (inventory.contains("book_of_ham")) {
            statement("You search the wardrobe but find nothing.")
            return
        }
        addOrDrop("book_of_ham")
        item(
            item = "book_of_ham",
            text = "You find a book on Philosophy written by the 'Human's Against Monsters' " +
                "leader, Johanhus Albrect.",
        )
    }

    suspend fun Player.bribeChoice() {
        choice("Be bribed by Sithik for 2 million?") {
            refuseBribe()
            acceptBribe()
        }
    }

    fun ChoiceOption.refuseBribe(): Unit = option<Neutral>(
        "No, I won't take the money, I'm going to bring you to justice!",
    ) {
        npc<Angry>(
            npcId = sithik,
            text = "Oh well, suit yourself! I wasn't going to give you the money anyway! No one " +
                "will believe some crazy adventurer and an Inn keep.",
        )
    }

    fun ChoiceOption.acceptBribe(): Unit = option<Neutral>(
        "Ok, I'll shut up for two million!",
    ) {
        npc<Happy>(
            npcId = sithik,
            text = "Ha! Ha! You believed me! I'm not going to give you all my money! No one will " +
                "believe a crazy adventurer and a local Inn keep!",
        )
        player<Neutral>(
            "You're a mean and cruel man Sithik, a mean and cruel man!",
        )
    }

    // Picks the right chathead id depending on whether the player has transformed Sithik into an ogre.
    private val Player.sithik: String
        get() = if (get("thzfe_sithik_transformed", 0) >= 1) "zogre_sithik_ogre" else "zogre_sithik_man"
}
