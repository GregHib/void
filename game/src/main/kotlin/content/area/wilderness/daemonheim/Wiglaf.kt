package content.area.wilderness.daemonheim

import content.entity.player.dialogue.Pleased
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Wiglaf : Script {
    init {
        npcOperate("Talk-to", "wiglaf") {
            npc<Pleased>("I'm sorry, I'm really busy. Maybe next time?")
        }
    }
}
