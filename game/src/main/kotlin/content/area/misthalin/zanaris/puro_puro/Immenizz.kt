package content.area.misthalin.zanaris.puro_puro

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.ChoiceOption
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.type.Tile

class Immenizz : Script {
    init {
        npcOperate("Talk-to", "immenizz") {
            choice {
                place()
                portal()
                option<Bored>("I'll leave you alone.")
            }
        }
        npcOperate("Quick-leave", "immenizz") {
            // TODO check how this works
            tele(2426, 4445)
        }
    }

    private fun ChoiceOption.place() {
        option<Neutral>("What is this place?") {
            npc<Happy>("This is my home, mundane human! What do you have in your pockets? Something tasty?")
            player<Angry>("Stay out of my pockets! I don't have anything that you want.")
            npc<Happy>("Ah, but do you have anything that *you* want?")
            player<Neutral>("Of course I do!")
            npc<Happy>("Then you have something that implings want.")
            player<Quiz>("Eh?")
            npc<Happy>("We want things you people want. They are tasty to us! The more you want them, the tastier they are!")
            player<Quiz>("So, you collect things that humans want? Interesting... So, what would happen if I caught an impling in a butterfly net?")
            npc<Angry>("Don't do that! That would be cruel. But chase us, yes! That is good. Implings are not easy to catch. Especially ones with really tasty food.")
            player<Quiz>("So, some of these implings have things that I will really want? Hmm, maybe it would be worth my while trying to catch some.")
            choice {
                portal()
                option<Bored>("I'll leave you alone.")
            }
        }
    }

    private fun ChoiceOption.portal() {
        option<Quiz>("Tell me about this portal.") {
            npc<Happy>("You want to know about the portal? It is for leaving this place, of course!")
            player<Happy>("Where will I go if I leave?")
            npc<Happy>("Back where you came from, of course!")
            player<Happy>("So not back to where the crop circle is now?")
            npc<Happy>("The aetheric thread connecting you to your own plane leads back to the wheat field where you came from. Obviously. So you will always go back to where you came from! Don't you humans know anything?")
            player<Neutral>("Evidently not.")
            npc<Happy>("You wanna leave?")
            choice {
                option<Happy>("Yes, get me out of here!") {
                    walkToDelay(Tile(2592, 4319))
                    Teleport.teleport(this, Tile(2426, 4445), "puro_puro")
                }
                option<Happy>("Ah, no I'll hang around a bit longer.")
            }
        }
    }
}
