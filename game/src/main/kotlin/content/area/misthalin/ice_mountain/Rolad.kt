package content.area.misthalin.ice_mountain

import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Rolad : Script {

    init {
        npcOperate("Talk-to", "rolad*") {
            npc<Quiz>("Can you leave me alone please? I'm trying to study.")
        }
    }
}
