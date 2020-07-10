package rs.dusk.engine.client.verify

import org.koin.dsl.module
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.model.entity.index.player.Player
import rs.dusk.network.rs.codec.game.MessageCompanion
import rs.dusk.utility.get
import kotlin.collections.set
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
@Suppress("USELESS_CAST")
val clientVerificationModule = module {
    single { ClientVerification() }
}

class ClientVerification {
    val verifications = mutableMapOf<KClass<*>, Verification<*>>()

    /**
     * Adds a [Verification] for messages of type [clazz]
     */
    fun <T : Message> add(clazz: KClass<T>, verification: Verification<T>) {
        if (verifications.containsKey(clazz)) {
            throw IllegalArgumentException("Duplicate client verifier $clazz.")
        }
        verifications[clazz] = verification
    }

    /**
     * Returns [Verification] with matching [clazz]
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Message> get(clazz: KClass<T>): Verification<T>? {
        return verifications[clazz] as? Verification<T>
    }

    /**
     * Runs [Verification] verification on [message]
     */
    fun <T : Message> verify(player: Player, clazz: KClass<T>, message: T) {
        val verifier =
            get(clazz) ?: throw IllegalArgumentException("No verification found for player $player - $message")
        verifier.block(message, player)
    }

    /**
     * Helper function for verifying messages
     */
    inline fun <reified T : Message> verify(player: Player, event: T) = verify(player, T::class, event)
}

/**
 * Registers a simple event handler without filter or priority
 */
inline infix fun <reified T : Message, reified C : MessageCompanion<T>> C.verify(noinline action: T.(Player) -> Unit) {
    val verify: ClientVerification = get()
    verify.add(T::class, Verification(action))
}