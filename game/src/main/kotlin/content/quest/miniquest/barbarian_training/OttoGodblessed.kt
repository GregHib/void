package content.quest.miniquest.barbarian_training

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shock
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.barbarian_training.BarbarianTraining.DONE
import content.quest.miniquest.barbarian_training.BarbarianTraining.NOT_STARTED
import content.quest.miniquest.barbarian_training.BarbarianTraining.REPORTED
import content.quest.miniquest.barbarian_training.BarbarianTraining.STARTED
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class OttoGodblessed : Script {

    val sections = listOf("barehand", "rod", "herblore", "bow", "pyre", "spear", "hasta")

    init {
        npcOperate("Talk-to", "otto_godblessed_baxtorian_falls") {
            if (!get("barbarian_training_started", false)) {
                npc<Neutral>("Good day, you seem a hearty warrior. Maybe even some barbarian blood in that body of yours?")
                introduction()
                return@npcOperate
            }
            if (giveBooks()) {
                return@npcOperate
            }
            if (sections.all { barbarianTraining(it) == REPORTED }) {
                npc<Happy>("Welcome back oh friend of the spirits, I am humbled in your presence.")
                player<Quiz>("That's very kind, but I was wondering whether you have any more information for me?")
                npc<Neutral>("You are no longer the apprentice, but the master. Seek to inspire us with your daring deeds and legendary feats. Aspire to join the spirits when your time comes.")
                choice {
                    option<Quiz>("Could you tell me more of these spirits you continually refer to?") {
                        spirits()
                    }
                    option<Neutral>("I have no more questions at this time.") {
                        npc<Neutral>("In that case, farewell.")
                    }
                }
                return@npcOperate
            }
            topics()
        }
    }

    suspend fun Player.introduction() {
        choice {
            option<Neutral>("I really don't think I am related to any barbarian.") {
                npc<Neutral>("Your scepticism will be your loss.")
            }
            option<Quiz>("You think so?") {
                npc<Neutral>("Who can tell? My forefathers weren't averse to travelling, so it is possible. They tended to cause too much trouble in your so-called civilised lands, however, so most returned to their ancestral lands.")
                npc<Neutral>("In any case, I think you are ready to learn our more secret tribal feats for yourself.")
                player<Quiz>("Oh, that sounds interesting, what sort of skills would these be?")
                npc<Neutral>("To begin with I can supply knowledge in the ways of firemaking, our special rod fishing tricks and a selection of spear skills.")
                player<Quiz>("There are more advanced stages though, to judge by your description?")
                npc<Neutral>("Your perception is creditable. I can eventually teach of more advanced firemaking techniques, and the rod fishing skills are but a preliminary to our special potions and brews.")
                npc<Neutral>("These secrets must, however, wait until you have learned of the more basic skills.")
                set("barbarian_training_started", true)
                topics()
            }
            option<Quiz>("Who are you?") {
                player<Quiz>("Who are you and why are you here?")
                npc<Neutral>("Me? I am Otto, known as the Godblessed. I follow the ways of our barbarian ancestors, communing with the spirits of the dead and teaching their ways to worthy disciples.")
                npc<Neutral>("Maybe there's even some barbarian blood in that body of yours? You look a likely sort to learn of these ways.")
                introduction()
            }
            option<Neutral>("Sorry, I'm too busy to talk about genealogy today.") {
                npc<Neutral>("We will talk when you learn to be less impetuous.")
            }
        }
    }

    /**
     * Hands out the record books if the player has lost them.
     */
    suspend fun Player.giveBooks(): Boolean {
        if (inventory.isFull()) {
            return false
        }
        if (!ownsItem("barbarian_skills")) {
            npc<Neutral>("I see you have free space and no record of the tasks I have set you. Please take this book as a record of your progress - between the spirits and your diligence, I expect it will always be up to date.")
            inventory.add("barbarian_skills")
            return true
        }
        if (barbarianTrainingStarted("pyre") && !ownsItem("my_notes")) {
            npc<Neutral>("I see you have free space and no way to record any information you may recover from the caverns. Please take this book as a record of your researches - between the spirits and your diligence I expect it will always be up to date.")
            player<Quiz>("Why might I need such a thing? I already have a book of notes.")
            npc<Neutral>("Those who have previously entered the cavern have not all returned, some of them seemed intent upon research and discovery as much as loot and daring deeds.")
            npc<Neutral>("It would be a shame if you were to recover any of their notes and be unable to record them handily. I hope the new book will give you space for such discoveries.")
            inventory.add("my_notes")
            return true
        }
        return false
    }

    suspend fun Player.topics() {
        choice {
            if (barbarianTraining("barehand") != REPORTED) {
                option<Quiz>("Please, supply me details of your cunning with harpoons.") {
                    barehandFishing()
                }
            }
            if (barbarianTraining("rod") != REPORTED) {
                option<Quiz>("Are there any ways to use a fishing rod which I might learn?") {
                    rodFishing()
                }
            } else if (barbarianTraining("herblore") != REPORTED) {
                option<Quiz>("What was that secret knowledge of Herblore we talked of?") {
                    herblore()
                }
            }
            if (barbarianTraining("bow") != REPORTED) {
                option<Neutral>("My mind is ready for your Firemaking wisdom, please instruct me.") {
                    bowFiremaking()
                }
            } else if (barbarianTraining("pyre") != REPORTED) {
                option<Quiz>("I have completed Firemaking with a bow. What follows this?") {
                    pyreShips()
                }
            }
            if (barbarianTraining("rod") == REPORTED && barbarianTraining("barehand") == REPORTED) {
                if (barbarianTraining("spear") != REPORTED) {
                    option<Quiz>("Tell me more about the use of spears.") {
                        spears()
                    }
                } else if (barbarianTraining("hasta") != REPORTED) {
                    option<Quiz>("What of the one-handed spears of which you spoke?") {
                        hastae()
                    }
                }
            }
            if (barbarianTrainingStarted("pyre")) {
                option<Quiz>("Could you tell me more of these spirits you continually refer to?") {
                    spirits()
                }
            }
        }
    }

    suspend fun Player.moreQuestions() {
        choice {
            option("I seek more answers.") {
                topics()
            }
            option<Neutral>("I have no more questions at this time.") {
                npc<Neutral>("In that case, farewell.")
            }
        }
    }

    suspend fun Player.barehandFishing() {
        when (barbarianTraining("barehand")) {
            NOT_STARTED -> {
                npc<Neutral>("First you must know more of harpoons through special study of fish that are usually caught with such a device.")
                player<Quiz>("What do I need to know?")
                npc<Neutral>("You must catch fish which are usually harpooned, without a harpoon. You will be using your skill and strength.")
                player<Quiz>("How do you expect me to do that?")
                npc<Neutral>("Use your arm as bait. Wriggle your fingers as if they are a tasty snack and hungry tuna and swordfish will throng to be caught by you.")
                player<Shock>("That sounds rather insanely dangerous. I'm glad you didn't mention shark too.")
                npc<Neutral>("Oh, my mind slipped for a moment, this method does indeed work with shark - though in this case the action must be more of a frenzied thrashing of the arm than a wriggle.")
                player<Neutral>("...and I thought Fishing was a safe way to pass the time.")
                set("barbarian_training_barehand", STARTED)
            }
            STARTED -> {
                npc<Neutral>("I see you need encouragement in learning the ways of Fishing without a harpoon.")
                player<Neutral>("It's not the most safe-sounding task I have ever been given.")
                npc<Neutral>("Neither is it the most deadly. Go now and return when you have drawn a fish from the sea, with sheer strength and knowledge of our scaled prey.")
            }
            DONE -> {
                // TODO confirm report dialogue (from OSRS)
                npc<Neutral>("I sense you have more understanding of harpoons through your studies.")
                player<Quiz>("Well it was interesting, can I move on to more sane hobbies now?")
                npc<Neutral>("Nothing is sane when one is born knowing only certain death. The spirits approve of you however, so death is on your side.")
                player<Quiz>("What is that supposed to mean?")
                npc<Neutral>("I mean that when you eventually die and find peace, at least the spirits you encounter will be your friends. Alas for you adventurous sort, the natural ways of passing are close to impossible.")
                set("barbarian_training_barehand", REPORTED)
            }
        }
        moreQuestions()
    }

    suspend fun Player.rodFishing() {
        when (barbarianTraining("rod")) {
            NOT_STARTED -> {
                npc<Neutral>("While you civilised folk use small, weak fishing rods, we barbarians are skilled with heavier tackle. We fish in the lake nearby.")
                player<Quiz>("So can you teach me of this Fishing method?")
                npc<Neutral>("Certainly. Take the rod from under the bed in my dwelling and fish in the lake. When you have caught a few fish I am sure you will be ready to talk more with me.")
                npc<Neutral>("You will know when you are ready, since inspiration will fill your mind.")
                player<Quiz>("So I can obtain new foods from these Fishing spots?")
                npc<Neutral>("We do not use these fish quite as you might expect. When you return from Fishing we can speak more of this matter.")
                set("barbarian_training_rod", STARTED)
            }
            STARTED -> {
                npc<Neutral>("Alas, I do not sense that you have been successful in your Fishing yet. The look in your eyes is not that of the osprey.")
                player<Quiz>("Osprey?")
                npc<Neutral>("Legendary birds, which the ignorant call eagles. They prey upon fish. Do as they do to gain inspiration.")
            }
            DONE -> {
                npc<Neutral>("Your mind is as clear as the water you have fished. This is good.")
                player<Quiz>("So what do I do with these fish? They are all bloated and so nasty looking that even a starving vulture wouldn't eat one.")
                npc<Neutral>("Patience young one. These are fish which are fat with eggs rather than fat of flesh, that is what we will make use of. We can speak of this in a few moments.")
                set("barbarian_training_rod", REPORTED)
            }
        }
        moreQuestions()
    }

    suspend fun Player.herblore() {
        when (barbarianTraining("herblore")) {
            NOT_STARTED -> {
                npc<Neutral>("If you use a knife upon a fat fish, several new fishy items will be produced. Fish parts can be used as bait; the roe or caviar is more useful for us.")
                npc<Neutral>("Mixing these items with two dose potions is what should be performed. This results in nutritious slop, perfect for healing as well as imparting the effect of the potion.")
                npc<Neutral>("Roe can be used for some more easily combined mixes, while caviar may be used for any mixes of which I am aware.")
                npc<Neutral>("You will discover you will be able to decant a four dose potion into an empty vial, thus giving two potions of two doses. This will aid you in the process.")
                npc<Neutral>("In this case I in fact require a potion for my own stocks. Bring a lesser attack potion combined with fish roe.")
                set("barbarian_training_herblore", STARTED)
            }
            else -> if (inventory.contains("attack_mix_2")) {
                npc<Neutral>("I see you have my potion. I will say no more than that I am eternally grateful.")
                inventory.remove("attack_mix_2")
                set("barbarian_training_herblore", REPORTED)
                player<Neutral>("I feel I am missing some vital information about your need for this potion, though I often have this suspicion.")
                npc<Neutral>("I will not reveal all of my private matters to you. Some secrets are best kept rather than revealed.")
            } else {
                npc<Quiz>("Do you have my potion?")
                player<Quiz>("What was it you needed again?")
                npc<Neutral>("Bring me a lesser attack potion combined with fish roe. There is more importance in this than you will ever know.")
            }
        }
        moreQuestions()
    }

    suspend fun Player.bowFiremaking() {
        when (barbarianTraining("bow")) {
            NOT_STARTED -> {
                npc<Neutral>("The first point in your progression is that of lighting fires without a tinderbox.")
                player<Quiz>("That sounds pretty useful, tell me more.")
                npc<Neutral>("For this process you will require a strung bow. You use the bow to quickly rotate pieces of wood against one another. As you rub the wood becomes hot, eventually springing into flame.")
                player<Quiz>("No more secret details?")
                npc<Neutral>("The spirits will aid you, the power they supply will guide your hands. Go and benefit from their guidance upon an oaken log.")
                set("barbarian_training_bow", STARTED)
            }
            STARTED -> {
                npc<Neutral>("By now you know my response.")
                player<Quiz>("Go and light some oak logs using a strung bow?")
                npc<Neutral>("Exactly.")
            }
            DONE -> {
                // TODO confirm whether the topic line is repeated first
                player<Happy>("The Firemaking with my bow worked!")
                npc<Happy>("Fine news indeed, secrets of our spirit boats now await your attention.")
                set("barbarian_training_bow", REPORTED)
            }
        }
        moreQuestions()
    }

    suspend fun Player.pyreShips() {
        when (barbarianTraining("pyre")) {
            NOT_STARTED -> {
                npc<Neutral>("The next stage is quite complex, so listen well.")
                pyreInstructions()
                set("barbarian_training_pyre", STARTED)
            }
            STARTED -> {
                npc<Neutral>("I will repeat myself fully, since this is quite complex. Listen well.")
                pyreInstructions()
            }
            DONE -> {
                // TODO confirm report dialogue (from OSRS)
                player<Happy>("I've created a pyre ship!")
                npc<Happy>("Hail to you, saviour of my ancestors. The spirits herald your presence with a spectral fanfare.")
                player<Happy>("It's good to be appreciated.")
                npc<Neutral>("On this great day you have my eternal thanks. May you find riches while rescuing my spiritual ancestors in the caverns for many moons to come.")
                set("barbarian_training_pyre", REPORTED)
            }
        }
        moreQuestions()
    }

    suspend fun Player.pyreInstructions() {
        npc<Neutral>("In order to send our ancestors into the spirit world, their mortal remains must be burned with due ceremony. This can only be performed close to the water on the shore of the lake, just to our north-east.")
        npc<Neutral>("You will recognise the correct places by the ashes to be seen there.")
        npc<Neutral>("You will need to construct a small ship by using a hatchet upon logs in this area, then add the bones of a long dead barbarian hero.")
        player<Quiz>("Where do I obtain these bones?")
        npc<Neutral>("From the caverns beneath this lake. Many of our ancestors travelled to these caverns in order to hunt for glory and found only death. Their bones must still lie inside, their spirits trapped in torment.")
        player<Quiz>("So I make sure I am in the right place, then throw these bones on a ceremonial ship before I set light to it?")
        npc<Neutral>("Correct. The spirit will ascend to glory; the pyre will send the earthly remains to the depths. You will also obtain a closer link to the spirit world.")
        npc<Neutral>("During this heightened contact, any bones you bury will have increased importance to the gods. The number of bones that may be buried, before the link fades, is increased with the difficulty of obtaining the wood which you use.")
        player<Quiz>("Do you foresee any problems?")
        npc<Neutral>("I have little knowledge of the caverns - they are blocked from the sight of the spirits with whom I commune. I can only suspect that whatever slew barbarian heroes is indeed mighty.")
        npc<Neutral>("I would also suggest that these bones might well be very uncommon, since heroes are not found in vast numbers. Good luck.")
        player<Quiz>("How do I enter these caverns then?")
        npc<Neutral>("Dive into the whirlpool in the lake to the east. The spirits will use their abilities to ensure you arrive in the correct location, though their influence fades, so you must find your own way out.")
    }

    suspend fun Player.spears() {
        if (!questCompleted("tai_bwo_wannai_trio")) {
            npc<Neutral>("The next step is to manufacture a spear, suitable for combat. Our distance cousins on Karamja are in need of help, however, and you must aid them before I can aid you.")
            npc<Neutral>("You must go now and complete the Tai Bwo Wannai Trio quest.")
            player<Quiz>("Couldn't you just take a bribe or something? I am sure you could do with some cash.")
            npc<Neutral>("I am afraid this is a vital step; the spirits foresee that your understanding of spears will increase through this quest. You may not progress in the use of spears until you have completed this mission.")
            moreQuestions()
            return
        }
        when (barbarianTraining("spear")) {
            NOT_STARTED -> {
                npc<Neutral>("Many warriors complain that spears are difficult to find, we barbarians thus forge our own.")
                // TODO confirm player lines (from OSRS)
                player<Quiz>("Can I just use some metal on an anvil, or is there something else involved?")
                npc<Neutral>("If you use our special barbarian anvil here, you will find it ideal. Other forges are not sturdy enough or for the forging work involved. Make any of our spears and return.")
                npc<Neutral>("Note well that you will require wood for the spear shafts, the quality of the wood must be similar to that of the metal involved.")
                set("barbarian_training_spear", STARTED)
            }
            STARTED -> {
                // TODO confirm reminder dialogue (from OSRS)
                npc<Neutral>("You do not exude the presence of one who has poured his soul into manufacturing spears.")
                player<Quiz>("The spirits told you?")
                npc<Neutral>("Indeed they did. There are no secrets from those who live eternally.")
            }
            DONE -> {
                npc<Neutral>("The manufacture of spears is now yours as a speciality. Use your skill well. In addition, I am ready to reveal more spear-related crafts.")
                set("barbarian_training_spear", REPORTED)
            }
        }
        moreQuestions()
    }

    suspend fun Player.hastae() {
        when (barbarianTraining("hasta")) {
            NOT_STARTED -> {
                npc<Neutral>("The next step up is to manufacture a one handed version of a spear, suitable for combat. Such a spear is known as a hasta.")
                // TODO confirm player lines (from OSRS)
                player<Quiz>("Can't I just pick one up and use it in one hand?")
                npc<Neutral>("As you might suspect, our ways require greater understanding than is gained by looking at a weapon. It is also the case that the process involves a differently balanced spear.")
                npc<Neutral>("Before you use such a weapon in anger you must make an example. Only then will you understand the poise of the techniques involved.")
                player<Neutral>("So I must manufacture one? I see. This makes sense.")
                npc<Neutral>("You may use our special anvil for this spear type too. As the ways of black and dragon are beyond our knowledge, however, these spears may not be created.")
                set("barbarian_training_hasta", STARTED)
            }
            STARTED -> {
                // TODO confirm reminder dialogue (from OSRS)
                npc<Neutral>("Take some wood and metal and make a spear upon the nearby anvil, then you may return to me. As an example, you may use bronze bars with normal logs or iron bars with oak logs.")
                npc<Neutral>("If you perform this task I will be freed from the spirits complaining of your lack of dedication.")
                player<Neutral>("I'll be back.")
            }
            DONE -> {
                // TODO confirm report dialogue (from OSRS)
                npc<Neutral>("I see you have constructed your hasta, and are approaching readiness.")
                player<Quiz>("If I may ask, readiness for what?")
                npc<Neutral>("To live life to it's fullest of course - that you may be a peaceful spirit when your time ends.")
                set("barbarian_training_hasta", REPORTED)
            }
        }
        moreQuestions()
    }

    suspend fun Player.spirits() {
        npc<Neutral>("Certainly, though it is quite simple - they are the barbarians of the past, who have died in the passing of the years. There are three categories which learned sages claim may describe all spirits encountered.")
        npc<Neutral>("The majority are at peace, having died with their ambitions sated, even if this ambition was as simple as a glorious death. They can contact those in our world in order to spur us on to glory.")
        npc<Neutral>("Of course, they cannot or will not contact all inhabitants of these lands. All are not worthy.")
        player<Quiz>("So those are the spirits who give us guidance. What of the other sorts?")
        npc<Neutral>("A second category of spirit is made up of those who are not yet at peace. However, all that is needed is for their mortal remains to be laid to rest and they will join the peaceful majority.")
        player<Quiz>("I can see the pyres serve this purpose. The final category is also restless?")
        npc<Neutral>("Alas, there is a small minority who will never be at rest. These have been slain in moments of insanity by their brothers or by adventurers. They are not at rest and will attack those they blame for their tortured state.")
        npc<Neutral>("They are not known, however, for being able to determine this blame with any great accuracy, so may be considered as dangerous to all.")
        player<Neutral>("I thank you Otto, it makes a bit more sense now.")
        if (sections.all { barbarianTraining(it) == REPORTED }) {
            return
        }
        moreQuestions()
    }
}
