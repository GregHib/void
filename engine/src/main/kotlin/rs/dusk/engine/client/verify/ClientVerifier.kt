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
val clientVerificationModule = module {
    single { ClientVerifier() }
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

    override fun <T : Message> verify(clazz: KClass<T>, player: Player, message: T) {
        val verifier = get(clazz) ?: throw IllegalArgumentException("No verification found for player message $player - $clazz")
        verifier.block(message, player)
    }

}