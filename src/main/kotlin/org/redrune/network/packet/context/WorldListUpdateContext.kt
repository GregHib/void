package org.redrune.network.packet.context

import org.redrune.network.packet.PacketContext

/**
 * @author Tyluur <contact@kiaira.tech>
 * @since 2020-01-18
 */
data class WorldListUpdateContext(val updateType: Int) : PacketContext()