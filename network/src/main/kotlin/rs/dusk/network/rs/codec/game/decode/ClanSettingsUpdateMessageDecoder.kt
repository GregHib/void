package rs.dusk.network.rs.codec.game.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_BYTE
import rs.dusk.network.rs.codec.game.GameMessageDecoder
import rs.dusk.network.rs.codec.game.GameOpcodes.CLAN_SETTINGS_UPDATE
import rs.dusk.network.rs.codec.game.decode.message.ClanSettingsUpdateMessage

@PacketMetaData(opcodes = [CLAN_SETTINGS_UPDATE], length = VARIABLE_LENGTH_BYTE)
class ClanSettingsUpdateMessageDecoder : GameMessageDecoder<ClanSettingsUpdateMessage>() {

    override fun decode(packet: PacketReader) = ClanSettingsUpdateMessage(packet.readShort(), packet.readString())

}