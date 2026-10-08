package content.minigame.fist_of_guthix

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class Fiara : Script {

    init {
        npcOperate("Talk-to", "fiara") {
            if (!hasSeenFiaraIntroduction()) {
                firstTimeDialogue()
                setHasSeenFiaraIntroduction()
                if (get("fist_of_guthix_tutorial_complete", false)) {
                    afterTutorialDialogue()
                } else {
                    beforeTutorialMenu()
                }
                return@npcOperate
            }
            if (get("fist_of_guthix_tutorial_complete", false)) {
                afterTutorialDialogue()
            } else {
                statement("As you approach the...big creature thing, it lets out a hiss that sounds partly like a threat and partly like a sigh.")
                beforeTutorialMenu()
            }
        }
    }

    private suspend fun Player.firstTimeDialogue() {
        statement("As you approach the...big creature thing, it lets out a hiss that sounds partly like a threat and partly like a sigh.")
        if (spokenToDruid()) {
            player<Confused>("Erm, excuse me...Lady Fiara, but the druids told me to talk to you.")
            npc<Angry>("*Hiss* Yes...the druids. They say and do far more than they should.")
            player<Quiz>("What's wrong with the druids? They seem to worship you.")
            npc<Angry>("They worship everything that is green or sounds even slightly magical. Not just worship - they stalk. And when they have arrived, it is impossible to get them away. They are like insects.")
            player<Neutral>("You want to get rid of them, eh? I might be able to-")
            npc<Angry>("No, no. *Hiss* Now you're just like them. \"What can we do for you today, oh great Guardian of Guthix, Lady Fiara?\"")
            player<Neutral>("Erm, sorry, I guess.")
            npc<Neutral>("*Hiss* Do not worry your feeble mind with it. What was it you wanted? Let's just get it over with.")
        } else {
            player<Quiz>("Hmm. What was that about?")
            npc<Angry>("*Hiss* Another human. Why do you all keep coming down here?")
        }
    }

    private suspend fun Player.beforeTutorialMenu() = choice("Select an Option") {
        option("What is this place?") {
            askWhatThisPlaceIs()
        }
        option("What are you?") {
            askWhatFiaraIs()
        }
        option<Neutral>("Never mind.") {
        }
    }

    private suspend fun Player.askWhatThisPlaceIs() {
        player<Quiz>("What is this place?")
        npc<Neutral>("It is the site of the Fist of Guthix.")
        player<Quiz>("Fist of Guthix? What is that?")
        npc<Neutral>("It is what it is. If you do not already know, it's probably best that you don't learn.")
        player<Quiz>("Umm, okay. What are you all doing down here then?")
        npc<Angry>("*Hiss* Three druids are mainly just walking around NOT being quiet. Another one is setting up a fortress of crates and dragging all kinds of material junk into the cave. The fifth human, who isn't a druid, showed up when the following masses swarmed the cave.")
        player<Quiz>("I see. But...what drew them here in the first place?")
        npc<Neutral>("The Fist of Guthix. And now they try to harness its power through this little business they've set up.")
        if (spokenToDruid()) {
            player<Quiz>("Okay, but they told me to talk to you. Why did they want that?")
            npc<Neutral>("Because I am the guardian of this site and I need to make sure that their games do not get out of control.")
            player<Quiz>("Alright. Can you tell me about the games, then?")
        } else {
            player<Neutral>("Ooh, that sounds interesting. I am a fan of power, myself.")
            npc<Angry>("I figured you were. After all, you are human.")
            player<Neutral>("I guess I should talk to the druids about that business, then.")
            npc<Neutral>("No, you should not. I am the guardian here. I need to make sure that the rules are obeyed and that their games do not go out of control.")
            player<Quiz>("Okay, you tell me about it, then.")
        }
        setHasCompletedFiaraWhatIsThisPlaceDialogue()
        startTutorial()
        aboutFistOfGuthixMenu()
    }

    private suspend fun Player.askWhatFiaraIs() {
        player<Quiz>("What are you?")
        npc<Neutral>("I am Fiara, Guardian of Guthix. What are you?")
        choice("Select an Option") {
            option("I am $name. What are you guarding?") {
                setFiaraIdentity(IDENTITY_PLAYER)
                player<Quiz>("I am $name. What are you guarding?")
                npc<Neutral>("Hello, $name. I am guarding this very site.")
                player<Quiz>("And what's so special about it?")
                npc<Neutral>("It is the site of the Fist of Guthix. If you do not already know that, you are probably not fit to learn.")
                npc<Neutral>("Is there anything else I can help you with?")
                whatPlaceMenu()
            }
            option("I am $name, a Guardian of Saradomin.") {
                setFiaraIdentity(IDENTITY_SARADOMIN)
                player<Quiz>("I am $name, a Guardian of Saradomin.")
                npc<Angry>("Hello, $name, blind follower of the evil good and keeper of lies. Was there anything else you wanted?")
                whatPlaceMenu()
            }
            option("I am $name, also a Guardian of Guthix.") {
                setFiaraIdentity(IDENTITY_GUTHIX)
                player<Quiz>("I am $name, also a Guardian of Guthix.")
                npc<Angry>("Hello, $name, seer of light but bringer of lies. Was there anything else you wanted?")
                whatPlaceMenu()
            }
            option("I am $name, a Guardian of Zamorak.") {
                setFiaraIdentity(IDENTITY_ZAMORAK)
                player<Quiz>("I am $name, a Guardian of Zamorak.")
                npc<Angry>("Hello, $name, follower of the usurper and bringer of your own demise. Was there anything else you wanted?")
                whatPlaceMenu()
            }
        }
    }

    private suspend fun Player.whatPlaceMenu() = choice("Select an Option") {
        option("What is this place?") {
            askWhatThisPlaceIs()
        }
        option<Neutral>("No, thanks.") {
        }
    }

    private suspend fun Player.afterTutorialDialogue() {
        when (fiaraIdentity()) {
            IDENTITY_SARADOMIN -> npc<Angry>("Hello, blind follower of evil good and keeper of lies. Can one such as I be of any help to you?")
            IDENTITY_GUTHIX -> npc<Neutral>("Hello, $name, what tales do you bring with you today? One would think you'd already know whatever I have to tell you.")
            IDENTITY_ZAMORAK -> npc<Angry>("Hello, usurper. Should you not be raining destruction on those who want nothing to do with you?")
            IDENTITY_PLAYER -> npc<Neutral>("Greetings, $name, can I be of assistance?")
            else -> npc<Neutral>("Can I help you?")
        }
        aboutFistOfGuthixMenu()
    }

    private suspend fun Player.aboutFistOfGuthixMenu(): Unit = choice("Select an Option") {
        option("I have some questions about the Fist of Guthix game.") {
            gameQuestionsMenu()
        }
        option("What are you?") {
            askWhatFiaraAfterTutorial()
        }
        option<Neutral>("Never mind.") {
        }
    }

    private suspend fun Player.gameQuestionsMenu(): Unit = choice("Select an Option") {
        option("Could you tell me all about the game again?") {
            npc<Neutral>("Okay, I will tell you. Now, listen carefully, so that you do not forget.")
            statement("The Fist of Guthix tutorial starts.")
            startTutorial(skipIntro = true)
            npc<Neutral>("Now, do you have any questions?")
            gameQuestionsMenu()
        }
        option("How do I get charges?") {
            player<Quiz>("How do I get charges?")
            npc<Neutral>("You can only get charges while you are in the role of the hunted. When the game starts, look around you - there should be a stone dispenser nearby. Pick up a magical stone from there and wield it to gather charges. The closer to the centre you are, the more charges you will get.")
            moreQuestionsMenu()
        }
        option("Why can't I get into the arena?") {
            player<Quiz>("Why can't I get into the game?")
            npc<Neutral>("There must be enough players in the waiting room for a game to start. Also, there can only be a certain amount of players in the game at any time.")
            moreQuestionsMenu()
        }
        option("I keep losing all the time!") {
            player<Angry>("I keep losing all the time!")
            npc<Neutral>("That was not a question, but I will give you some tips.")
            npc<Neutral>("Remember that the closer you are to the centre of the arena, the weaker you will become. If you find yourself getting defeated easily and that your armour does you no good, you may want to try to flee to the outer edges of the arena where you will be less weakened. This also applies to your opponent. If it is someone who appears to be a lot stronger than you, he or she will still be very weak closer to the centre - this is where you should strike. You should also know that while you are in the very centre, the energy from the Fist of Guthix will help you replenish your run energy much faster than usual.")
            npc<Neutral>("Also, use the ruins of the past to your advantage. Hide behind rubble and make a run for it when your opponent is out of reach. Use the buildings to hide or as a means of escape.")
            moreQuestionsMenu()
        }
        option<Neutral>("Never mind.") {
        }
    }

    private suspend fun Player.moreQuestionsMenu() {
        npc<Neutral>("Any more questions?")
        choice("Select an Option") {
            option<Quiz>("I have more questions about the Fist of Guthix game.") {
                gameQuestionsMenu()
            }
            option("What are you?") {
                askWhatFiaraAfterTutorial()
            }
            option<Neutral>("No, thanks.") {
            }
        }
    }

    private suspend fun Player.askWhatFiaraAfterTutorial() {
        player<Quiz>("What are you?")
        npc<Neutral>("I am Fiara, a Guardian of Guthix.")
        player<Neutral>("And you are guarding the Fist of Guthix, which, if I don't already know about, I am not fit to know about?")
        npc<Neutral>("That is correct. Was there anything else?")
        someQuestionsMenu()
    }

    private suspend fun Player.someQuestionsMenu() = choice("Select an Option") {
        option("I have some questions about the Fist of Guthix game.") {
            gameQuestionsMenu()
        }
        option<Neutral>("No, thanks.") {
        }
    }

    private suspend fun Player.startTutorial() {
        startTutorial(skipIntro = false)
    }

    private suspend fun Player.startTutorial(skipIntro: Boolean) {
        if (!skipIntro) {
            npc<Neutral>("Okay, I will tell you. Now, listen carefully so that you do not forget.")
            statement("The Fist of Guthix tutorial starts.")
        }
        npc<Neutral>("The passageway on my left leads into the waiting room. If you want to participate in the games, this is where you must go.")
        npc<Neutral>("Once enough adventurers have gathered, I will pair you up one-on-one against each other, and send you into the arena. The arena is the cave of the Fist of Guthix. Allow me to show you.")
        statement("The screen fades out and then fades in. Subsequent dialogue appears over views of the Fist of Guthix arena.")
        npc<Neutral>("Each game is divided into two rounds, and you and your opponent will take turns being the hunter and the hunted. I decide who starts as what.")
        npc<Neutral>("While you are the hunter, you will see the text 'Hunting' in the upper right corner of the screen.")
        npc<Neutral>("While you are the hunted, this text will say 'Hunted by'.")
        statement("The view pans to the centre of the arena.")
        npc<Neutral>("The arena is large and circular, and ancient power emanates from its centre. You will start on opposite sides of this arena.")
        npc<Neutral>("The goal of the hunter is simple: chase down and defeat the hunted by any means necessary.")
        statement("The view pans to the stone dispenser.")
        npc<Neutral>("The goal of the hunted is not quite as easy. When you appear in the arena, you will find yourself close to a small stone altar - a stone dispenser. On top of this, you should see a glowing stone.")
        npc<Neutral>("In the role of the hunted, you must pick up a stone from the dispenser. This is what you will use to gather the powerful energy from the centre of the arena.")
        npc<Neutral>("While you are wielding the stone - and only while you hold it in your hands, not when it's in your backpack - it will draw energy from the Fist of Guthix. Your objective is to get as many of these charges as possible.")
        npc<Neutral>("The amount of charges you have gathered is listed in the upper right corner.")
        npc<Neutral>("As the hunted, however, you must be careful. Your opponent, the hunter, is there to defeat you. While you are wielding the stone, you will be unable to fight.")
        npc<Neutral>("Also, you may feel very strong, but in the role of the hunted, you will find your abilities severely drained, more as you get closer to the centre - someone who otherwise may be an easy opponent to defeat in combat may instead prove most dangerous.")
        npc<Neutral>("You can of course try to fight back against your opponent, even though it may be difficult. Also, as the hunted, you will be limited in your magical abilities as well. Only a certain spellbook can aid you in this role.")
        npc<Neutral>("Speaking of magic, I should also mention that you will find that some spells are not as efficient in the cave as they may be in other areas.")
        npc<Neutral>("Finally, as the hunted, you will also take periodic damage from the powerful energies in the arena.")
        player<Quiz>("Okay, so the hunter only has to hunt down their target?")
        npc<Neutral>("That is correct.")
        player<Quiz>("And the hunted will be weaker than usual, has to pick up a stone from the stone dispenser at the edge of the arena, and wield this stone in order to charge it?")
        npc<Neutral>("Yes, and the closer to the centre of the arena you are, the more it will be charged.")
        player<Quiz>("Anything else?")
        statement("The view pans to one of the buildings in the arena.")
        npc<Neutral>("Yes. You will find four small buildings in the arena. Their doorways are blocked by magical barriers. Only the hunted can enter through these, and, while inside, will be completely invisible to everyone else.")
        npc<Neutral>("The barriers are dangerous, however, and will drain some of your health as you pass through them.")
        npc<Neutral>("Inside these houses, you will find portals. You can use them to teleport from house to house - still invisible to everyone else - but be aware that the magic is very old and the portals have not been actively used for thousands of years.")
        npc<Neutral>("They may act a bit strangely from time to time. Also, the portals require energy to work properly, and will drain some charges from your stone. If you don't have enough charges, you won't be able to use the portal.")
        player<Quiz>("Okay, I will keep that in mind. So, when does the round end?")
        npc<Neutral>("A round can end in four different ways.")
        npc<Neutral>("One: If the hunted is defeated.")
        npc<Neutral>("Two: If the hunted completely charges their stone - which is unlikely to happen.")
        statement("A scoreboard and 'Time left:' bar appear on screen.")
        npc<Neutral>("Three: If the time runs out - each round can take no longer than ten minutes. You can see how much time there is left in the lower left corner of the screen.")
        statement("The view pans to the exit of the arena.")
        npc<Neutral>("Four: If either player leaves the arena, in which case the other one will be declared the winner. The forfeiter will also be flagged as such and receive fewer tokens in the next game they play.")
        npc<Neutral>("When the first round has ended, the second round starts immediately and the roles switch. The one who played the hunter in the first round will now have a chance to play the hunted and gather charges.")
        npc<Neutral>("When both rounds have been played, the one with the most charges wins.")
        player<Quiz>("Ooh, what are the rewards?")
        npc<Neutral>("The druids put a lot of value into the gathered energy, and you will be awarded Fist of Guthix tokens. How many tokens you get depends on your total skill level - a wide and balanced array of knowledge is something positive in the eyes of Guthix - and how many charges you've got.")
        npc<Neutral>("The Fist of Guthix tokens can then be spent on items in the shop the druids have decided to clutter my cave with.")
        npc<Neutral>("You will also be awarded a rating, which is completely separate from the tokens. This will simply keep track of how well you play the game. If you win, your rating improves. If you lose, your rating will suffer.")
        npc<Neutral>("Your rating will improve significantly if you gather a lot more charges than your opponent. Also, the rating will be further adjusted if the difference in combat levels is large.")
        npc<Neutral>("If you lose to someone much more capable than you in combat, your rating won't suffer as much as if you lost to someone around your own level.")
        player<Quiz>("Is that it?")
        npc<Neutral>("Almost. Patience is a great virtue.")
        npc<Neutral>("Finally, you cannot bring just anything you want into the game. Only items usable in combat, but no food and no potions.")
        npc<Neutral>("I will supply you with some means of healing and a large amount of generic runes, called elemental runes and catalytic runes, for your magic.")
        npc<Neutral>("These runes can be used in place of any other runes you can take with you, but do note that they can only be used in the Fist of Guthix cave.")
        npc<Neutral>("Also, any arrows or other forms of ammunition used in the arena will be handed back to you after the game. Assuming they are still intact, of course. And don't leave anything lying around! I don't want you to completely ruin this sacred site.")
        statement("The screen fades out and then fades back in. The view is back in the Fist of Guthix lobby.")
        set("fist_of_guthix_tutorial_complete", true)
    }

    private fun Player.fiaraIdentity(): String = get("fist_of_guthix_fiara_identity", "")

    private fun Player.setFiaraIdentity(identity: String) {
        set("fist_of_guthix_fiara_identity", identity)
    }

    private fun Player.hasSeenFiaraIntroduction(): Boolean = get("fist_of_guthix_seen_fiara_introduction", false)

    private fun Player.setHasSeenFiaraIntroduction() {
        set("fist_of_guthix_seen_fiara_introduction", true)
    }

    private fun Player.setHasCompletedFiaraWhatIsThisPlaceDialogue() {
        set("fist_of_guthix_completed_fiara_what_is_this_place_dialogue", true)
    }

    private fun Player.spokenToDruid(): Boolean = get("fist_of_guthix_spoken_to_alran", false) ||
        get("fist_of_guthix_spoken_to_getorix", false) ||
        get("fist_of_guthix_spoken_to_pontimer", false)

    companion object {
        private const val IDENTITY_PLAYER = "player"
        private const val IDENTITY_SARADOMIN = "saradomin"
        private const val IDENTITY_GUTHIX = "guthix"
        private const val IDENTITY_ZAMORAK = "zamorak"
    }
}
