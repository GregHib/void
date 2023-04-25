package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.engine.client.variable.set
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

fun Player.talkWith(npc: NPC) {
    set("dialogue_target", npc)
}