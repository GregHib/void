package content.area.morytania.mort_myre_swamp

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
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

class HaroldEvans : Script {
    init {
        npcOperate("Talk-to", "harold_evans_meiyerditch_tunnels") {
            when (questStage("in_search_of_the_myreque")) {
                in myrequeStage("delivered_weapons")..myrequeStage("hellhound_summoned") -> {
                    message("This person isn't able to talk to you at this time.")
                }
                myrequeStage("entered_hideout") -> {
                    npc<Happy>(
                        "Hail and well met friend! You'd better go and introduce yourself to Veliaf. " +
                            "But after come and have a chat if you've a mind to.",
                    )
                }
                myrequeStage("met_veliaf"), myrequeStage("weapons_accepted") -> {
                    if (!get("met_harold", false)) {
                        set("met_harold", true)
                        npc<Happy>(
                            "Well met friend! My name's Harold...nice to meet a fellow soldier. " +
                                "Just joined us have you?",
                        )
                        player<Confused>("Not exactly! I'd like to ask a few questions if that's Ok?")
                        soldierResponse()
                    } else {
                        npc<Happy>("Well met $name! Here's to your strong sword arm!")
                        player<Quiz>("Do you mind if I ask a few questions?")
                        soldierResponse()
                    }
                }
            }
        }
    }

    private suspend fun Player.soldierResponse() {
        npc<Happy>("Sure go ahead, I'll answer as best I can.")
        questionsChoice()
    }

    suspend fun Player.questionsChoice() {
        choice {
            whatsYourJob()
            tellMeAboutSquad()
            graspOfSituation()
            heardAnyRumors()
            option<Neutral>("Ok, thanks.")
        }
    }

    fun ChoiceOption.whatsYourJob(): Unit = option<Quiz>("What's your job here?") {
        npc<Happy>(
            "I generally do what Veliaf orders...taking a bridge defending a tactical retreat..." +
                "you name it, I'm usually in the thick of it. It's funny, I seem to cheat death " +
                "most of the time.",
        )
        questionsChoice()
    }

    fun ChoiceOption.tellMeAboutSquad(): Unit = option<Quiz>("What can you tell me about your squad?") {
        npc<Neutral>(
            "They're all good soldiers, though they're pretty much a bunch of irregulars. I " +
                "consider them a good squad, ready to leap into the fray if needed, but we're " +
                "seriously outnumbered by the Drakans and their blood.",
        )
        npc<Neutral>(
            "Most fights we have to improvise but this bunch are very good at that. That young " +
                "lad over there, Ivan Strom, he saved our bacon in the last encounter by his " +
                "faith in Saradomin, he weakened the Juves and we ran for it!",
        )
        player<Quiz>("Juves? What are they?")
        npc<Neutral>(
            "It's short for 'juvenile', that's the stage that those vampires were at in the " +
                "forest. They've only recently become vampiric and they're pretty inexperienced.",
        )
        npc<Neutral>(
            "However, they're stronger and faster than many well trained militia men, and twice " +
                "as blood thirsty.",
        )
        questionsChoice()
    }

    fun ChoiceOption.graspOfSituation(): Unit = option<Quiz>("What's your grasp of the situation soldier?") {
        npc<Neutral>(
            "The situation is grave, it would be dangerous to think differently. We're really up " +
                "against it here, but we have no choice, this is a battle for our right to exist " +
                "and we need to employ any tactics to achieve our goal.",
        )
        questionsChoice()
    }

    fun ChoiceOption.heardAnyRumors(): Unit = option<Quiz>("Have you heard any rumors?") {
        npc<Neutral>(
            "Yes, I hear them all the time, thankfully I don't pay any attention to them, though.",
        )
        questionsChoice()
    }
}
