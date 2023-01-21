package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.set

fun Player.talkWith(npc: NPC) {
    set("dialogue_target", npc)
}