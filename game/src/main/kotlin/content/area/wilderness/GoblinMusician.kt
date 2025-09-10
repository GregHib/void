package content.area.wilderness

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.PlayerChoice
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.event.Script
@Script
class GoblinMusician {

    init {
        npcOperate("Talk-to", "goblin_musician") {
            choice()
        }

    }

    suspend fun SuspendableContext<Player>.choice() {
        choice {
            option<Quiz>("Who are you?") {
                npc<Happy>("Me? Thump-Thump. Me make thump-thumps with thump-thump drum. Other goblins listen.")
                choice()
            }
            option("Can I ask you some questions about resting?") {
                resting()
            }
            exit()
        }
    }
    
    suspend fun SuspendableContext<Player>.resting() {
        choice("Can I ask you some questions about resting?") {
            option<Quiz>("How does resting work?") {
                npc<Neutral>("You stoopid. Goblin sit down, goblin rest, goblin feel better.")
                resting()
            }
            option<Pleased>("What's special about resting by a musician?") {
                npc<Neutral>("Drumming good! Make you feel better, boom booms make you run longer!")
                resting()
            }
            option<Pleased>("Can you summarise the effects for me?") {
                npc<Neutral>("Wot? You sit down, you rest. Listen to Thump-Thump is better.")
                resting()
            }
            exit()
        }
    }
    
    suspend fun PlayerChoice.exit(): Unit = option<Quiz>("That's all for now.") {
        npc<Happy>("You listen to boom boom. Good!")
    }
}
