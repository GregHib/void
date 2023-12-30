package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.Talk
import world.gregs.voidps.world.interact.dialogue.type.npc

on<NPCOption>({ operate && target.id == "iconis" && option == "Talk-to" }) { player: Player ->
    if (!World.members) {
        nonMember()
        return@on
    }
}

on<NPCOption>({ operate && target.id == "iconis" && option == "Take-picture" }) { player: Player ->
    if (!World.members) {
        nonMember()
        return@on
    }
}

suspend fun NPCOption.nonMember() {
    npc<Talk>("Good day! I'm afraid you can't use the booth's services on a non-members world. Film costs a lot you know!")
}