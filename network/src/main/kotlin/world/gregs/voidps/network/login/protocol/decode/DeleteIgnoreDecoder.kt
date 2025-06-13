package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.IgnoreDelete
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class DeleteIgnoreDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: ByteReadPacket): Instruction = IgnoreDelete(packet.readString())
}
