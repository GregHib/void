package content.area.morytania.mort_myre_swamp

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
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

class SaniPiliu : Script {
    init {
        npcOperate("Talk-to", "sani_piliu_meiyerditch_tunnels") {
            when (questStage("in_search_of_the_myreque")) {
                in myrequeStage("delivered_weapons")..myrequeStage("hellhound_summoned") -> {
                    message("This person isn't able to talk to you at this time.")
                }
                myrequeStage("entered_hideout") -> {
                    npc<Happy>(
                        "Hi there...you're new here aren't you? You'd best go and talk with Veliaf. " +
                            "But afterwards, I'd love to introduce myself properly.",
                    )
                }
                myrequeStage("met_veliaf"), myrequeStage("weapons_accepted") -> {
                    if (!get("met_sani", false)) {
                        firstMeeting()
                    } else {
                        npc<Happy>(
                            "Oh, hello $name nice to see you again! How's things?",
                        )
                        player<Neutral>("Fine thanks, do you mind if I ask a few questions?")
                        npc<Neutral>("Sure, go ahead, I'll answer as best I can.")
                        questionsChoice()
                    }
                }
            }
        }
    }

    private suspend fun Player.firstMeeting() {
        npc<Happy>(
            "Hello...very nice to meet you! My name is Sani Piliu, what's yours?",
        )
        player<Happy>("I am known as $name nice to meet you Sani.")
        set("met_sani", true)
        npc<Happy>(
            "The pleasure's all mine! Excuse me for being forward, but you somehow seem different " +
                "to other people, somehow more determined. I sense a powerful aura. Sorry, that " +
                "must seem really strange?",
        )
        player<Neutral>(
            "Well, perhaps a little but it's nice of you to say so in any case. I'd like to ask " +
                "a few questions if that's ok?",
        )
        npc<Neutral>("Sure, go ahead, I'll answer as best I can.")
        questionsChoice()
    }

    suspend fun Player.questionsChoice() {
        choice {
            tellAboutYourself()
            tellMeAboutMyreque()
            whatsGoingOn()
            mainCamp()
            option<Neutral>("Ok, thanks.")
        }
    }

    fun ChoiceOption.tellAboutYourself(): Unit = option<Neutral>("Tell me a bit about yourself.") {
        npc<Sad>(
            "There's not much to tell really. I was about nine years old when my family was " +
                "ripped to pieces by a vampire attack, one day a happy member of a loving " +
                "family, the next day an orphan living a hand to mouth existence.",
        )
        npc<Neutral>(
            "Somehow I managed to get by, eking out a living where ever I could. People took " +
                "pity on me, I earnt food by helping people out, before too long I was quite " +
                "adept at helping myself, other people appreciated my skills.",
        )
        npc<Neutral>(
            "I bumped into Veliaf in Canifis. He saw that I would be an asset to the resistance " +
                "so I joined. I want to fight back against those killers who took my family. " +
                "One day I may escape to the west across the Salve! Who knows?",
        )
        questionsChoice()
    }

    fun ChoiceOption.tellMeAboutMyreque(): Unit = option<Quiz>("What can you tell me about the Myreque?") {
        npc<Neutral>(
            "As far as I know Veliaf is one of two Lieutenants who report to another soldier " +
                "called Calsidiu. While we don't advertise the fact, there are quite a lot of " +
                "people in Morytania who want to put up a fight.",
        )
        npc<Neutral>(
            "Our current operations are merely trying to find a weakness in the Drakans' " +
                "hierarchy amd perhaps get a spy in there. We need to find out how to kill " +
                "them before we then take the war to them.",
        )
        player<Neutral>("You make it sound easy!")
        npc<Neutral>(
            "With determination you can achieve great things. However, Veliaf always tells us " +
                "not to underestimate the enemy. After those juvenile vampires discovered us " +
                "in the forest, it's a mistake I won't be making again.",
        )
        questionsChoice()
    }

    fun ChoiceOption.whatsGoingOn(): Unit = option<Quiz>("What's going on in Morytania?") {
        npc<Neutral>(
            "Lowernial Drakan and his siblings collect blood tithes from the locals as a way of " +
                "controlling the population and keeping his brood happy. Sanguinesti and Castle " +
                "Drakan hold sway over all the inhabitants of Morytania.",
        )
        npc<Neutral>(
            "We're nothing more than an ill ogranised rabble, but we're determined to fight " +
                "against these evil monsters. We won't just offer our family members to the " +
                "Drakans for a weekly snack, we're going to fight back!",
        )
        questionsChoice()
    }

    fun ChoiceOption.mainCamp(): Unit = option<Quiz>("Is this your main camp?") {
        npc<Neutral>("It's our only camp! At least, I don't know of any others.")
        questionsChoice()
    }
}
