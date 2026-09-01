package content.area.morytania.mort_myre_swamp

import content.entity.player.dialogue.Angry
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

class IvanStrom : Script {
    init {
        npcOperate("Talk-to", "ivan_strom_meiyerditch_tunnels") {
            when (questStage("in_search_of_the_myreque")) {
                in myrequeStage("delivered_weapons")..myrequeStage("hellhound_summoned") -> {
                    message("This person isn't able to talk to you at this time.")
                }
                myrequeStage("entered_hideout") -> {
                    npc<Sad>(
                        "Oh...er... hello...greetings that is...sorry... you need to speak to Veliaf " +
                            "first. I'm not sure if I'm supposed to be talking to you.",
                    )
                }
                myrequeStage("met_veliaf"), myrequeStage("weapons_accepted") -> {
                    if (get("met_ivan", false)) {
                        npc<Happy>("Oh, hello again. How are you?")
                        questionsChoice()
                    } else {
                        firstMeeting()
                    }
                }
                else -> aftermath()
            }
        }
    }

    private suspend fun Player.firstMeeting() {
        player<Quiz>(
            "Hi, Veliaf said that I should introduce myself to all members of the Myreque.",
        )
        set("met_ivan", true)
        npc<Neutral>(
            "Ok, Veliaf says it's ok for me to talk to you. My name is Ivan, nice to meet you.",
        )
        player<Quiz>("Would you mind if I ask you a few questions?")
        npc<Neutral>("Yeah, I suppose so, but I don't know how much I can help.")
        questionsChoice()
    }

    suspend fun Player.questionsChoice() {
        choice {
            whatAreYouDoing()
            tellMeAboutMyreque()
            aboutMorytania()
            heardRumors()
            option<Neutral>("Ok, thanks.")
        }
    }

    fun ChoiceOption.whatAreYouDoing(): Unit = option<Quiz>("What are you doing here?") {
        npc<Neutral>(
            "I'm not sure really, I should really be at the temple to Saradomin, but Veliaf says " +
                "that the trip's too dangerous. I am a trainee cleric of Saradomin. I hope one day " +
                "to serve him as faithfully as all his priests.",
        )
        questionsChoice()
    }

    fun ChoiceOption.tellMeAboutMyreque(): Unit = option<Quiz>("What can you tell me about the Myreque?") {
        npc<Sad>(
            "Well, they're certainly brave and they're prepared to put up a fight against the " +
                "evil of the Drakan clan. I'm proud to be associated with them, but with my poor " +
                "skills I worry that I will let them down.",
        )
        questionsChoice()
    }

    fun ChoiceOption.aboutMorytania(): Unit = option<Quiz>("What do you know about Morytania?") {
        npc<Neutral>(
            "It's a lost land ruled by the evil Drakans. People cling to each other in fear every " +
                "week when the blood tithes are to be paid. With Saradomin's help we'll bring " +
                "light back to this dark land.",
        )
        questionsChoice()
    }

    fun ChoiceOption.heardRumors(): Unit = option<Quiz>("Have you heard any rumors?") {
        npc<Neutral>(
            "I'm sorry my friend, but I spend most of my free time in prayer to Saradomin. " +
                "I prefer to cleanse my soul in case he should wish that I serve him closer.",
        )
        questionsChoice()
    }

    private suspend fun Player.aftermath() {
        npc<Sad>(
            "Oh, thank Saradomin you managed to kill that awful beast! Oh my, I'm in so much " +
                "shock after seeing Sani and Harold killed right in front of my own eyes.",
        )
        player<Sad>(
            "I'm so sorry for your loss. If there's anything I can do, please let me know.",
        )
        npc<Angry>(
            "I think you've done enough already, bringing that monster Vanstrom here!",
        )
    }
}
