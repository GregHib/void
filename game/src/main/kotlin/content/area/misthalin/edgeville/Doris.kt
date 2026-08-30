package content.area.misthalin.edgeville

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Doris : Script {

    init {
        npcOperate("Talk-to", "doris_edgeville") {
            npc<Scared>("What are you doing in my house?")
            choice("Select an Option") {
                option("I'm just wandering around.") {
                    player<Neutral>("I'm just wandering around.")
                    npc<Quiz>("Would you mind wandering out of my house?")
                }
                option("I want to use your kitchen.") {
                    player<Quiz>("I want to use your kitchen.")
                    npc<Sad>("I suppose you can, but try not to make a mess.")
                }
                option("Give me all your money!") {
                    player<Angry>("Give me all your money!")
                    npc<Angry>("I haven't got any money!")
                }
            }
        }
    }
}
