package rs.dusk.engine.client.verify

import org.koin.dsl.module
import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.entity.model.Player
import kotlin.collections.set
import kotlin.reflect.KClass

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
@Suppress("USELESS_CAST")
val clientVerificationModule = module {
    single { ClientVerifier() as ClientVerification }
}

class ClientVerifier : ClientVerification() {
    val verifications = mutableMapOf<KClass<*>, Verification<*>>()

    override fun <T : Message> add(clazz: KClass<T>, verification: Verification<T>) {
        if (verifications.containsKey(clazz)) {
            throw IllegalArgumentException("Duplicate client verifier $clazz.")
        }
        verifications[clazz] = verification
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Message> get(clazz: KClass<T>): Verification<T>? {
        return verifications[clazz] as? Verification<T>
    }

    override fun <T : Message> verify(player: Player, clazz: KClass<T>, message: T) {
        val verifier =
            get(clazz) ?: throw IllegalArgumentException("No verification found for player $player - $message")
        verifier.block(message, player)
    }

}