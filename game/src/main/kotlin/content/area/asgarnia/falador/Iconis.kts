package content.area.asgarnia.falador

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.npc

npcOperate("Talk-to", "iconis") {
    if (!World.members) {
        nonMember()
        return@npcOperate
    }
}

npcOperate("Take-picture", "iconis") {
    if (!World.members) {
        nonMember()
        return@npcOperate
    }
}

suspend fun NPCOption<Player>.nonMember() {
    npc<Talk>("Good day! I'm afraid you can't use the booth's services on a non-members world. Film costs a lot you know!")
}