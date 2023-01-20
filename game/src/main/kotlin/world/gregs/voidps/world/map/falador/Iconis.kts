import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.members
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.dialogue.type.npc

on<NPCOption>({ npc.id == "iconis" && option == "Talk-to" }) { player: Player ->
    if (!World.members) {
        nonMember(player, npc)
        return@on
    }
}

on<NPCOption>({ npc.id == "iconis" && option == "Take-picture" }) { player: Player ->
    if (!World.members) {
        nonMember(player, npc)
        return@on
    }
}

fun nonMember(player: Player, npc: NPC) {
    player.talkWith(npc) {
        npc("talk", """
            Good day! I'm afraid you can't use the booth's services on
            a non-members world. Film costs a lot you know!
        """)
    }
}