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

class RadigadPonfit : Script {
    init {
        npcOperate("Talk-to", "radigad_ponfit") {
            when (questStage("in_search_of_the_myreque")) {
                in myrequeStage("delivered_weapons")..myrequeStage("hellhound_summoned") -> {
                    message("This person isn't able to talk to you at this time.")
                }
                myrequeStage("entered_hideout") -> {
                    npc<Neutral>(
                        "Greetings friend! Once you've introduced yourself to Veliaf please feel " +
                            "free to chat with me...",
                    )
                }
                myrequeStage("met_veliaf"), myrequeStage("weapons_accepted") -> {
                    if (!get("met_radigad", false)) {
                        set("met_radigad", true)
                        npc<Happy>(
                            "Ah it's $name. I see you've introduced yourself to Veliaf, " +
                                "good! Now, how can I be of service to you?",
                        )
                        player<Quiz>("Do you mind if I ask a few questions?")
                    } else {
                        npc<Happy>(
                            "Hello again! Radigad at your service. Are you planning to join the fray " +
                                "against the Morytania monster and the rest of the Drakans?",
                        )
                        player<Quiz>("Maybe! I'd like to ask a few questions if that's Ok?")
                    }
                    npc<Neutral>("Sure, go ahead, I'll answer as best I can.")
                    questionsChoice()
                }
                else -> aftermath()
            }
        }
    }

    suspend fun Player.questionsChoice() {
        choice {
            whatsYourJob()
            tellMeAboutMyreque()
            futurePlans()
            heardRumors()
            option<Neutral>("Ok, thanks.")
        }
    }

    fun ChoiceOption.whatsYourJob(): Unit = option<Quiz>("What's your job here?") {
        npc<Neutral>(
            "I help where I can. I'm a bit of an all rounder. I try to help Veliaf as much as " +
                "I can. I've relied on his sword arm many a time, I like to think that he can " +
                "rely on mine.",
        )
        questionsChoice()
    }

    fun ChoiceOption.tellMeAboutMyreque(): Unit = option<Quiz>("What can you tell me about the Myreque?") {
        npc<Neutral>(
            "We're just developing the resistance against the Drakans and the rest of the " +
                "Vampiric entourage. We're seriously out numbered, Veliaf doesn't like to hear " +
                "us saying 'hopeless' but that's our situation at the minute.",
        )
        questionsChoice()
    }

    fun ChoiceOption.futurePlans(): Unit = option<Quiz>("What are your plans for the future?") {
        npc<Neutral>(
            "I'm not really sure, I guess Veliaf might know that, however I hope that more " +
                "people will join the ranks and take the fight to the dead hearts of our " +
                "Vampiric foe so that we might one day reclaim our right to exist.",
        )
        questionsChoice()
    }

    fun ChoiceOption.heardRumors(): Unit = option<Quiz>("Have you heard any rumors?") {
        npc<Neutral>(
            "I have. Something you might find interesting is that our enigmatic leader 'Safalaan' " +
                "has connections inside the Drakan Clan. I'm not sure how true that is but it " +
                "would be great!",
        )
        questionsChoice()
    }

    private suspend fun Player.aftermath() {
        player<Quiz>("Hello, are you ok?")
        npc<Sad>(
            "Yeah, I'm Ok thanks. You certainly proved yourself against that unearthly monster " +
                "that Vanstrom conjured. I'm sorry I can't be more upbeat, but I'm very sad " +
                "about Harold and Sani. It's terrible to see your comrades cut down in combat, " +
                "especially those two. They were still so very young.",
        )
    }

    private fun getMyrequeMember(player: Player, index: Int): Boolean = player["myreque_member_$index", false]

    private fun setMyrequeMember(player: Player, index: Int, value: Boolean) {
        player["myreque_member_$index"] = value
    }
}
