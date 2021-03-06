package world.gregs.voidps.network.codec.login.decode

import world.gregs.voidps.buffer.read.Reader
import world.gregs.voidps.network.ClientSession
import world.gregs.voidps.network.codec.Decoder
import world.gregs.voidps.network.packet.PacketSize.SHORT

/**
 * @author GregHib <greg@gregs.world>
 * @since April 18, 2020
 */
class GameLoginDecoder : Decoder(SHORT) {

    override fun decode(session: ClientSession, packet: Reader) {
        val triple = LoginHeaderDecoder.decode(packet)
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
        val totalMemory = packet.readUnsignedMedium()
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
        handler?.loginGame(
            session = session,
            username = username,
            password = password,
            isaacKeys = isaacKeys,
            mode = displayMode,
            width = screenWidth,
            height = screenHeight,
            antialias = antialiasLevel,
            settings = settings,
            affiliate = affiliateId,
            sessionId = sessionId,
            os = os,
            is64Bit = is64Bit,
            versionType = versionType,
            vendorType = vendorType,
            javaRelease = javaRelease,
            javaVersion = javaVersion,
            javaUpdate = javaUpdate,
            isUnsigned = isUnsigned,
            heapSize = heapSize,
            processorCount = processorCount,
            totalMemory = totalMemory
        )
    }

}