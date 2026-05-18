package content.area.misthalin.varrock.palace

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.member.misc.pipProgress
import content.quest.member.misc.rollMonuments
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

class KingRoald : Script {
    init {
        npcOperate("Talk-to", "king_roald") {
            // Defender of Varrock zombie palace region
            if (tile.region.id == 7511) {
                npc<Angry>(
                    "I'm rather busy right now. In case you hadn't noticed, there are zombies " +
                            "all over the palace."
                )
                zombiePalaceChoice()
                return@npcOperate
            }

            // What Lies Below — Surok letter delivery
            val whatLiesBelow = get("what_lies_below", 0)
            if (whatLiesBelow == 60 || whatLiesBelow == 61) {
                surokOrSomethingElseChoice()
                return@npcOperate
            }

            // Default greeting
            player<Happy>("Greetings, your majesty.")
            routeByItems()
        }
    }

    private suspend fun Player.routeByItems() {
        when {
            inventory.contains("intact_shield_arrav") || inventory.contains("broken_shield_arrav") -> {
                shieldOfArravRecovered()
            }
            inventory.contains("certificate_full") -> {
                claimReward("certificate_full")
            }
            inventory.contains("certificate_phoenix") -> {
                claimReward("certificate_phoenix")
            }
            inventory.contains("certificate_black_arm") -> {
                claimReward("certificate_black_arm")
            }
            else -> {
                priestInPerilGreeting()
            }
        }
    }

    private suspend fun Player.priestInPerilGreeting() {
        npc<Quiz>("Well hello there. What do you want?")
        when (pipProgress) {
            0 -> startQuestRequest()
            1 -> alreadyAcceptedQuest()
            3 -> killedGuardDog()
            4 -> shoutBorderSecure()
            5 -> drezelImprisonedReport()
            6 -> drezelVampireReport()
            in 7..59 -> {
                borderSecuredCheck()
            }
            else -> postQuestSmalltalk()
        }
    }

    private suspend fun Player.startQuestRequest() {
        player<Neutral>("I am looking for a quest!")
        npc<Neutral>(
            "A quest you say? Hmm, what an odd request to make of the king. It's funny you " +
                    "should mention it though, as there is something you can do for me."
        )
        npc<Confused>(
            "Are you aware of the temple east of here? It stands on the river Salve and guards " +
                    "the entrance to the lands of Morytania?"
        )
        player<Confused>("No, I don't think I know it...")
        npc<Neutral>(
            "Hmm, how strange that you don't. Well anyway, it has been some days since last I " +
                    "heard from Drezel, the priest who lives there."
        )
        npc<Neutral>(
            "Be a sport and make sure that nothing untoward has happened to the silly old " +
                    "codger for me, would you?"
        )
        questAcceptChoice()
    }

    suspend fun Player.questAcceptChoice() {
        choice {
            sureAccept()
            soundsBoring()
        }
    }

    fun ChoiceOption.sureAccept(): Unit = option("Sure.") {
        rollMonuments()
        pipProgress = 1
        player<Neutral>("Sure, I don't have anything better to do right now.")
        npc<Happy>(
            "Many thanks adventurer! I would have sent one of my squires but they wanted " +
                    "payment for it!"
        )
    }

    fun ChoiceOption.soundsBoring(): Unit = option<Neutral>("No, that sounds boring.") {
        npc<Neutral>(
            "Yes, I dare say it does. I wouldn't even have mentioned it had you not seemed to " +
                    "be looking for something to do anyway."
        )
    }

    private suspend fun Player.alreadyAcceptedQuest() {
        npc<Neutral>("You have news of Drezel for me?")
        questQuestionsChoice()
    }

    suspend fun Player.questQuestionsChoice() {
        choice {
            whosDrezel()
            whereGoAgain()
            whyCareAboutDrezel()
            doIGetReward()
        }
    }

    fun ChoiceOption.whosDrezel(): Unit = option<Quiz>("Who's Drezel?") {
        npc<Confused>(
            "Drezel is the priest who lives in the Temple to the east of here. You're supposed " +
                    "to go make sure nothing's happened to him. Remember?"
        )
        player<Neutral>("Oooooooooh, THAT Drezel. Yup, I'll go do that then.")
    }

    fun ChoiceOption.whereGoAgain(): Unit = option<Quiz>("Where am I supposed to go again?") {
        npc<Neutral>(
            "The temple where Drezel lives is but a short journey east from here. It lies " +
                    "south of the cliffs, on the mouth of the river Salve."
        )
        npc<Neutral>("Don't worry, you can't miss it.")
    }

    fun ChoiceOption.whyCareAboutDrezel(): Unit = option<Quiz>("Why do you care about Drezel anyway?") {
        npc<Neutral>(
            "Well, that is a slightly impertinent question to ask of your King, but I shall " +
                    "overlook it this time."
        )
        npc<Neutral>(
            "As you are no doubt aware, this kingdom worships Saradomin, and is a peaceful " +
                    "place to live and prosper. The temple where Drezel lives stands"
        )
        npc<Neutral>(
            "on the Eastern border of Misthalin, and further East lie the evil lands of " +
                    "Morytania, a fearful land of undead monsters and Zamorakians."
        )
        npc<Neutral>(
            "The sacred river Salve marks a natural border between our kingdoms, and the " +
                    "temple prevents any invasions to this land from Morytania."
        )
        npc<Neutral>(
            "By keeping the water of the river blessed, our defences remain strong, as the " +
                    "fiends that inhabit Morytania cannot cross such a holy barrier."
        )
        npc<Neutral>(
            "Drezel is the descendant of one of the original Saradominist priests who first " +
                    "blessed the river, and built the temple there."
        )
        npc<Neutral>(
            "His job is to ensure nothing happens to the river at the source that might allow " +
                    "the evil Morytanians to invade this land. This is the reason"
        )
        npc<Neutral>(
            "why the lack of communication from him bothers me somewhat, although I am sure " +
                    "nobody would dare to try and attack our kingdom!"
        )
    }

    fun ChoiceOption.doIGetReward(): Unit = option<Quiz>("Do I get a reward for this?") {
        npc<Neutral>(
            "You will be rewarded in the knowledge that you have done the right thing and " +
                    "assisted the King of Misthalin."
        )
        player<Angry>("Soooooo...... that would be a 'no' then?")
        npc<Neutral>("That is correct.")
    }

    private suspend fun Player.killedGuardDog() {
        npc<Neutral>("You have news of Drezel for me?")
        player<Happy>(
            "Yeah, I spoke to the guys at the temple and they said they were being bothered by " +
                    "that dog in the crypt, so I went and killed it for them. No problem."
        )
        npc<Shock>("YOU DID WHAT???")
        npc<Angry>(
            "Are you mentally deficient??? That guard dog was protecting the route to " +
                    "Morytania! Without it we could be in severe peril of attack!"
        )
        player<Sad>("Did I make a mistake?")
        npc<Angry>(
            "YES YOU DID!!!!! You need to get there right now and find out what is happening! " +
                    "Before it is too late for us all!"
        )
        player<Sad>("B-but Drezel TOLD me to...!")
        npc<Angry>(
            "No, you absolute cretin! Obviously some fiend has done something to Drezel and " +
                    "tricked your feeble intellect into helping them kill that guard dog!"
        )
        npc<Angry>(
            "You get back there and do whatever is necessary to safeguard my kingdom from " +
                    "attack, or I will see you beheaded for high treason!"
        )
        pipProgress = 4
        player<Sad>("Y-yes your Highness.")
    }

    private suspend fun Player.shoutBorderSecure() {
        npc<Angry>(
            "AND MORE IMPORTANTLY, WHY HAVEN'T YOU ENSURED THE BORDER TO MORYTANIA IS SECURE YET?"
        )
        player<Sad>("Okay, okay, I'm going, I'm going... There's no need to shout...")
        npc<Angry>("NO NEED TO SHOUT???")
        npc<Neutral>(
            "Listen, and listen well, and see if your puny mind can comprehend this: if the " +
                    "border is not protected, then we are at the mercy of the evil beings"
        )
        npc<Neutral>(
            "that live in Morytania. Given that most of the inhabitants consider humans to be " +
                    "nothing more than over talkative snack food, I would"
        )
        npc<Angry>(
            "say that me shouting at you for your incompetence is the LEAST of your worries " +
                    "right now. NOW GO!"
        )
    }

    private suspend fun Player.drezelImprisonedReport() {
        npc<Neutral>("You have news of Drezel for me?")
        player<Neutral>("I do indeed, sire. He has been imprisoned by some Zamorakian monks.")
        npc<Angry>(
            "What? This is wholly unacceptable! I order you to do all that you can to free " +
                    "Drezel immediately!"
        )
        player<Neutral>("I was doing that anyway.")
        npc<Neutral>("Ah, I see. In that case keep up the good work.")
    }

    private suspend fun Player.drezelVampireReport() {
        npc<Neutral>("Have you freed Drezel yet?")
        player<Neutral>(
            "Well, I found the key to his cell and unlocked it, but there's a vampire in there " +
                    "stopping him leaving."
        )
        npc<Neutral>("A vampire eh? Nasty pieces of work. Well, I order you to do something about it at once!")
        player<Confused>("Yeah, I was planning on doing that anyway.")
        npc<Neutral>("Good work! Always a place for quick thinkers in my kingdom!")
    }

    private suspend fun Player.borderSecuredCheck() {
        npc<Neutral>("Have you ensured Misthalin's border is fully secured to the East?")
        player<Neutral>("Not yet. I'm working on it though.")
        npc<Neutral>("Good, good.")
    }

    private suspend fun Player.postQuestSmalltalk() {
        npc<Neutral>("Ah, it's you again. Hello there.")
        npc<Confused>("Do you have anything of importance to say?")
        player<Sad>("...Not really.")
        npc<Neutral>(
            "You will have to excuse me, then. I am very busy as I have a kingdom to run!"
        )
    }

    private suspend fun Player.shieldOfArravRecovered() {
        player<Happy>(
            "Your majesty, I have recovered the Shield Of Arrav; I would like to claim the reward."
        )
        npc<Quiz>(
            "The Shield of Arrav, eh? Yes, I do recall my father, King Roald, put a reward " +
                    "out for that."
        )
        npc<Neutral>("Very well.")
        npc<Neutral>(
            "If you get the authenticity of the shield verified by the curator at the museum " +
                    "and then return here with authentication, I will grant you your reward."
        )
    }

    private suspend fun Player.claimReward(certificate: String) {
        player<Happy>(
            "Your majesty, I have come to claim the reward for the return of the Shield Of Arrav."
        )
        item("certificate", "You show the certificate to the king.")
        if (certificate == "certificate_full") {
            npc<Happy>(
                "My goodness! This claim is for the reward offered by my father many years ago!"
            )
            npc<Happy>(
                "I never thought I would live to see the day when someone came forward to claim " +
                        "this reward!"
            )
            npc<Happy>(
                "I heard that you found half the shield, so I will give you half of the bounty. " +
                        "That comes to exactly 600gp!"
            )
            item("coins", "You hand over a certificate. The king gives you 600gp.") // TODO check amount
            // grantShieldOfArravReward(certificate) TODO
        } else {
            npc<Sad>(
                "I'm afraid that's only half the reward certificate. You'll have to get the " +
                        "other half and join them together if you want to claim the reward."
            )
        }
    }

    suspend fun Player.surokOrSomethingElseChoice() {
        choice {
            talkAboutSurok()
            talkAboutSomethingElse()
        }
    }

    fun ChoiceOption.talkAboutSurok(): Unit = option("Talk about Surok.") {
        player<Neutral>("Your majesty, I think that you should see this letter.")
        npc<Neutral>(
            "Letter? Let me see. Where's Postie Pete? He usually deals with the mail around here."
        )
        player<Neutral>(
            "This letter was delivered to me by hand, your majesty. I think you may be in some " +
                    "danger."
        )
        npc<Neutral>(
            "Hmmm. I see. I appreciate your concern. However, I assure you, I am quite safe " +
                    "here. My guards are on full alert at all times."
        )
        player<Neutral>("I don't think you understand...")
        npc<Neutral>(
            "I understand perfectly. Now, for all I know, that letter could be a fake ruse. " +
                    "We get a lot of that sort of thing and it causes a lot of hassle."
        )
        player<Angry>("I'm trying to save your life!")
        npc<Angry>(
            "And you are shouting at your king! People who yell at their monarchs often find " +
                    "their heads become...loose!"
        )
        player<Neutral>("I'm just saying that the letter is real.")
        npc<Quiz>("Then why do you have it?")
        player<Sad>("Because I'm supposed to deliv...oh.")
        npc<Neutral>(
            "It seems to me that if that letter were genuine, I would be forced to arrest its " +
                    "bearer for treason. You understand."
        )
        npc<Quiz>("So what do you say?")
        player<Sad>("I...er...I reckon it's a fake! ...I guess...")
        npc<Happy>("Just as I thought! Now off you go!")
    }

    fun ChoiceOption.talkAboutSomethingElse(): Unit = option<Neutral>("Talk about something else.") {
        player<Neutral>("Greetings, your majesty.")
        routeByItems()
    }

    suspend fun Player.zombiePalaceChoice() {
        choice {
            talkAboutZombies()
            talkAboutPriestInPeril()
            talkAboutBeacons()
            moreOptions()
        }
    }

    fun ChoiceOption.talkAboutZombies(): Unit = option("Talk about Zombies.") {
        val defenderProgress = get("defender_of_varrock", 0)
        if (defenderProgress in 220..225) {
            player<Neutral>(
                "So I can see. We might have a solution with this shield, though."
            )
            shieldSolution()
        } else {
            player<Neutral>("I'm sorry, Your Majesty. I'm trying to get rid of them.")
            npc<Angry>(
                "Well, help get rid of them, rather than standing there yapping at me."
            )
        }
    }

    fun ChoiceOption.talkAboutPriestInPeril(): Unit = option<Neutral>("Talk about Priest in Peril") {
        // TODO: Priest in Peril branch from zombie palace
    }

    fun ChoiceOption.talkAboutBeacons(): Unit = option<Neutral>("Talk about the beacons.") {
        // TODO: beacons branch
    }

    fun ChoiceOption.moreOptions(): Unit = option<Neutral>("More...") {
        // TODO: more options branch
    }

    private suspend fun Player.shieldSolution() {
        if (inventory.contains("restored_shield_of_arrav")) {
            npc<Confused>(
                "No one has figured out how to unlock the powers of the shield in hundreds of " +
                        "years - are we so desperate that we are relying on legends?"
            )
            player<Happy>(
                "I have a reason to believe that you are descended from an original elder of " +
                        "Varrock."
            )
            npc<Neutral>(
                "That's right. Would you expect anything less of the man who is king?"
            )
            player<Neutral>(
                "No, Your Majesty, but it might mean that you can use the shield to defeat the " +
                        "zombies. Here, hold the shield and see if anything happens."
            )
            npc<Neutral>("Well, I have held the shield before, but I'll give it a go.")
            item(item = "restored_shield_of_arrav", "You pass the shield to King Roald.")
            npc<Neutral>("It doesn't seem to be doing anything special.")
            player<Neutral>(
                "It must be someone else who is descended from the correct elder, then. Ah, " +
                        "well, thank you for your time."
            )
        } else {
            npc<Confused>("What shield?")
            player<Neutral>(
                "Actually, no, I don't have it. How embarrassing. I'll be back in a bit."
            )
        }
    }

    // ===== Helpers =====

    private fun grantShieldOfArravReward(player: Player, certificate: String) {
        // TODO: hook into your engine's quest reward system
        // - Delete the certificate item
        // - Increment the appropriate gang counter (Phoenix vs Black Arm)
        // - Give 600gp
        // - Mark Shield of Arrav as completed
        // - Award quest points
    }
}