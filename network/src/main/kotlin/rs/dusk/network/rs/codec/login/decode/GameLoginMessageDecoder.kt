package rs.dusk.network.rs.codec.login.decode

import rs.dusk.core.network.codec.packet.access.PacketReader
import rs.dusk.core.network.model.packet.PacketMetaData
import rs.dusk.core.network.model.packet.PacketType.Companion.VARIABLE_LENGTH_SHORT
import rs.dusk.network.rs.codec.game.GameOpcodes.GAME_LOGIN
import rs.dusk.network.rs.codec.login.LoginMessageDecoder
import rs.dusk.network.rs.codec.login.decode.message.GameLoginMessage

/**
 * @author Greg Hibberd <greg@greghibberd.com>
 * @since April 18, 2020
 */
@PacketMetaData(opcodes = [GAME_LOGIN], length = VARIABLE_LENGTH_SHORT)
class GameLoginMessageDecoder : LoginMessageDecoder<GameLoginMessage>() {

    override fun decode(packet: PacketReader): GameLoginMessage {
        val triple = LoginHeaderDecoder.decode(packet, true)
        val password = triple.second!!
        val isaacKeys = triple.third!!
        val username = packet.readString()
        packet.readUnsignedByte()// social login
        val displayMode = packet.readUnsignedByte()
        val screenWidth = packet.readUnsignedShort()
        val screenHeight = packet.readUnsignedShort()
        val antialiasLevel = packet.readUnsignedByte()
        packet.skip(24)// graphics preferences
        val settings = packet.readString()
        val affiliateId = packet.readInt()
        packet.skip(packet.readUnsignedByte())// useless settings
        val sessionId = packet.readUnsignedByte()

        val os = packet.readUnsignedByte()
        val is64Bit = packet.readUnsignedByte()
        val versionType = packet.readUnsignedByte()
        val vendorType = packet.readUnsignedByte()
        val javaRelease = packet.readUnsignedByte()
        val javaVersion = packet.readUnsignedByte()
        val javaUpdate = packet.readUnsignedByte()
        val isUnsigned = packet.readUnsignedByte()
        val heapSize = packet.readShort()
        val processorCount = packet.readUnsignedByte()
        val totalMemory = packet.readMedium()
        packet.readShort()
        packet.readUnsignedByte()
        packet.readUnsignedByte()
        packet.readUnsignedByte()
        packet.readByte()
        packet.readString()
        packet.readByte()
        packet.readString()
        packet.readByte()
        packet.readString()
        packet.readByte()
        packet.readString()
        packet.readUnsignedByte()
        packet.readShort()
        val unknown3 = packet.readInt()
        val userFlow = packet.readLong()
        val hasAdditionalInformation = packet.readUnsignedBoolean()
        if (hasAdditionalInformation) {
            val additionalInformation = packet.readString()
        }
        val hasJagtheora = packet.readUnsignedBoolean()
        val js = packet.readUnsignedBoolean()
        val hc = packet.readUnsignedBoolean()
        return GameLoginMessage(
            username,
            password,
            isaacKeys,
            displayMode,
            screenWidth,
            screenHeight,
            antialiasLevel,
            settings,
            affiliateId,
            sessionId,
            os,
            is64Bit,
            versionType,
            vendorType,
            javaRelease,
            javaVersion,
            javaUpdate,
            isUnsigned,
            heapSize,
            processorCount,
            totalMemory
        )
    }

}