package rs.dusk.engine.client.ui.dialogue

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import rs.dusk.engine.entity.Entity
import java.util.*
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume

class Dialogue(private val io: DialogueIO) {

    enum class Type {
        Chat,
        Statement,
        Choice,
        String,
        Int,
        Destroy
    }

    private val suspensions: Queue<Pair<Type, CancellableContinuation<*>>> = LinkedList()

    val isEmpty: Boolean
        get() = suspensions.isEmpty()


    fun currentType(): Type? {
        return suspensions.peek()?.first
    }

    fun resume() = resume(Unit)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> resume(value: T) {
        val cont = suspensions.poll() as? CancellableContinuation<T>
        cont?.resume(value)
    }

    fun start(function: suspend Dialogue.() -> Unit) {
        val coroutine = function.createCoroutine(this, DialogueContinuation)
        coroutine.resume(Unit)
    }

    suspend fun <T> await(type: Type) = suspendCancellableCoroutine<T> {
        suspensions.add(type to it)
    }

    suspend fun stringEntry(text: String, clickToContinue: Boolean = true): String {
        io.sendStringEntry(text, clickToContinue)
        return await(Type.String)
    }

    suspend fun intEntry(text: String): String {
        io.sendIntEntry(text)
        return await(Type.Int)
    }

    suspend fun destroy(text: String, item: Int): String {
        io.sendItemDestroy(text, item)
        return await(Type.Destroy)
    }

    private suspend fun <T : Any> send(builder: DialogueBuilder, text: String, type: Type): T {
        builder.text = text
        when(type) {
            Type.Chat -> io.sendChat(builder)
            Type.Statement -> io.sendStatement(builder)
            Type.Choice -> io.sendChoice(builder)
            else -> throw UnsupportedOperationException("Unknown builder type $type")
        }
        return await(type)
    }

    suspend infix fun DialogueBuilder.dialogue(text: String): Unit = send(this, text, Type.Chat)

    suspend infix fun DialogueBuilder.statement(text: String): Unit = send(this, text, Type.Statement)

    suspend infix fun DialogueBuilder.choice(text: String): Int = send(this, text, Type.Choice)

    suspend infix fun Entity.choice(text: String) = DialogueBuilder(target = this).choice(text)

    suspend infix fun Entity.statement(text: String) = DialogueBuilder(target = this).statement(text)

    suspend infix fun Entity.dialogue(text: String) = DialogueBuilder(target = this).dialogue(text)

    infix fun Entity.animation(expression: Expression) = DialogueBuilder(target = this, expression = expression)

    infix fun Entity.title(text: String) = DialogueBuilder(target = this, title = text)

    infix fun Entity.large(boolean: Boolean) = DialogueBuilder(target = this, large = boolean)

    infix fun Entity.clickToContinue(boolean: Boolean) = DialogueBuilder(target = this, clickToContinue = boolean)
}