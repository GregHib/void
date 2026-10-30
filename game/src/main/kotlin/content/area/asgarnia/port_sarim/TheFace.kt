package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.type.equals

class TheFace : Script {

    init {
        npcOperate("Talk-to", "the_face_port_sarim") {
            player<Neutral>("Hello.")
            statement("She looks through you as if you don't exist.")
        }

        objTeleportTakeOff("Climb-down", "*") { target, _ ->
            if (!target.tile.equals(3018, 3232) || questCompleted("rat_catchers")) {
                return@objTeleportTakeOff Teleport.CONTINUE
            }
            npc<Angry>("the_face_port_sarim", "You don't have business down there so stay away!")
            Teleport.CANCEL
        }
    }
}
