package world.gregs.voidps.network.login.protocol.decode

import io.ktor.utils.io.core.*
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.FriendAdd
import world.gregs.voidps.network.login.protocol.Decoder
import world.gregs.voidps.network.login.protocol.readString

class AddFriendDecoder : Decoder(BYTE) {

    override suspend fun decode(packet: ByteReadPacket): Instruction = FriendAdd(packet.readString())
}
