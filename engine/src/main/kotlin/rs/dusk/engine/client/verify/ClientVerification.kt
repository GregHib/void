package rs.dusk.engine.client.verify

import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.entity.model.Player
import rs.dusk.utility.get
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
abstract class ClientVerification {
    /**
     * Adds a [Verification] for messages of type [clazz]
     */
    abstract fun <T : Message> add(clazz: KClass<T>, verification: Verification<T>)

    /**
     * Returns [Verification] with matching [clazz]
     */
    abstract fun <T : Message> get(clazz: KClass<T>): Verification<T>?

    /**
     * Runs [Verification] verification on [message]
     */
    abstract fun <T : Message> verify(clazz: KClass<T>, player: Player, message: T)

    /**
     * Helper function for verifying messages
     */
    inline fun <reified T : Message> verify(player: Player, event: T) = verify(T::class, player, event)
}

/**
 * Registers a simple event handler without filter or priority
 */
inline infix fun <reified T : Message, reified C : MessageCompanion<T>> C.verify(noinline action: T.(Player) -> Unit) {
    val verify: ClientVerification = get()
    verify.add(T::class, Verification(action))
}