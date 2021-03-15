package world.gregs.voidps.network.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.Decoder

class PublicQuickChatDecoder : Decoder(BYTE) {

    override fun decode(player: Player, packet: Reader) {
        handler?.publicQuickChat(
            player = player,
            script = packet.readByte(),
            file = packet.readUnsignedShort(),
            data = ByteArray(packet.readableBytes()).apply {
                packet.readBytes(this)
            }
        )
    }

}