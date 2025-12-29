package world.gregs.voidps.engine.client.ui.dialogue

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import world.gregs.voidps.cache.definition.type.NPCType
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Wildcard
import world.gregs.voidps.engine.event.Wildcards

interface Dialogues {
    fun continueDialogue(id: String = "*", handler: Player.(id: String) -> Unit) {
        Wildcards.find(id, Wildcard.Component) { i ->
            handlers[i] = handler
        }
    }
    fun continueItemDialogue(handler: Player.(String) -> Unit) {
        itemHandler = handler
    }

    companion object : AutoCloseable {
        private var itemHandler: (Player.(String) -> Unit)? = null
        private val handlers = Object2ObjectOpenHashMap<String, Player.(String) -> Unit>(30)

        fun continueDialogue(player: Player, id: String) {
            handlers[id]?.invoke(player, id)
        }

        fun continueItem(player: Player, item: String) {
            itemHandler?.invoke(player, item)
        }

        override fun close() {
            handlers.clear()
            itemHandler = null
        }
    }
}

fun Player.talkWith(npc: NPC, def: NPCType = npc.def) {
    set("dialogue_target", npc)
    set("dialogue_def", def)
}

suspend fun Player.talkWith(npc: NPC, block: suspend Dialogue.() -> Unit) {
    set("dialogue_target", npc)
    set("dialogue_def", npc.def(this))
    block(Dialogue(this, npc))
}

class Dialogue(
    val character: Player,
    val target: NPC,
)