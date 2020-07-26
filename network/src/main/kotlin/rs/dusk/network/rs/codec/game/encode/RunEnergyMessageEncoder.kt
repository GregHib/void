package rs.dusk.network.rs.codec.game.encode

import rs.dusk.core.network.codec.packet.access.PacketWriter
import rs.dusk.network.rs.codec.game.GameMessageEncoder
import rs.dusk.network.rs.codec.game.GameOpcodes.RUN_ENERGY
import rs.dusk.network.rs.codec.game.encode.message.RunEnergyMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since July 27, 2020
 */
class RunEnergyMessageEncoder : GameMessageEncoder<RunEnergyMessage>() {

    override fun encode(builder: PacketWriter, msg: RunEnergyMessage) {
        builder.writeOpcode(RUN_ENERGY)
        builder.writeByte(msg.energy)
    }
}