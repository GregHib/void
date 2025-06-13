package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player

fun Player.talkWith(npc: NPC, def: NPCDefinition = npc.def) {
    set("dialogue_target", npc)
    set("dialogue_def", def)
}
