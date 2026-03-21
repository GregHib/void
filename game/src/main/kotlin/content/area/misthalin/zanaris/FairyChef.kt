package content.area.misthalin.zanaris

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class FairyChef : Script {
    init {
        npcOperate("Talk-to", "fairy_chef") {
            npc<Happy>("'Ello, sugar. I'm afraid I can't gossip right now, I've got a cake in the oven.")
        }
    }
}
