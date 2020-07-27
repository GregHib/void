package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.REPORT_ABUSE
import rs.dusk.network.rs.codec.game.decode.message.ReportAbuseMessage

@PacketMetaData(opcodes = [REPORT_ABUSE], length = PacketType.VARIABLE_LENGTH_BYTE)
class ReportAbuseMessageDecoder : GameMessageDecoder<ReportAbuseMessage>() {

    override fun decode(packet: PacketReader) =
        ReportAbuseMessage(packet.readString(), packet.readByte(), packet.readByte(), packet.readString())

}