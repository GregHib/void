package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.FinishRegionLoad
import world.gregs.voidps.network.login.protocol.Decoder

class RegionLoadedDecoder : Decoder(0) {

    override suspend fun decode(packet: ByteReadPacket): Instruction = FinishRegionLoad
}
