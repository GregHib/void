package world.gregs.voidps.engine.client.ui.dialogue

import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.suspend.SuspendableContext
import world.gregs.voidps.engine.suspend.Suspension

fun Player.talkWith(npc: NPC, def: NPCDefinition = npc.def) {
    set("dialogue_target", npc)
    set("dialogue_def", def)
}

suspend fun Player.talkWith(npc: NPC, block: suspend Dialogue.() -> Unit) {
    set("dialogue_target", npc)
    set("dialogue_def", npc.def) // TODO real def
    block(Dialogue(this, npc))
}

class Dialogue(
    override val character: Player,
    val target: NPC,
) : SuspendableContext<Player> {
    override suspend fun pause(ticks: Int) {
        Suspension.start(character, ticks)
    }
}