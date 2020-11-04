package rs.dusk.engine.client.ui.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.npc.detail.NPCDefinitions
import rs.dusk.engine.entity.character.player.Player
import rs.dusk.utility.get
import java.util.*
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

class Dialogues {

    private val suspensions: Queue<DialogueContext> = LinkedList()

    val isEmpty: Boolean
        get() = suspensions.isEmpty()


    fun currentType(): String {
        return suspensions.peek()?.suspensionType ?: ""
    }

    fun resume() = resume(Unit)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resume(value: T) {
        val cont = suspensions.poll().coroutine as? CancellableContinuation<T>
        cont?.resume(value)
    }

    fun start(player: Player, npc: NPC? = null, function: suspend DialogueContext.() -> Unit) {
        start(DialogueContext(this, player, npc), function)
    }

    fun start(player: Player, npcId: Int, npcName: String, function: suspend DialogueContext.() -> Unit) {
        start(DialogueContext(this, player, npcId, npcName), function)
    }

    fun start(context: DialogueContext, function: suspend DialogueContext.() -> Unit) {
        val coroutine = function.createCoroutine(context, DialogueContinuation)
        coroutine.resume(Unit)
    }

    fun add(context: DialogueContext) {
        suspensions.add(context)
    }

    fun clear() {
        val throwable = CancellationException("Dialogues cleared")
        suspensions.forEach {
            it.coroutine?.cancel(throwable)
        }
        suspensions.clear()
    }
}

fun Player.dialogue(id: String, function: suspend DialogueContext.() -> Unit) {
    val definitions: NPCDefinitions = get()
    val npcId = definitions.getId(id)
    dialogues.start(this, npcId, definitions.getName(npcId), function)
}

fun Player.dialogue(id: Int, name: String, function: suspend DialogueContext.() -> Unit) {
    dialogues.start(this, id, name, function)
}

fun Player.dialogue(npc: NPC? = null, function: suspend DialogueContext.() -> Unit) {
    dialogues.start(this, npc, function)
}