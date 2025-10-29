package content.area.asgarnia.falador

import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.ui.dialogue.Dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.event.Script

@Script
class Iconis : Api {

    init {
        npcOperateDialogue("Talk-to", "iconis") {
            if (!World.members) {
                nonMember()
                return@npcOperateDialogue
            }
        }

        npcOperate("Take-picture", "iconis") { player, target ->
            if (!World.members) {
                player.talkWith(target) {
                    nonMember()
                }
                return@npcOperate
            }
        }
    }

    suspend fun Dialogue.nonMember() {
        npc<Talk>("Good day! I'm afraid you can't use the booth's services on a non-members world. Film costs a lot you know!")
    }
}
