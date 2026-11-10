package content.area.fremennik_province.keldagrim

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name

/**
 * Veldaban after King of the Dwarves.
 * https://runescape.wiki/w/Transcript:King_of_the_Dwarves#King_Veldaban
 */
class KingVeldaban : Script {

    init {
        npcOperate("Talk-to", "king_veldaban_2") {
            npc<Neutral>("Hello, $name.")
            choice {
                option<Neutral>("Hello, Veldaban.") {
                    topics()
                }
                option<Neutral>("Your Majesty.") {
                    npc<Sad>("Oh, don't call me that, $name. You of all people...")
                    topics()
                }
            }
        }
    }

    private suspend fun Player.topics() {
        npc<Quiz>("What do you want to talk about?")
        // "So you're a king - what now?" is left out, the only transcript of it was rewritten for Birthright of the Dwarves
        choice {
            option<Quiz>("I don't understand how you ended up as king.") {
                npc<Shifty>("Meike altered the records. It only took a few pen-strokes, she said. It looks like I, and not Hreidmar, descended from King Alvis's secret heir.")
                npc<Shifty>("You, I and Meike are the only people who know it isn't legitimate. This must stay our secret, $name.")
                npc<Sad>("I never wanted this kind of power, but it was the only way to keep Hreidmar off the throne.")
                legitimacy()
            }
            option<Neutral>("Goodbye.")
        }
    }

    private suspend fun Player.legitimacy() {
        choice {
            question("Shouldn't your father, Dondakan, be king?") {
                npc<Neutral>("The old rules of succession included a maximum age. Once the king reaches a hundred and fifty, the throne passes on to their heir.")
            }
            question("Why didn't Meike make herself queen?") {
                npc<Neutral>("The monarchy was for men only and everyone knows that. No one would have believed the records if they had pointed to Meike.")
            }
            question("What's happened to the Consortium now?") {
                npc<Neutral>("The Consortium still exists. It's gone back to being an advisory body, just like it was in King Alvis's day.")
                npc<Neutral>("I don't like the Consortium's priorities, but they've got experience in running the city that I don't. I still need them around.")
            }
            question("Will you have the statue rebuilt with your head?") {
                npc<Neutral>("It doesn't seem right. Maybe one day, but not anytime soon.")
            }
            option<Neutral>("Goodbye.")
        }
    }

    private fun ChoiceOption.question(text: String, block: suspend Player.() -> Unit) {
        option<Quiz>(text) {
            block()
            legitimacy()
        }
    }
}
