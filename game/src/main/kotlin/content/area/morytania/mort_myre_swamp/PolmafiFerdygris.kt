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

class PolmafiFerdygris : Script {
    init {
        npcOperate("Talk-to", "polmafi_ferdygris") {
            when (questStage("in_search_of_the_myreque")) {
                in myrequeStage("delivered_weapons")..myrequeStage("hellhound_summoned") -> {
                    message("This person isn't able to talk to you at this time.")
                }
                myrequeStage("entered_hideout") -> {
                    npc<Neutral>(
                        "Good day. You should quickly introduce yourself to Veliaf and after that " +
                            "I'll be happy to have a chat with you.",
                    )
                }
                myrequeStage("met_veliaf"), myrequeStage("weapons_accepted") -> {
                    if (!get("met_polmafi", false)) {
                        set("met_polmafi", true)
                        npc<Happy>(
                            "Good! Now that you've introduced yourself to Veliaf and observed proper " +
                                "protocol, I'll be happy to chat with you. Now, please tell me, what " +
                                "can I do for you?",
                        )
                        player<Quiz>("Do you mind if I ask a few questions?")
                    } else {
                        npc<Happy>("Warmest salutations of the day my friend! How are you?")
                        player<Quiz>("Fine thanks. I'd like to ask a few questions if that's Ok?")
                    }
                    npc<Neutral>("Not at all, I'm sure I'll be able to help and if I can't I'll tell you so.")
                    questionsChoice()
                }
                else -> aftermath()
            }
        }
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
            "I am the key advisor to Veliaf, he seeks my council on many issues before we engage " +
                "the enemy. Not that we would be doing that any time soon, the situation is " +
                "somewhat difficult and terrifying at the moment.",
        )
        npc<Neutral>(
            "However, we must always remember, 'All that is required for evil to survive is for " +
                "good people to do nothing.'",
        )
        questionsChoice()
    }

    fun ChoiceOption.tellMeAboutMyreque(): Unit = option<Quiz>("What can you tell me about the Myreque?") {
        npc<Neutral>(
            "Well, we have a hierarchical chain of command. Veliaf reports to Calsidiu who is " +
                "considered the head of the armed resistance against the Drakans.",
        )
        npc<Neutral>(
            "I've also heard people talking about a spiritual man, who inspires many people to " +
                "join the resistance. He's said to be very influential with supposed contacts " +
                "within the Drakan clan.",
        )
        questionsChoice()
    }

    fun ChoiceOption.aboutMorytania(): Unit = option<Quiz>("What do you know about Morytania?") {
        npc<Neutral>(
            "It has a rugged landscape,, situated to the east of the River Salve. Primary " +
                "occupants seem to be human in apperance, but are in fact large vampiric predators.",
        )
        npc<Neutral>(
            "They maintain a stock of human villages in the same way that humans keep cattle. " +
                "Weekly blood tithes are paid to the Vampyric overlord Drakan in return for " +
                "moderate safety.",
        )
        questionsChoice()
    }

    fun ChoiceOption.heardRumors(): Unit = option<Quiz>("Have you heard any rumors?") {
        npc<Neutral>(
            "Yes, I hear them all the time, they're neither correct or even all that interesting, " +
                "I suspect that we will be looking to make a strike against the human " +
                "sympathisers some time soon.",
        )
        questionsChoice()
    }

    private suspend fun Player.aftermath() {
        npc<Sad>(
            "I'm so sad since Sani and Harold were killed. The Myreque will be lonelier without " +
                "them. Still, they didn't suffer at least.",
        )
        player<Sad>(
            "My commiserations for your loss. I wish that there was something I could do to help.",
        )
        npc<Sad>(
            "Don't worry. No one blames you. It could have been anyone that was tricked by him. " +
                "Whilst I'm sad at the loss of my comrades, it serves no useful purpose to blame " +
                "anyone for it.",
        )
    }
}
