package world.gregs.voidps.engine.client.ui.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.Suspension
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.watch
import world.gregs.voidps.engine.entity.set
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

class Dialogues(
    private val continuation: Continuation<Any> = DialogueContinuation
) {

    private val suspensions: Queue<DialogueContext> = LinkedList()

    private val isEmpty: Boolean
        get() = suspensions.isEmpty()

    private fun currentType(): String {
        return suspensions.peek()?.suspensionType ?: ""
    }

    private fun resume() = resume(Unit)

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> resume(value: T) {
        val cont = suspensions.poll().coroutine as? CancellableContinuation<T>
        cont?.resume(value)
    }

    fun start(player: Player, npc: NPC? = null, function: suspend DialogueContext.() -> Unit) {
        start(DialogueContext(this, player, npc), function)
    }

    fun start(context: DialogueContext, function: suspend DialogueContext.() -> Unit) {
        val coroutine = function.createCoroutine(context, continuation)
        coroutine.resume(Unit)
    }

    fun add(context: DialogueContext) {
        suspensions.add(context)
    }

    fun clear() {
        suspensions.forEach {
            it.coroutine?.cancel()
            it.close()
        }
        suspensions.clear()
    }
}

fun Player.talkWith(npc: NPC) {
    set("dialogue_target", npc)
}

// TODO convert existing. How to handle dialogue target when not interacting? Convert rest of interface interactions to use queues.

@Deprecated("Remove")
fun Player.talkWith(npc: NPC, function: suspend DialogueContext.() -> Unit) {
    npc.action(ActionType.Dialogue) {
        await(Suspension.Infinite)
    }
    dialogues.clear()
    dialogues.start(this, npc) {
        try {
            npc.mode = EmptyMode
            npc.watch(player)
            function.invoke(this)
        } finally {
            npc.action.cancel(ActionType.Dialogue)
            npc.watch(null)
        }
    }
}

fun Player.dialogue(npc: NPC? = null, function: suspend DialogueContext.() -> Unit) {
    (mode as? Interact)?.onStop = {
        dialogues.clear()
    }
    dialogues.start(this, npc, function)
}

suspend fun Player.awaitDialogue(npc: NPC? = null, function: suspend DialogueContext.() -> Unit) {
    suspendCancellableCoroutine<Unit> { cont ->
        dialogues.start(this, npc) {
            cont.resume(Unit)
            function(this)
        }
    }
}