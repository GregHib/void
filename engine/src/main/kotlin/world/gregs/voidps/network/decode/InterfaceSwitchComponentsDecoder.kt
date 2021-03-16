package world.gregs.voidps.network.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder
import world.gregs.voidps.network.readShortAddLittle
import world.gregs.voidps.network.readUnsignedIntMiddle

class InterfaceSwitchComponentsDecoder : Decoder(16) {

    override fun decode(player: Player, packet: ByteReadPacket) {
        handler?.interfaceSwitch(
            player = player,
            fromHash = packet.readInt(),
            toSlot = packet.readShortLittleEndian().toInt(),
            toHash = packet.readUnsignedIntMiddle(),
            fromType = packet.readShort().toInt(),
            fromSlot = packet.readShortAddLittle(),
            toType = packet.readShortAddLittle()
        )
    }

}