package content.area.kandarin.tree_gnome_stronghold

import content.entity.player.dialogue.Idle
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Farquie : Script {

    init {
        npcOperate("Talk-to", "farquie") {
            npc<Idle>("Everyone has gone away on a honeymoon and left me here to clean up their mess.")
        }
    }
}
