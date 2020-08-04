package rs.dusk.engine.client.ui.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import rs.dusk.engine.entity.character.npc.NPC
import rs.dusk.engine.entity.character.player.Player
import java.util.*
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

class Dialogues(val player: Player) {

    var npc: NPC? = null
        private set

    private val suspensions: Queue<Pair<String, CancellableContinuation<*>>> = LinkedList()

    val isEmpty: Boolean
        get() = suspensions.isEmpty()


    fun currentType(): String {
        return suspensions.peek()?.first ?: ""
    }

    fun resume() = resume(Unit)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resume(value: T) {
        val cont = suspensions.poll().second as? CancellableContinuation<T>
        cont?.resume(value)
    }

    fun start(npc: NPC? = null, function: suspend Dialogues.() -> Unit) {
        this.npc = npc
        val coroutine = function.createCoroutine(this, DialogueContinuation)
        coroutine.resume(Unit)
    }

    suspend fun <T> await(type: String) = suspendCancellableCoroutine<T> {
        suspensions.add(type to it)
    }
}