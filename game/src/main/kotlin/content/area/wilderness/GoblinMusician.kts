package content.area.wilderness

import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.SuspendableContext
import content.entity.player.dialogue.HappyOld
import content.entity.player.dialogue.NeutralOld
import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.PlayerChoice
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc

npcOperate("Talk-to", "goblin_musician") {
    choice()
}

suspend fun SuspendableContext<Player>.choice() {
    choice {
        option<Quiz>("Who are you?") {
            npc<HappyOld>("Me? Thump-Thump. Me make thump-thumps with thump-thump drum. Other goblins listen.")
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
            npc<NeutralOld>("You stoopid. Goblin sit down, goblin rest, goblin feel better.")
            resting()
        }
        option<Pleased>("What's special about resting by a musician?") {
            npc<NeutralOld>("Drumming good! Make you feel better, boom booms make you run longer!")
            resting()
        }
        option<Pleased>("Can you summarise the effects for me?") {
            npc<NeutralOld>("Wot? You sit down, you rest. Listen to Thump-Thump is better.")
            resting()
        }
        exit()
    }
}

suspend fun PlayerChoice.exit(): Unit = option<Quiz>("That's all for now.") {
    npc<HappyOld>("You listen to boom boom. Good!")
}