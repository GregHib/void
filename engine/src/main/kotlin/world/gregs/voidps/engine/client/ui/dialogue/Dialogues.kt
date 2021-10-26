package world.gregs.voidps.engine.client.ui.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.watch
import world.gregs.voidps.engine.entity.definition.NPCDefinitions
import world.gregs.voidps.engine.utility.get
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

    fun start(player: Player, npcId: String, title: String, function: suspend DialogueContext.() -> Unit) {
        start(DialogueContext(this, player, npcId, title), function)
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
            it.close()
        }
        suspensions.clear()
    }
}

fun Player.dialogue(id: String, function: suspend DialogueContext.() -> Unit) {
    val definitions: NPCDefinitions = get()
    dialogues.start(this, id, definitions.get(id).name, function)
}

fun Player.dialogue(id: String, title: String, function: suspend DialogueContext.() -> Unit) {
    dialogues.start(this, id, title, function)
}

fun Player.talkWith(npc: NPC, function: suspend DialogueContext.() -> Unit) {
    dialogues.start(this, npc) {
        try {
            npc.movement.clear()
            npc.watch(player)
            function.invoke(this)
        } finally {
            npc.watch(null)
        }
    }
}

fun Player.dialogue(npc: NPC? = null, function: suspend DialogueContext.() -> Unit) {
    dialogues.start(this, npc, function)
}