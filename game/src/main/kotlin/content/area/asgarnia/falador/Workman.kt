package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Workman : Script {
    init {
        npcOperate("Talk-to", "workman_falador") {
            player<Happy>("Hiya.")
            npc<Angry>("What do you want? I've got work to do!")
            player<Quiz>("Can you teach me anything?")
            npc<Angry>("No - I've got one lousy apprentice already, and that's quite enough hassle! Go away!")
        }
    }
}
