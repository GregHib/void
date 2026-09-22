package content.area.asgarnia.falador

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Akrisae : Script {

    init {
        npcOperate("Talk-to", "akrisae") {
            npc<Happy>("Hello, adventurer.")
            player<Happy>("Hello, cleric.")
        }
    }
}
