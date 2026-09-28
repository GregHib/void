package content.area.asgarnia.falador

import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Silif : Script {

    init {
        npcOperate("Talk-to", "silif_falador") {
            player<Idle>("Who are you?")
            npc<Neutral>("Who, me? Oh, no one, I assure you. No one that you want to tangle with!")
        }
    }
}
