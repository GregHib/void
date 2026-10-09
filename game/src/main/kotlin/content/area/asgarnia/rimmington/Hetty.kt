package content.area.asgarnia.rimmington

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Hetty : Script {

    init {
        npcOperate("Talk-to", "hetty_rimmington") {
            if (quest("witches_potion_miniquest").startsWith("completed")) {
                afterWitchesPotion()
            } else {
                beforeWitchesPotion()
            }
        }
    }

    private suspend fun Player.beforeWitchesPotion() {
        npc<Neutral>("What could you want with an old woman like me?")
        beforeWitchesPotionOptions()
    }

    private suspend fun Player.beforeWitchesPotionOptions() {
        choice("Select an Option") {
            lookingForQuest()
            lookLikeWitch(this@beforeWitchesPotionOptions)
        }
    }

    private suspend fun Player.afterWitchDialogueOptions() {
        choice("Select an Option") {
            lookingForQuest()
            option<Sad>("Goodbye.")
        }
    }

    private fun ChoiceOption.lookingForQuest() = option<Neutral>("I am in search of a quest.") {
        npc<Shifty>("Hmmm... Maybe I can think of something for you.")
        npc<Happy>("Would you like to become more proficient in the dark arts?")
        choice("Select an Option") {
            option<Happy>("Yes, help me become one with my darker side.") {
                continueWitchesPotionDialogue()
            }
            option<Neutral>("No, I have my principles and honour.") {
                npc<Sad>("Suit yourself, but you're missing out.")
            }
            option<Sad>("What, you mean improve my magic?") {
                statement("The witch sighs.")
                npc<Neutral>("Yes, improve your magic...")
                npc<Sad>("Do you have no sense of drama?")
                choice("Select an Option") {
                    option<Happy>("Yes, I'd like to improve my magic.") {
                        continueWitchesPotionDialogue()
                    }
                    option<Neutral>("No, I'm not interested.") {
                        npc<Neutral>("Suit yourself. Many aren't, at first.")
                        statement("The witch smiles mysteriously.")
                        npc<Shifty>("But I think you'll be drawn back to this place.")
                    }
                    option<Neutral>("Show me the mysteries of the dark arts...") {
                        continueWitchesPotionDialogue()
                    }
                }
            }
        }
    }

    private suspend fun Player.continueWitchesPotionDialogue() {
        npc<Happy>("Okay, I'm going to make a potion to help bring out your darker self.")
        npc<Neutral>("You will need certain ingredients.")
        player<Quiz>("What do I need?")
        // Witch's potion quest begins after continuing the dialogue
        npc<Neutral>("You need an eye of newt, a rat's tail, an onion... Oh and a piece of burnt meat.")
        player<Happy>("Great, I'll go and get them.")
    }

    private fun ChoiceOption.lookLikeWitch(player: Player) = option<Happy>("I've heard that you are a witch.") {
        npc<Sad>("Yes it does seem to be getting fairly common knowledge.")
        npc<Sad>("I fear I may be getting a visit from the witch hunters of Falador before long.")
        player.afterWitchDialogueOptions()
    }

    private suspend fun Player.afterWitchesPotion() {
        npc<Quiz>("How's your magic coming along?")
        player<Neutral>("I'm practicing and slowly getting better.")
        npc<Happy>("Good, good.")
    }
}
