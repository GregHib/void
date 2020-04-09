package org.redrune.engine.client.verify

import org.redrune.core.network.model.message.Message
import org.redrune.engine.entity.model.Player

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 09, 2020
 */
data class Verification<T : Message>(val block: T.(Player) -> Unit)