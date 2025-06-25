package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.login.protocol.Decoder

class ClientDetailOptionsStatusDecoder : Decoder(BYTE) {
    @OptIn(ExperimentalUnsignedTypes::class)
    override suspend fun decode(packet: ByteReadPacket): Instruction? {
        val x = packet.readByte()
        val antialiasingMode        = packet.readByte()
        val bloom                   = packet.readByte()
        val brightness              = packet.readByte()
        val buildArea               = packet.readByte()
        val flickeringEffects       = packet.readByte()
        val fog                     = packet.readByte()
        val groundBlending          = packet.readByte()
        val groundDecor             = packet.readByte()
        val idleAnimations          = packet.readByte()
        val lightDetail             = packet.readByte()
        val hardShadows             = packet.readByte()
        val orthographic            = packet.readByte()
        val particles               = packet.readByte()
        val removeRoofs             = packet.readByte()
        val maxScreenSize           = packet.readByte()
        val skydetail               = packet.readByte()
        val spotShadows             = packet.readByte()
        val smallTextures           = packet.readByte()
        val textures                = packet.readByte()
        val toolkitDefault          = packet.readByte()
        val animateBackgroundDefault= packet.readByte()
        val waterDetail             = packet.readByte()
        val screenSizeDefault       = packet.readByte()
        val customCursors           = packet.readByte()
        val graphicsQuality         = packet.readByte()
        val cpuUsage                = packet.readByte()
        val loadingSequence         = packet.readByte()
        val safeMode                = packet.readByte()
        val soundVolume             = packet.readByte()
        val backgroundSoundVolume   = packet.readByte()
        val speechVolume            = packet.readByte()
        val musicVolume             = packet.readByte()
        val loginVolume             = packet.readByte()
        val stereoSound             = packet.readByte()

        return null
    }
}