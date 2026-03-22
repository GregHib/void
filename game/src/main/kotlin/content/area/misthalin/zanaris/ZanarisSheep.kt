package content.area.misthalin.zanaris

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class ZanarisSheep : Script {
    init {
        npcOperate("Talk-to", "sheep_zanaris,sheep_zanaris_2") {
            npc<Quiz>("Hello, I've just finished composing my latest poem. Would you like to hear it?")
            choice {
                option<Neutral>("Yes, please.") {
                    npc<Happy>("Excellent! The humans an aficionado! Make yourself comfortable and I'll begin...")
                    npc<Neutral>("Twinkle, twinkle little egg Wouldn't you like to grow some legs? And run around, on grass so green arguing with the milk and cream.")
                }
                option<Neutral>("No thanks.") {
                    npc<Angry>("Huh, the cow's right. You humans have absolutely no understanding of art!")
                }
            }
        }
    }
}
