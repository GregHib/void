package rs.dusk.engine.client.ui.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import rs.dusk.engine.entity.Entity
import rs.dusk.engine.entity.character.player.Player
import java.util.*
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

class Dialogues(private val io: DialogueIO, val player: Player) {

    private val suspensions: Queue<Pair<String, CancellableContinuation<*>>> = LinkedList()

    val isEmpty: Boolean
        get() = suspensions.isEmpty()


    fun currentType(): String {
        return suspensions.peek()?.first ?: ""
    }

    fun resume() = resume(Unit)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resume(value: T) {
        val cont = suspensions.poll() as? CancellableContinuation<T>
        cont?.resume(value)
    }

    fun start(function: suspend Dialogues.() -> Unit) {
        val coroutine = function.createCoroutine(this, DialogueContinuation)
        coroutine.resume(Unit)
    }

    suspend fun <T> await(type: String) = suspendCancellableCoroutine<T> {
        suspensions.add(type to it)
    }

    suspend infix fun DialogueBuilder.dialogue(text: String) {
        this.text = text
        if(io.sendChat(this)) {
            return await("chat")
        }
    }

    suspend infix fun DialogueBuilder.statement(text: String) {
        this.text = text
        if(io.sendStatement(this)) {
            return await("statement")
        }
    }

    suspend infix fun DialogueBuilder.choice(text: String): Int {
        this.text = text
        if(io.sendChoice(this)) {
            return await("choice")
        }
        return -1
    }

    suspend infix fun Entity.choice(text: String) = DialogueBuilder(target = this).choice(text)

    suspend infix fun Entity.statement(text: String) = DialogueBuilder(target = this).statement(text)

    suspend infix fun Entity.dialogue(text: String) = DialogueBuilder(target = this).dialogue(text)

    infix fun DialogueBuilder.animation(expression: Expression) = apply { this.expression = expression }

    infix fun Entity.animation(expression: Expression) = DialogueBuilder(target = this, expression = expression)

    infix fun DialogueBuilder.title(text: String) = apply { title = text }

    infix fun Entity.title(text: String) = DialogueBuilder(target = this, title = text)

    infix fun DialogueBuilder.large(boolean: Boolean) = apply { large = boolean }

    infix fun Entity.large(boolean: Boolean) = DialogueBuilder(target = this, large = boolean)

    infix fun DialogueBuilder.clickToContinue(boolean: Boolean) = apply { clickToContinue = boolean }

    infix fun Entity.clickToContinue(boolean: Boolean) = DialogueBuilder(target = this, clickToContinue = boolean)
}