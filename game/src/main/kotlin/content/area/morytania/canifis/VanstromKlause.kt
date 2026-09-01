package content.area.morytania.canifis

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.member.myreque.myrequeStage
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class VanstromKlause : Script {
    init {
        npcOperate("Talk-to", "stranger_2") {
            if (questStage("in_search_of_the_myreque") == myrequeStage("unstarted")) {
                npc<Neutral>("Hello there, how goes it stranger?")
                firstMeetingChoices()
            } else {
                npc<Quiz>("Hello again, how's the quest going?")
                returningChoices()
            }
        }
    }

    private suspend fun Player.firstMeetingChoices() {
        player<Neutral>("Quite well thanks for asking, how about you?")
        npc<Neutral>(
            "Hmm, well, I am a little concerned about some friends of mine, they're in dire need of " +
                "some assistance, but I'm at a loss of how I can help them.",
        )
        initialQuestionChoice()
    }

    suspend fun Player.initialQuestionChoice() {
        choice {
            whatFriends()
            whyHelp()
            tooBusy()
            option<Neutral>("Ok, thanks.")
        }
    }

    suspend fun Player.extendedQuestionChoice() {
        choice {
            whatFriends(extended = true)
            whyHelp()
            tooBusy()
            offerToHelp()
            option<Neutral>("Ok, thanks.")
        }
    }

    fun ChoiceOption.whatFriends(extended: Boolean = false): Unit = option<Neutral>("What friends are these?") {
        npc<Neutral>(
            "It's a personal tragedy that I have yet to meet them in the flesh. But their exploits " +
                "make mouth watering hero stories...the real meat and drink of high adventure and " +
                "daring...so they say.",
        )
        player<Neutral>(
            "What does that mean exactly? I mean, I have some stories. I'm quite a hero myself, " +
                "you may actually be talking about me?",
        )
        npc<Neutral>(
            "They're regarded as heroes in Morytania, though some people see them as vigilantes. " +
                "The local villagers call them the 'Myreque'. Some people call them terrorists while " +
                "others call them freedom fighters!",
        )

        if (extended) {
            extendedQuestionChoice()
        } else {
            initialQuestionChoice()
        }
    }

    fun ChoiceOption.whyHelp(): Unit = option<Neutral>("Why do they need help? Are they in trouble?") {
        npc<Happy>(
            "I should imagine that heroes of such high calibre are almost always in some sort of " +
                "trouble, wouldn't you? There's always some evil heel ready to grind the face of " +
                "humanity into the dirt?",
        )
        npc<Neutral>(
            "However, the Myreque are almost certainly able to handle themselves...given the tools! " +
                "I hear they're short of weapons, I was hoping to do it myself but I find that I'm " +
                "rather short of time and ability.",
        )
        player<Neutral>("What help do you hope to give them?")
        npc<Neutral>("I'd have taken some weapons to them!")
        player<Neutral>("What kind of weapons do they need?")
        npc<Neutral>(
            "Steel I believe. All six of them require steel weapons. I would have suggested a " +
                "longsword, two shortswords, a dagger, a mace and a warhammer.",
        )
        extendedQuestionChoice()
    }

    fun ChoiceOption.tooBusy(): Unit = option<Neutral>("I wish I could help, but I'm busy at the moment.") {
        npc<Neutral>(
            "Hmm, the same as me I fear! Oh well, I'm sure they'll get on well by themselves, " +
                "the little dears. Thanks for even considering it though.",
        )
    }

    fun ChoiceOption.offerToHelp() {
        option("Perhaps I could help you out here.") {
            player<Neutral>("Perhaps I could help you out here. Maybe I could take these weapons to the Myreque.")
            npc<Neutral>("Oh yes, well that would be very nice of you! Are you sure you want to help out?")
            helpDetailsChoice()
        }
    }

    suspend fun Player.helpDetailsChoice() {
        choice {
            whatToDo()
            whatWeapons()
            whereToTake()
            agreeToHelp()
            declineHelp()
        }
    }

    fun ChoiceOption.whatToDo(): Unit = option<Neutral>("What would I have to do?") {
        npc<Neutral>(
            "Well, some freedom fighter, called the Myreque, require some weapons. You'd need to " +
                "get the weapons they require and give them to the freedom fighters.",
        )
        helpDetailsChoice()
    }

    fun ChoiceOption.whatWeapons(): Unit = option<Neutral>("What weapons do I need to get?") {
        npc<Neutral>(
            "Well, I'm sure the following will be fine. A longsword, two shortswords, a dagger, " +
                "a mace and a warhammer, all the items should be made of steel.",
        )
        npc<Neutral>(
            "You would also have to fund the costs of this yourself and once you've found them, " +
                "I'll be sure to give you a nice surprise in return for your help.",
        )
        helpDetailsChoice()
    }

    fun ChoiceOption.whereToTake(): Unit = option<Neutral>("Where do I need to take the weapons?") {
        npc<Neutral>(
            "I believe there's a boatman in Mort'ton who can show you the way, I'm sure if you use " +
                "your powers of persuasion you can get some information out of him! The Myreque " +
                "have a lot of sympathisers.",
        )
        helpDetailsChoice()
    }

    fun ChoiceOption.agreeToHelp(): Unit = option<Happy>("Yes, I'll do it!") {
        set("in_search_of_the_myreque", "agreed_to_help")
        npc<Neutral>(
            "That's great news my friend, really great news! Perhaps the many peoples of Morytania " +
                "now have an additional hero that they can come to rely upon?",
        )
    }

    fun ChoiceOption.declineHelp(): Unit = option<Neutral>("Sorry, I can't do it!") {
        npc<Neutral>(
            "Oh well, that is a shame, I'm sorry to hear it. But if you change your mind, please " +
                "visit me again.",
        )
    }

    private suspend fun Player.returningChoices() {
        choice {
            needHelp()
            farewell()
            forgotTask()
        }
    }

    fun ChoiceOption.needHelp(): Unit = option<Sad>("Not very well, I need some help.") {
        npc<Neutral>("Oh dear... what do you need help with?")
        helpQuestionsChoice()
    }

    suspend fun Player.helpQuestionsChoice() {
        choice {
            whereMyreque()
            whatWeaponsAgain()
            howKnowMyreque()
            anyoneElse()
            farewell()
        }
    }

    fun ChoiceOption.whereMyreque(): Unit = option<Quiz>("Where are the Myreque?") {
        npc<Neutral>(
            "You know what? I'm not exactly sure. The only thing I've heard, and it is a rumour, " +
                "but the boatman in Mort'ton might be able to help. But again, it is only a rumour.",
        )
        helpQuestionsChoice()
    }

    fun ChoiceOption.whatWeaponsAgain(): Unit = option<Quiz>("What weapons am I supposed to get again?") {
        npc<Neutral>(
            "Well, I'm sure the following will be fine. A longsword, two shortswords, a dagger, " +
                "a mace and a warhammer, all the items should be made of steel.",
        )
        helpQuestionsChoice()
    }

    fun ChoiceOption.howKnowMyreque(): Unit = option<Quiz>("How do you know 'The Myreque'?") {
        npc<Neutral>(
            "Actually you know, I don't know them at all! I've never actually met them, but would " +
                "love to at some point! However, I'm sure that you'll be much more able than I at " +
                "tracking them down.",
        )
        helpQuestionsChoice()
    }

    fun ChoiceOption.anyoneElse(): Unit = option<Quiz>("Is there anyone else who can help me?") {
        npc<Neutral>("Hmm. Apart from the boatman at Mor'ton, I have no clue my friend.")
        helpQuestionsChoice()
    }

    fun ChoiceOption.forgotTask(): Unit = option<Quiz>("What am I supposed to do again?") {
        npc<Neutral>(
            "You've forgotten already? Ha! That's funny! But I do understand, my memory isn't " +
                "what it was!",
        )
        npc<Neutral>(
            "It would be great if you could get some steel weapons and take them to the Myreque, " +
                "the boatman in Mort'ton should be able to help you find them. But you may need " +
                "to be a bit persuasive!",
        )
    }

    fun ChoiceOption.farewell(): Unit = option<Neutral>("Ok, thanks.") {
        npc<Neutral>("Well, if you have any problems please feel free to come and chat with me.")
    }
}
