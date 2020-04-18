package rs.dusk.engine.client.verify

import rs.dusk.core.network.model.message.Message
import rs.dusk.engine.entity.model.Player

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
data class Verification<T : Message>(val block: T.(Player) -> Unit)